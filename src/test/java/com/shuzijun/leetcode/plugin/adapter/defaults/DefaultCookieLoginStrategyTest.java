package com.shuzijun.leetcode.plugin.adapter.defaults;

import org.junit.Test;

import java.net.HttpCookie;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DefaultCookieLoginStrategyTest {

    @Test
    public void parsesCookieValuesContainingEqualsSigns() {
        List<HttpCookie> cookies = DefaultCookieLoginStrategy.parseCookies(
                "csrftoken=token; LEETCODE_SESSION=session=value; invalid"
        );

        assertEquals(2, cookies.size());
        assertEquals("csrftoken", cookies.get(0).getName());
        assertEquals("token", cookies.get(0).getValue());
        assertEquals("LEETCODE_SESSION", cookies.get(1).getName());
        assertEquals("session=value", cookies.get(1).getValue());
        assertEquals("/", cookies.get(1).getPath());
    }
}
