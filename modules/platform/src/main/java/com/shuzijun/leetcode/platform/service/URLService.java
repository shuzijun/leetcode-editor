package com.shuzijun.leetcode.platform.service;

import com.shuzijun.leetcode.platform.repository.ConfigService;
import org.apache.commons.lang.StringUtils;

public class URLService {
    public static final String leetcode = "leetcode.com";
    public static final String leetcodecn = "leetcode.cn";
    public static final String leetcodecnOld = "leetcode-cn.com";
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
    private static String leetcodeProgress = "/api/progress/all/";
    private static String leetcodeSession = "/session/";
    private static String leetcodeCardInfo = "/problems/api/card-info/";
    private static String leetcodeRandomOneQuestion = "/problems/random-one-question/all";
    private ConfigService configService;

    private URLService(ConfigService configService) {
        this.configService = configService;
    }

    public static URLService getInstance(ConfigService configService) {
        return new URLService(configService);
    }

    public String getLeetcodeHost() {
        String host = configService.getConfig().getUrl();
        if (StringUtils.isBlank(host)) {
            return leetcode;
        }
        return host;
    }

    public boolean equalsHost(String host) {
        String thisHost = getLeetcodeHost();
        if (thisHost.equals(host)) {
            return true;
        } else if (thisHost.equals(leetcodecn) && leetcodecnOld.equals(host)) {
            return true;
        } else {
            return false;
        }
    }

    public String getLeetcodeUrl() {
        return leetcodeUrl + getLeetcodeHost();
    }

    public String getLeetcodeLogin() {
        return getLeetcodeUrl() + leetcodeLogin;
    }

    public String getLeetcodeLogout() {
        return getLeetcodeUrl() + leetcodeLogout;
    }

    public String getLeetcodeAll() {
        return getLeetcodeUrl() + leetcodeAll;
    }

    public String getLeetcodeGraphql() {
        return getLeetcodeUrl() + leetcodeGraphql;
    }

    public String getLeetcodePoints() {
        return getLeetcodeUrl() + leetcodePoints;
    }

    public String getLeetcodeProblems() {
        return getLeetcodeUrl() + leetcodeProblems;
    }

    public String getLeetcodeSubmissions() {
        return getLeetcodeUrl() + leetcodeSubmissions;
    }

    public String getLeetcodeTags() {
        return getLeetcodeUrl() + leetcodeTags;
    }

    public String getLeetcodeFavorites() {
        return getLeetcodeUrl() + leetcodeFavorites;
    }

    public String getLeetcodeVerify() {
        return getLeetcodeUrl() + leetcodeVerify;
    }

    public String getLeetcodeProgress() {
        return getLeetcodeUrl() + leetcodeProgress;
    }

    public String getLeetcodeSession() {
        return getLeetcodeUrl() + leetcodeSession;
    }

    public String getLeetcodeCardInfo() {
        return getLeetcodeUrl() + leetcodeCardInfo;
    }

    public String getDescContent() {
        if (leetcode.equals(getLeetcodeHost()) || configService.getConfig().getEnglishContent()) {
            return "content";
        } else {
            return "translatedContent";
        }
    }

    public boolean isCn() {
        if (leetcode.equals(getLeetcodeHost())) {
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }
    }

    public String getTagName() {
        if (leetcode.equals(getLeetcodeHost()) || configService.getConfig().getEnglishContent()) {
            return "name";
        } else {
            return "translatedName";
        }
    }

    public String getLeetcodeRandomOneQuestion() {
        return getLeetcodeUrl() + leetcodeRandomOneQuestion;
    }
}
