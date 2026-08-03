package com.shuzijun.leetcode.plugin.utils;

import com.shuzijun.leetcode.plugin.model.Config;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.junit.Test;

import java.net.HttpCookie;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CookieUtilsTest {

    @Test
    public void roundTripsStoredCookiesForCookieLogin() {
        BasicClientCookie session = new BasicClientCookie("LEETCODE_SESSION", "session-value");
        session.setDomain("leetcode.cn");
        session.setPath("/");
        BasicClientCookie csrf = new BasicClientCookie("csrftoken", "csrf-value");
        csrf.setDomain("leetcode.cn");
        csrf.setPath("/");

        String storedCookies = CookieUtils.toJSONString(Arrays.asList(session, csrf));
        List<HttpCookie> restoredCookies = CookieUtils.toHttpCookie(storedCookies);

        assertEquals(2, restoredCookies.size());
        assertEquals("LEETCODE_SESSION", restoredCookies.get(0).getName());
        assertEquals("session-value", restoredCookies.get(0).getValue());
        assertEquals("leetcode.cn", restoredCookies.get(0).getDomain());
        assertEquals("/", restoredCookies.get(0).getPath());
        assertEquals(7 * 24 * 60, restoredCookies.get(0).getMaxAge());
        assertEquals("csrftoken", restoredCookies.get(1).getName());
    }

    @Test
    public void storesBrowserCookiesUnderTheConfiguredAccountKey() {
        HttpCookie session = new HttpCookie("LEETCODE_SESSION", "session-value");
        session.setDomain("leetcode.cn");
        session.setPath("/");

        Config config = new Config();
        config.setUrl("leetcode.cn");
        config.setLoginName("tester");
        config.addCookie(config.getUrl() + config.getLoginName(),
                CookieUtils.httpCookieToJSONString(Arrays.asList(session)));

        String storedCookies = config.getCookie("leetcode.cntester");
        assertNotNull(storedCookies);
        List<HttpCookie> restoredCookies = CookieUtils.toHttpCookie(storedCookies);
        assertEquals(1, restoredCookies.size());
        assertEquals("session-value", restoredCookies.get(0).getValue());
    }
}
