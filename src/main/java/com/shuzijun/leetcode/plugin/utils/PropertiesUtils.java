package com.shuzijun.leetcode.plugin.utils;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author shuzijun
 */
public class PropertiesUtils {

    private final static String baseName = "i18n/info";
    private final static  ResourceBundle rb1 = ResourceBundle.getBundle(baseName);

    public static String getInfo(String key, String... params) {
        return new MessageFormat(rb1.getString(key)).format(params);

    }

    public static void main(String[] args) {
        System.out.println(Locale.getDefault());
        System.out.println(ResourceBundle.getBundle(baseName).getString("login.failed"));
        System.out.println(new MessageFormat(ResourceBundle.getBundle(baseName).getString("login.failed")).format(null));
        System.out.println(getInfo("login.failed"));

    }

}
