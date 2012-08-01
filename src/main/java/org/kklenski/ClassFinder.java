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
    
    private MatchMode mode;
    private char match;
    //TODO: multiIndex
    private int matchIdx;

	private int multiIdx = -1;

    /**
     * @param in Class names on separate lines
     */
    public ClassFinder(InputStream in) {
        this.in = in;
    }

    public Collection<String> findMatching(String pattern) {
		// we should expect unicode as class names can consist of unicode chars,
		// see: http://stackoverflow.com/questions/1422655/java-unicode-variable-names
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8"))); 
        ArrayList<String> result = new ArrayList<String>();
        try {
            String className;
            while ((className = reader.readLine()) != null) {
                if (matches(getSimpleName(className), pattern)) {
                    result.add(className);
                }
            }
        } catch (IOException e) {
            //FIXME: what should we do? throw exception, return empty list or current findings
        }
        
        Collections.sort(result, new Comparator<String>() {
			@Override
			public int compare(String className1, String className2) {
				return getSimpleName(className1).compareTo(getSimpleName(className2));
			}
		});
        
        return result;
    }
    
    static String getSimpleName(String className) {
    	//FIXME: test class with no package
    	return className.substring(className.lastIndexOf('.')+1);
	}

	private enum MatchMode {
    	UPPER,
    	LOWER,
    	ZERO_OR_MORE,
    	END
    }

	private void updateMode(String pattern) {
		match = pattern.charAt(matchIdx);
        switch (match) {
        case ' ':
            mode = MatchMode.END;
            break;
        case '*':
        	match = pattern.charAt(++matchIdx);
        	multiIdx = matchIdx;
        default:
            mode = Character.isUpperCase(match) ? MatchMode.UPPER : MatchMode.LOWER;
            break;
        }
        match = Character.toUpperCase(match);
	}

	//FIXME: empty pattern case test
	//FIXME: Character."supplementary characters"
    private boolean matches(String className, String pattern) {
    	matchIdx = 0;
        updateMode(pattern);
        
        for (int i = 0; i < className.length(); i++) {
        	char c = Character.toUpperCase(className.charAt(i));
        	switch (mode) {
        	case LOWER:
        		if (c != match) {
        			if (multiIdx != -1) {
        				matchIdx = multiIdx;
        				updateMode(pattern);
        				multiIdx = -1;
        			} else {
        				return false;
        			}
        		}
			case UPPER:
				if (c == match) {
					if (++matchIdx < pattern.length()) {
						updateMode(pattern);
						break;
					} else {
						return true;
					}
				}
				break;
			case END:
				return false;
        	}
        }
        return mode == MatchMode.END;
    }

}
