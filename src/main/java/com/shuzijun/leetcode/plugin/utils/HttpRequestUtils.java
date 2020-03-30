package com.shuzijun.leetcode.plugin.utils;

import com.shuzijun.leetcode.plugin.utils.io.HttpRequests;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.Map;

/**
 * @author shuzijun
 */
public class HttpRequestUtils {

    private static final CookieManager cookieManager = new CookieManager();

    static {
        CookieHandler.setDefault(cookieManager);
    }

    public static HttpResponse executeGet(HttpRequest httpRequest) {

        HttpResponse httpResponse = new HttpResponse();
        try {
            HttpRequests.request(httpRequest.getUrl())
                    .throwStatusCodeException(false)
                    .tuner(new HttpRequestTuner(httpRequest))
                    .connect(new HttpResponseProcessor(httpRequest, httpResponse));

        } catch (IOException e) {
            LogUtils.LOG.error("HttpRequestUtils request error:", e);
            httpResponse.setStatusCode(-1);
        }
        return httpResponse;
    }

    public static HttpResponse executePost(HttpRequest httpRequest) {
        HttpResponse httpResponse = new HttpResponse();
        try {
            HttpRequests.post(httpRequest.getUrl(), httpRequest.getContentType())
                    .throwStatusCodeException(false)
                    .tuner(new HttpRequestTuner(httpRequest))
                    .connect(new HttpResponseProcessor(httpRequest, httpResponse));
        } catch (IOException e) {
            LogUtils.LOG.error("HttpRequestUtils request error:", e);
            httpResponse.setStatusCode(-1);
        }
        return httpResponse;
    }

    public static String getToken() {
        if (cookieManager.getCookieStore().getCookies() == null) {
            return null;
        }
        for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
            if ("csrftoken".equals(cookie.getName())) {
                return cookie.getValue();
            }

        }
        return null;
    }

    public static boolean isLogin() {
        HttpRequest request = HttpRequest.get(URLUtils.getLeetcodePoints());
        HttpResponse response = executeGet(request);
        if (response.getStatusCode() == 200) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public static void setCookie(List<HttpCookie> cookieList) {

        cookieManager.getCookieStore().removeAll();
        for (HttpCookie cookie : cookieList) {
            cookieManager.getCookieStore().add(null, cookie);
        }
    }

    public static void resetHttpclient() {
        cookieManager.getCookieStore().removeAll();
    }


    private static void defaultHeader(HttpRequest httpRequest) {
        Map<String, String> header = httpRequest.getHeader();
        header.putIfAbsent(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.119 Safari/537.36");
        header.putIfAbsent(HttpHeaders.ACCEPT, "*/*");
        //header.putIfAbsent(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br");
        header.putIfAbsent(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.9");
        header.putIfAbsent("origin", URLUtils.getLeetcodeUrl());
        //header.putIfAbsent(":authority", URLUtils.getLeetcodeHost());
        //header.putIfAbsent(":scheme", "https");
    }

    private static class HttpRequestTuner implements HttpRequests.ConnectionTuner {

        private HttpRequest httpRequest;

        public HttpRequestTuner(HttpRequest httpRequest) {
            this.httpRequest = httpRequest;
        }

        @Override
        public void tune(@NotNull URLConnection urlConnection) throws IOException {

            if (StringUtils.isNotBlank(getToken())) {
                urlConnection.addRequestProperty("x-csrftoken", getToken());
            }
            urlConnection.addRequestProperty("referer", urlConnection.getURL().toString());
            //urlConnection.addRequestProperty(":path", urlConnection.getURL().getPath());

            defaultHeader(httpRequest);
            httpRequest.getHeader().forEach((k, v) -> urlConnection.addRequestProperty(k, v));
        }
    }


    private static class HttpResponseProcessor implements HttpRequests.RequestProcessor<HttpResponse> {

        private final HttpRequest httpRequest;
        private final HttpResponse httpResponse;

        public HttpResponseProcessor(HttpRequest httpRequest, HttpResponse httpResponse) {
            this.httpRequest = httpRequest;
            this.httpResponse = httpResponse;
        }

        @Override
        public HttpResponse process(@NotNull HttpRequests.Request request) throws IOException {

            if (StringUtils.isNoneBlank(httpRequest.getBody())) {
                request.write(httpRequest.getBody());
            }

            URLConnection urlConnection = request.getConnection();

            if (!(urlConnection instanceof HttpURLConnection)) {
                httpResponse.setStatusCode(-1);
                return httpResponse;
            } else {
                httpResponse.setStatusCode(((HttpURLConnection) urlConnection).getResponseCode());
            }

            try {
                httpResponse.setBody(request.readString());
            } catch (IOException ignore) {
            }
            return httpResponse;
        }
    }

}
