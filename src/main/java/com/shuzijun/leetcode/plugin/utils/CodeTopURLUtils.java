package com.shuzijun.leetcode.plugin.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * @author shuzijun
 */
public class CodeTopURLUtils {

    public static final String codetop = "codetop.cc" ;

    private static String codetopUrl = "https://" ;

    private static String tags = "/api/tags/" ;
    private static String companies = "/api/companies/" ;

    private static String questions = "/api/questions/";

    public static String getTags() {
        return getCodeTopUrl() + tags;
    }

    public static String getCompanies(){
        return getCodeTopUrl() + companies;
    }

    public static String getQuestions() {
        return getCodeTopUrl() + questions;
    }

    private static String getCodeTopUrl() {
        String testBaseUrl = System.getProperty("leetcode.test.base.url");
        if (StringUtils.isNotBlank(testBaseUrl)) {
            return StringUtils.removeEnd(testBaseUrl, "/");
        }
        return codetopUrl + codetop;
    }
}
