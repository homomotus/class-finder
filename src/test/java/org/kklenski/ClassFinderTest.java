package org.kklenski;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

public class ClassFinderTest {
    
    @Test
    public void testBasicSuccessCases() {
        String[] classNames = new String[] {"c.d.FooBar", "a.b.FooBarBaz"};
        
        assertArrayEquals(classNames, findMatching(classNames, "FB"));
        assertArrayEquals(classNames, findMatching(classNames, "FoBa"));
        assertArrayEquals(classNames, findMatching(classNames, "FBar"));
    }
    
    @Test
    public void testBasicFailureCases() {
        String[] classNames = new String[] {"c.d.FooBar", "a.b.FooBarBaz"};
        
        assertArrayEquals(new String[]{}, findMatching(classNames, "B"));
        assertArrayEquals(new String[]{}, findMatching(classNames, "Fob"));
        assertArrayEquals(new String[]{}, findMatching(classNames, "FooBarBazz"));
    }
    
    public void testEmptyPattern() throws Exception {
    	String[] classNames = new String[] {"c.d.FooBar", "a.b.FooBarBaz"};
    	
        assertArrayEquals(new String[]{}, findMatching(classNames, ""));
	}    
    
    @Test
    public void testSpace() throws Exception {
        String[] classNames = new String[] {"a.b.FooBarBaz", "c.d.FooBar"};
        String[] expected = new String[] {"c.d.FooBar"};
        assertArrayEquals(expected, findMatching(classNames, "FBar "));
        
        classNames = new String[] {"Geek", "Geek_Out", "GeekOut_", "Geeky", "Geek_", "Geek__"};
        expected = new String[] {"Geek", "Geek_", "Geek_Out", "Geek__"};
        assertArrayEquals(expected, findMatching(classNames, "Geek "));
        
        assertArrayEquals(new String[] {"Geek_", "Geek_Out", "Geek__"}, findMatching(classNames, "Geek_"));
        assertArrayEquals(new String[] {"Geek__"}, findMatching(classNames, "Geek__"));
        assertArrayEquals(new String[] {"Geek__"}, findMatching(classNames, "Geek _"));
        
        //XXX: Strange, but two cases below do not behave like this in IDEA
        assertArrayEquals(new String[] {"Geek_", "Geek__"}, findMatching(classNames, "Geek_ "));
        assertArrayEquals(new String[] {"Geek_", "Geek__"}, findMatching(classNames, "Geek  "));
        
        expected = new String[] {"GeekOut_", "Geek_Out"};
        assertArrayEquals(expected, findMatching(classNames, "Geek*Out"));
        
        expected = new String[] {"Geek_Out"};
        assertArrayEquals(expected, findMatching(classNames, "Geek Out"));
        
        classNames = new String[] {"_", "Geek"};
        expected = new String[] {"_"};
        assertArrayEquals(expected, findMatching(classNames, "_"));
    }
    
    @Test
    public void testWildcard() throws Exception {
        String[] classNames = new String[] {"c.d.FooBar", "a.b.FooBarBaz"};
        String[] expected = new String[] {"a.b.FooBarBaz"};
        assertArrayEquals(expected, findMatching(classNames, "F*Baz"));
        assertArrayEquals(expected, findMatching(classNames, "*BB"));
        assertArrayEquals(expected, findMatching(classNames, "F*az"));
        assertArrayEquals(classNames, findMatching(classNames, "*"));
        assertArrayEquals(classNames, findMatching(classNames, "**"));
        assertArrayEquals(classNames, findMatching(classNames, "FB*"));
        
        classNames = new String[] {"c.d.FooBar", "a.b.FooBarBaz"};
        expected = new String[] {"a.b.FooBarBaz"};
        assertArrayEquals(classNames, findMatching(classNames, "F*b"));
        assertArrayEquals(expected, findMatching(classNames, "F**z"));
        assertArrayEquals(expected, findMatching(classNames, "F*a*z"));
        assertArrayEquals(expected, findMatching(classNames, "*Foo*Bar*Baz*"));
        assertArrayEquals(expected, findMatching(classNames, "*F*a*z"));
        
    	classNames = new String[] {"a.b.FooBarBaz", "c.d.FooBar"};
    	expected = new String[] {"a.b.FooBarBaz"};
    	assertArrayEquals(expected, findMatching(classNames, "F*az"));
    }
    
    @Test
    public void testSorting() throws Exception {
        String[] classNames = new String[] {"a.b.FooBarDaz", "c.d.FooBarBaz", "e.f.FooBarCaz"};
        String[] expected = new String[] {"c.d.FooBarBaz", "e.f.FooBarCaz", "a.b.FooBarDaz"};
        assertArrayEquals(expected, findMatching(classNames, "FB"));
    }
    
    @Test
    public void testGetSimpleName() {
    	assertEquals("FooBar", ClassFinder.getSimpleName("a.b.FooBar"));
    	assertEquals("FooBar", ClassFinder.getSimpleName("FooBar"));
    	assertEquals("", ClassFinder.getSimpleName("a."));
	}
    
    @Test
    public void testNormalizePattern() {
    	assertEquals("foo*bar", ClassFinder.normalizePattern("FooBar"));
    	assertEquals("foo*bar*baz", ClassFinder.normalizePattern("FooBarBaz"));
    	assertEquals("foo*ar*baz", ClassFinder.normalizePattern("foo*arBaz"));
    	assertEquals("*foo*bar*baz*", ClassFinder.normalizePattern("*Foo*Bar*Baz*"));
    	assertEquals("*foo*b", ClassFinder.normalizePattern("*FooB"));
    }
    
    @Ignore(value="Introduced to evaluate the algorythm performance.")
    public void testPerformance() throws Exception {
		String[] classes = new String[100000];
		Random rnd = new Random();
		for (int i = 0; i < classes.length; i++) {
			classes[i] = "a.b.Foo"+rnd.nextInt(1000)+"Bar"+rnd.nextInt(1000)+"Baz"+rnd.nextInt(1000);
		}
		findMatching(classes, "*Foo*Bar*Baz*");
	}
    
    //FIXME: test executing findMatching more than one time
    private String[] findMatching(String[] classNames, String pattern) {
        InputStream in = createStream(classNames);
        try {
            return new ClassFinder(in).findMatching(pattern).toArray(new String[] {});
        } finally {
            try {
                in.close();
            } catch (IOException e) {
            }
        }
    }

    private InputStream createStream(String... classNames) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, Charset.forName("UTF-8")), true);
            for (String className : classNames) {
                writer.println(className);
            }
            return new ByteArrayInputStream(out.toByteArray());
        } finally {
            try {
                out.close();
            } catch (IOException e) {
            }
        }
    }

}
