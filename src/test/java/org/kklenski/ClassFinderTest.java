package org.kklenski;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.junit.Test;

public class ClassFinderTest {
    
    @Test
    public void testBasicCases() {
        String[] classNames = new String[] {"c.d.FooBar", "a.b.FooBarBaz"};
        
        assertArrayEquals(classNames, findMatching(classNames, "FB"));
        assertArrayEquals(classNames, findMatching(classNames, "FoBa"));
        assertArrayEquals(classNames, findMatching(classNames, "FBar"));
    }
    
    //FIXME: add negative cases
    
    @Test
    public void testSpace() throws Exception {
        String[] classNames = new String[] {"a.b.FooBarBaz", "c.d.FooBar"};
        String[] expected = new String[] {"c.d.FooBar"};
        assertArrayEquals(expected, findMatching(classNames, "FBar "));
        
        //FIXME: test space in other pattern places
    }
    
    @Test
    public void testWildcard() throws Exception {
        String[] classNames = new String[] {"a.b.FooBarBaz", "c.d.FooBar"};
        String[] expected = new String[] {"a.b.FooBarBaz"};
        assertArrayEquals(expected, findMatching(classNames, "F*Baz"));
        assertArrayEquals(expected, findMatching(classNames, "*BB"));
        
        //FIXME: test plain wildcard / wildcard and space combinations
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
    	assertEquals("", ClassFinder.getSimpleName("a.")); //FIXME: is it valid case?
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
            PrintWriter writer = new PrintWriter(out, true);
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
