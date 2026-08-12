package com.shuzijun.leetcode.plugin.utils;

import com.shuzijun.lc.http.HttpRequest;
import com.shuzijun.lc.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HttpRequestUtilsTest {

    @Before
    public void setUp() {
        System.clearProperty(DevelopmentTools.SYSTEM_PROPERTY);
    }

    @After
    public void tearDown() {
        HttpRequestUtils.setTestResponseProvider(null);
        System.clearProperty(DevelopmentTools.SYSTEM_PROPERTY);
    }

    @Test
    public void requestUsesOfflineResponseProvider() {
        AtomicReference<String> capturedUrl = new AtomicReference<>();
        HttpRequestUtils.setTestResponseProvider(url -> {
            capturedUrl.set(url);
            return new HttpResponse(
                    200,
                    "{\"data\":{\"ok\":true}}",
                    HttpRequest.builderGet(url).build()
            );
        });

        HttpResponse response = HttpRequestUtils.get("https://leetcode.com/api/company");

        assertEquals(200, response.getStatusCode());
        assertEquals("{\"data\":{\"ok\":true}}", response.getBody());
        assertNotNull(response.getHttpRequest());
        assertEquals("https://leetcode.com/api/company", response.getHttpRequest().getUrl());
        assertEquals("https://leetcode.com/api/company", capturedUrl.get());
    }

    @Test
    public void networkLoggingIsOptIn() {
        assertFalse(HttpRequestUtils.isNetworkLogEnabled());

        System.setProperty(DevelopmentTools.SYSTEM_PROPERTY, "true");

        assertTrue(HttpRequestUtils.isNetworkLogEnabled());
    }

    @Test
    public void sanitizesCredentialsInUrlsAndJsonResponses() {
        String content = "https://leetcode.com/api?token=query-token&slug=two-sum "
                + "{\"LEETCODE_SESSION\":\"session-value\",\"csrfToken\":\"csrf-value\","
                + "\"nested\":{\"password\":\"secret\"},\"data\":{\"title\":\"Two Sum\"}} "
                + "Authorization=Bearer bearer-value";

        String sanitized = HttpRequestUtils.sanitize(content);

        assertFalse(sanitized.contains("query-token"));
        assertFalse(sanitized.contains("session-value"));
        assertFalse(sanitized.contains("csrf-value"));
        assertFalse(sanitized.contains("secret"));
        assertFalse(sanitized.contains("bearer-value"));
        assertTrue(sanitized.contains("slug=two-sum"));
        assertTrue(sanitized.contains("\"title\":\"Two Sum\""));
    }
}
