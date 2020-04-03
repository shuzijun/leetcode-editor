package com.shuzijun.leetcode.plugin.utils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shuzijun
 */
public class HttpRequest {

    private String url;

    private String body;
    /**
     * POST
     */
    private String contentType;

    private Map<String, String> Header = new HashMap<>();

    public static HttpRequest get(String url) {
        return new HttpRequest(url, null);
    }

    public static HttpRequest post(String url, String contentType) {
        return new HttpRequest(url, contentType);
    }

    public static HttpRequest put(String url, String contentType) {
        return new HttpRequest(url, contentType);
    }

    private HttpRequest(String url, String contentType) {
        this.url = url;
        this.contentType = contentType;
    }

    public String getUrl() {
        return url;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getContentType() {
        return contentType;
    }

    public void addHeader(String name, String value) {
        Header.put(name, value);
    }

    public Map<String, String> getHeader() {
        return Header;
    }

    public void addParam(String key, String value) throws UnsupportedEncodingException {
        if (body == null || body.isEmpty()) {
            body = key + "=" + value;
        } else {
            body = body + "&" + key + "=" +value;
        }
    }
}