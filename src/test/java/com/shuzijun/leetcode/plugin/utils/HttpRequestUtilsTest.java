package com.shuzijun.leetcode.plugin.utils;

import com.shuzijun.leetcode.plugin.model.HttpRequest;
import org.junit.After;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HttpRequestUtilsTest {

    @After
    public void tearDown() {
        HttpRequestUtils.setTestResponseProvider(null);
    }

    @Test
    public void requestUsesOfflineResponseProvider() {
        AtomicReference<HttpRequest> capturedRequest = new AtomicReference<>();
        HttpRequestUtils.setTestResponseProvider(request -> {
            capturedRequest.set(request);
            HttpResponse response = new HttpResponse();
            response.setUrl(request.getUrl());
            response.setStatusCode(200);
            response.setBody("{\"data\":{\"ok\":true}}");
            return response;
        });

        HttpResponse response = HttpRequest.builderPost("https://leetcode.com/graphql", "application/json")
                .body("{\"operationName\":\"allQuestions\"}")
                .addHeader("Accept", "application/json")
                .cacheParam("test-user")
                .request();

        assertEquals(200, response.getStatusCode());
        assertEquals("{\"data\":{\"ok\":true}}", response.getBody());
        assertNotNull(capturedRequest.get());
        assertEquals("application/json", capturedRequest.get().getContentType());
        assertEquals(true, capturedRequest.get().isCache());
        assertEquals("test-user", capturedRequest.get().getCacheParam());
    }
}
