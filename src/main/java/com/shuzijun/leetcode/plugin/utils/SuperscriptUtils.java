package com.shuzijun.leetcode.plugin.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shuzijun
 */
public class SuperscriptUtils {

    public static Map<Character, String> superscriptMap = new HashMap<>();

    static {
        superscriptMap.put('0', "\u2070");
        superscriptMap.put('1', "\u00B9");
        superscriptMap.put('2', "\u00B2");
        superscriptMap.put('3', "\u00B3");
        superscriptMap.put('4', "\u2074");
        superscriptMap.put('5', "\u2075");
        superscriptMap.put('6', "\u2076");
        superscriptMap.put('7', "\u2077");
        superscriptMap.put('8', "\u2078");
        superscriptMap.put('9', "\u2079");
        superscriptMap.put('+', "\u207A");
        superscriptMap.put('-', "\u207B");
        superscriptMap.put('=', "\u207C");
        superscriptMap.put('(', "\u207D");
        superscriptMap.put(')', "\u207E");
        superscriptMap.put('.', "\u02D9");
        superscriptMap.put('/', "/");
        superscriptMap.put('a', "\u1D43");
        superscriptMap.put('b', "\u1D47");
        superscriptMap.put('c', "\u1D9C");
        superscriptMap.put('d', "\u1D48");
        superscriptMap.put('e', "\u1D49");
        superscriptMap.put('g', "\u1D4D");
        superscriptMap.put('h', "\u02B0");
        superscriptMap.put('i', "\u2071");
        superscriptMap.put('j', "\u02B2");
        superscriptMap.put('k', "\u1D4F");
        superscriptMap.put('l', "\u02E1");
        superscriptMap.put('m', "\u1D50");
        superscriptMap.put('n', "\u207F");
        superscriptMap.put('o', "\u1D52");
        superscriptMap.put('p', "\u1D56");
        superscriptMap.put('r', "\u02B3");
        superscriptMap.put('s', "\u02E2");
        superscriptMap.put('t', "\u1D57");
        superscriptMap.put('u', "\u1D58");
        superscriptMap.put('v', "\u1D5B");
        superscriptMap.put('w', "\u02B7");
        superscriptMap.put('x', "\u02E3");
        superscriptMap.put('y', "\u02B8");
        superscriptMap.put('z', "\u1646");

    }

    public static String getSup(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        StringBuilder supSb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            Character c = str.charAt(i);
            if (superscriptMap.containsKey(str.charAt(i))) {
                supSb.append(superscriptMap.get(c));
            } else {
                supSb.append(c);
            }

        }
        return supSb.toString();
    }

}
