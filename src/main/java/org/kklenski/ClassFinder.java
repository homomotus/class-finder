package org.kklenski;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class ClassFinder {
    
	private InputStream in;
    
    private int patternIdx;
	private int wildcardIdx = -1;
	
	boolean[] matchMask;

    /**
     * @param in Stream containing class names on separate lines in <code>UTF-8</code> encoding. 
     */
    public ClassFinder(InputStream in) {
        this.in = in;
    }
    
    public Collection<String> findMatching(String pattern) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8"))); 
        ArrayList<String> result = new ArrayList<String>();
        
        try {
        	String nPattern = normalizePattern(pattern);
            String className;
            while ((className = reader.readLine()) != null) {
            	
                if (matches(getSimpleName(className), nPattern)) {
                    result.add(className);
                    
/*                  System.out.println(getSimpleName(className)+" ("+pattern+")");
                    printMask(matchMask);
                    System.out.println("===");
*/
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        //XXX: possible performance hit, redundant getSimpleName() calls
        Collections.sort(result, new Comparator<String>() {
			@Override
			public int compare(String className1, String className2) {
				return getSimpleName(className1).compareTo(getSimpleName(className2));
			}
		});
        
        return result;
    }
    
    static String getSimpleName(String className) {
    	return className.substring(className.lastIndexOf('.')+1);
	}

    private boolean matches(String className, String pattern) {
    	patternIdx = 0;
    	matchMask = new boolean[className.length()+1];
    	className = className.toLowerCase()+' ';
    	
        char p = getNextChar(pattern);
        
        for (int i = 0; i < className.length(); i++) {
        	
        	if (p == '*') {
        		return true;
        	}

        	char c = className.charAt(i);
        	boolean match = (c == p) || (p == ' ' && c == '_');
        	
       		if (!match) {
       			if (wildcardIdx != -1) {
       				patternIdx = wildcardIdx;
        			p = getNextChar(pattern);
        		} else {
        			return false;
        		}
       		} else {
       			matchMask[i] = true;
				if (++patternIdx < pattern.length()) {
					p = getNextChar(pattern);
					continue;
				} else {
					return true;
				}
       		}
        }
        return false;
	}

    private char getNextChar(String pattern) {
    	char p;
    	while ((p = pattern.charAt(patternIdx)) == '*') {
    		wildcardIdx = ++patternIdx;
    		if (patternIdx == pattern.length()) {
    			break; 
    		}
    	}
        return p;
	}
    
    static String normalizePattern(String pattern) {
    	StringBuilder result = new StringBuilder();
    	for (int i = 0; i < pattern.length(); i++) {
			char c = pattern.charAt(i);
			
			//add asterisk before upper case letters except first letter
			if (Character.isUpperCase(c) && (i != 0)) {
				if (!result.toString().endsWith("*")) {
					result.append('*');
				}
			}
			result.append(Character.toLowerCase(c));
		}
    	
		return result.toString();
	}

	private void printMask(boolean[] matchMask) {
    	for (boolean b : matchMask) {
    		System.out.print(b ? 'X' : ' ');
		}
    	System.out.println();
	}

}
