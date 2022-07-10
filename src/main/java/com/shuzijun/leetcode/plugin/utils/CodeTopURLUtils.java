package com.shuzijun.leetcode.plugin.utils;

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
        return codetopUrl + codetop + tags;
    }

    public static String getCompanies(){
        return codetopUrl + codetop + companies;
    }

    public static String getQuestions() {
        return codetopUrl + codetop + questions;
    }
}
