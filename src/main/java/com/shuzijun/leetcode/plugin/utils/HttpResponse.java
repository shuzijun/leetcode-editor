package com.shuzijun.leetcode.plugin.utils;

/**
 * @author shuzijun
 */
public class HttpResponse {
    private int statusCode;

    private String body;

    private String url;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
