package com.shuzijun.leetcode.plugin.utils;

import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import org.apache.commons.lang.StringUtils;

/**
 * @author shuzijun
 */
public class URLUtils {

    public static final String leetcode = "leetcode.com";
    public static final String leetcodecn = "leetcode-cn.com";

    private static String leetcodeUrl = "https://";
    private static String leetcodeLogin = "/accounts/login/";
    private static String leetcodeLogout = "/accounts/logout/";
    private static String leetcodeAll = "/api/problems/all/";
    private static String leetcodeGraphql = "/graphql";
    private static String leetcodePoints = "/points/api/";
    private static String leetcodeProblems = "/problems/";
    private static String leetcodeSubmissions = "/submissions/detail/";
    private static String leetcodeTags = "/problems/api/tags/";
    private static String leetcodeFavorites = "/problems/api/favorites/";
    private static String leetcodeVerify = "/problemset/all/";

    public static String getLeetcodeHost() {
        String host = PersistentConfig.getInstance().getConfig().getUrl();
        if (StringUtils.isBlank(host)) {
            return leetcode;
        }
        return host;
    }

    public static String getLeetcodeUrl() {
        return leetcodeUrl + getLeetcodeHost();
    }

    public static String getLeetcodeLogin() {
        return getLeetcodeUrl() + leetcodeLogin;
    }

    public static String getLeetcodeLogout() {
        return getLeetcodeUrl() + leetcodeLogout;
    }

    public static String getLeetcodeAll() {
        return getLeetcodeUrl() + leetcodeAll;
    }

    public static String getLeetcodeGraphql() {
        return getLeetcodeUrl() + leetcodeGraphql;
    }

    public static String getLeetcodePoints() {
        return getLeetcodeUrl() + leetcodePoints;
    }

    public static String getLeetcodeProblems() {
        return getLeetcodeUrl() + leetcodeProblems;
    }

    public static String getLeetcodeSubmissions() {
        return getLeetcodeUrl() + leetcodeSubmissions;
    }

    public static String getLeetcodeTags() {
        return getLeetcodeUrl() + leetcodeTags;
    }

    public static String getLeetcodeFavorites() {
        return getLeetcodeUrl() + leetcodeFavorites;
    }

    public static String getLeetcodeVerify() {
        return getLeetcodeUrl() + leetcodeVerify;
    }

    public static String getDescContent() {
        if ("leetcode.com".equals(getLeetcodeHost()) && !PersistentConfig.getInstance().getConfig().getEnglishContent()) {
            return "content";
        } else {
            return "translatedContent";
        }
    }

    public static boolean getQuestionTranslation() {
        if ("leetcode.com".equals(getLeetcodeHost())) {
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }
    }

    public static String getTagName() {
        if ("leetcode.com".equals(getLeetcodeHost())) {
            return "name";
        } else {
            return "translatedName";
        }
    }

}
