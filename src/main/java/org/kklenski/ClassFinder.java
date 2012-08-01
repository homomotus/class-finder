package org.kklenski;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

public class ClassFinder {
    
    private InputStream in;

    /**
     * @param in Class names on separate lines
     */
    public ClassFinder(InputStream in) {
        this.in = in;
    }

    public Collection<String> findMatching(String pattern) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        ArrayList<String> result = new ArrayList<String>();
        try {
            String className;
            while ((className = reader.readLine()) != null) {
                if (matches(className, pattern)) {
                    result.add(className);
                }
            }
        } catch (IOException e) {
            //FIXME: what should we do? throw exception, return empty list or current findings
        }
        return result;
    }

    private boolean matches(String className, String pattern) {
        int idx = 0;
        for (int i = 0; i < pattern.length(); i++) {
            char p = pattern.charAt(i);
            switch (p) {
            case '*':
                break;
            case ' ':
                return idx == pattern.length()-1;
            default:
                boolean upperCase = Character.isUpperCase(p);
                break;
            }
        }
        
        return false;
    }

}
