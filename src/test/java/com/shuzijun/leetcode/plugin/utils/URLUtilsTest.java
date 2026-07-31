package com.shuzijun.leetcode.plugin.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class URLUtilsTest {

    @Before
    public void setUp() {
        System.setProperty("leetcode.test.base.url", "http://127.0.0.1:8080/");
    }

    @After
    public void tearDown() {
        System.clearProperty("leetcode.test.base.url");
    }

    @Test
    public void buildsAllExternalQuestionAndAccountUrlsFromTheTestEndpoint() {
        assertEquals("http://127.0.0.1:8080", URLUtils.getLeetcodeUrl());
        assertEquals("http://127.0.0.1:8080/problems/two-sum",
                URLUtils.getLeetcodeProblems() + "two-sum");
        assertEquals("http://127.0.0.1:8080/accounts/login/", URLUtils.getLeetcodeLogin());
        assertEquals("http://127.0.0.1:8080/accounts/logout/", URLUtils.getLeetcodeLogout());
        assertEquals("http://127.0.0.1:8080/points/api/", URLUtils.getLeetcodePoints());
        assertEquals("http://127.0.0.1:8080/submissions/detail/123/check/",
                URLUtils.getLeetcodeSubmissions() + "123/check/");
    }
}
