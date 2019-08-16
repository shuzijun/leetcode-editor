package com.shuzijun.leetcode.plugin.utils;

import org.apache.commons.lang.StringUtils;

/**
 * @author shuzijun
 */
public class VelocityTool extends StringUtils {

    private static String[] numsAry = {"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"};

    public static String camelCaseName(String underscoreName) {

        if (isNotBlank(underscoreName)) {
            underscoreName = underscoreName.replace(" ", "");
            StringBuilder result = new StringBuilder();
            if (isNumeric(underscoreName.substring(0, 1))) {
                underscoreName = numsAry[Integer.valueOf(underscoreName.substring(0, 1))] + "-" + underscoreName.substring(1);
            }
            boolean first = true;
            boolean flag = false;
            for (int i = 0; i < underscoreName.length(); i++) {
                char ch = underscoreName.charAt(i);
                if ('_' == ch || '-' == ch) {
                    flag = true;
                } else {
                    if (flag || first) {
                        result.append(Character.toUpperCase(ch));
                        flag = false;
                        first = false;
                    } else {
                        result.append(ch);
                    }
                }
            }
            return result.toString();
        } else {
            return underscoreName;
        }
    }
}
