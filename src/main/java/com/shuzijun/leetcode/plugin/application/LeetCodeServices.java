package com.shuzijun.leetcode.plugin.application;

public final class LeetCodeServices {

    private static final LeetCodeApplicationService APPLICATION = new LeetCodeApplicationService();
    private static final LeetCodeApiService API = new LeetCodeApiService();
    private static final LeetCodeCodeService CODE = new LeetCodeCodeService();
    private static final LeetCodeNoteService NOTE = new LeetCodeNoteService();
    private static final LeetCodeSubmissionService SUBMISSION = new LeetCodeSubmissionService();
    private static final LeetCodeSessionService SESSION = new LeetCodeSessionService();
    private static final LeetCodeFavoriteService FAVORITE = new LeetCodeFavoriteService();
    private static final LeetCodeFindService FIND = new LeetCodeFindService();
    private static final LeetCodeSolutionService SOLUTION = new LeetCodeSolutionService();
    private static final LeetCodeLoginService LOGIN = new LeetCodeLoginService();

    private LeetCodeServices() {
    }

    public static LeetCodeApplicationService application() {
        return APPLICATION;
    }

    public static LeetCodeApiService api() {
        return API;
    }

    public static LeetCodeCodeService code() {
        return CODE;
    }

    public static LeetCodeNoteService note() {
        return NOTE;
    }

    public static LeetCodeSubmissionService submission() {
        return SUBMISSION;
    }

    public static LeetCodeSessionService session() {
        return SESSION;
    }

    public static LeetCodeFavoriteService favorite() {
        return FAVORITE;
    }

    public static LeetCodeFindService find() {
        return FIND;
    }

    public static LeetCodeSolutionService solution() {
        return SOLUTION;
    }

    public static LeetCodeLoginService login() {
        return LOGIN;
    }
}
