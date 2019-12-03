package com.shuzijun.leetcode.plugin.utils;

import com.intellij.util.net.HttpConfigurable;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author shuzijun
 */
public class HttpClientUtils {

    private static CloseableHttpClient httpclient = null;
    private static HttpClientContext context = null;

    private static void createHttpClient() {
        if (httpclient == null) {

            context = HttpClientContext.create();
            BasicCookieStore cookieStore = new BasicCookieStore();
            context.setCookieStore(cookieStore);

            RequestConfig.Builder globalConfigBuilder = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD)
                    .setConnectTimeout(30000).setConnectionRequestTimeout(10000)
                    .setSocketTimeout(30000)
                    .setCookieSpec(CookieSpecs.STANDARD_STRICT)
                    .setExpectContinueEnabled(Boolean.TRUE).setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
                    .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC));

            //proxy
            Config config = PersistentConfig.getInstance().getInitConfig();
            HttpConfigurable proxySettings = HttpConfigurable.getInstance();
            CredentialsProvider provider = null;
            if (config != null && config.getProxy() && proxySettings != null && proxySettings.USE_HTTP_PROXY && !proxySettings.PROXY_TYPE_IS_SOCKS) {
                HttpHost proxy = new HttpHost(proxySettings.PROXY_HOST, proxySettings.PROXY_PORT, "http");
                globalConfigBuilder.setProxy(proxy);
                if (proxySettings.PROXY_AUTHENTICATION) {
                    provider = new BasicCredentialsProvider();
                    provider.setCredentials(new AuthScope(proxy), new UsernamePasswordCredentials(proxySettings.getProxyLogin(), proxySettings.getPlainProxyPassword()));
                }

            }


            HttpClientBuilder httpClientBuilder = HttpClients.custom()
                    .setDefaultRequestConfig(globalConfigBuilder.build())
                    .setDefaultCookieStore(cookieStore)
                    .setDefaultHeaders(defaultHeader())
                    .setConnectionManager(getconnectionManager());
            if (provider != null) {
                httpClientBuilder.setDefaultCredentialsProvider(provider);
            }
            httpclient = httpClientBuilder.build();

        }
    }

    public static void setCookie(List<Cookie> cookieList) {
        if (httpclient == null) {
            createHttpClient();
        }
        for (Cookie cookie : cookieList) {
            context.getCookieStore().addCookie(cookie);
        }
    }

    public static CloseableHttpResponse executeGet(HttpGet httpGet) {
        if (httpclient == null) {
            createHttpClient();
        }
        return execute(httpGet);

    }

    public static CloseableHttpResponse executePost(HttpPost httpPost) {
        if (httpclient == null) {
            createHttpClient();
        }
        return execute(httpPost);
    }

    public static CloseableHttpResponse execute(HttpUriRequest httpUriRequest) {

        if (StringUtils.isNotBlank(getToken())) {
            httpUriRequest.setHeader("x-csrftoken", getToken());
        }

        httpUriRequest.setHeader("referer", httpUriRequest.getURI().toString());
        httpUriRequest.setHeader(":path", httpUriRequest.getURI().getPath());

        try {
            CloseableHttpResponse response = httpclient.execute(httpUriRequest, context);
            return response;
        } catch (Exception e) {
            LogUtils.LOG.error("请求出错:", e);
        } finally {
        }

        return null;

    }

    private static List<Header> defaultHeader() {
        ArrayList<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.119 Safari/537.36"));
        headers.add(new BasicHeader(HttpHeaders.ACCEPT, "*/*"));
        //headers.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br"));
        headers.add(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.9"));
        headers.add(new BasicHeader("origin", URLUtils.getLeetcodeUrl()));
        headers.add(new BasicHeader(":authority", URLUtils.getLeetcodeHost()));
        headers.add(new BasicHeader(":scheme", "https"));

        return headers;
    }

    public static boolean isLogin() {
        HttpGet httpget = new HttpGet(URLUtils.getLeetcodePoints());
        CloseableHttpResponse response = HttpClientUtils.executeGet(httpget);
        httpget.abort();
        if (response == null) {
            return Boolean.FALSE;
        }
        if (response.getStatusLine().getStatusCode() == 200) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public static String getToken() {
        if (context == null || context.getCookieStore().getCookies() == null) {
            return null;
        }
        for (Cookie cookie : context.getCookieStore().getCookies()) {
            if ("csrftoken".equals(cookie.getName())) {
                return cookie.getValue();
            }

        }
        return null;
    }

    public static void resetHttpclient() {
        if (httpclient != null) {
            try {
                httpclient.close();
            } catch (IOException e) {
                LogUtils.LOG.error("close error:", e);
            } finally {
                httpclient = null;
                context = null;
            }
        }
    }

    private static PoolingHttpClientConnectionManager getconnectionManager() {

        try {
            X509TrustManager trustManager = new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] xcs, String str) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] xcs, String str) {
                }
            };

            SSLContext ctx = SSLContext.getInstance(SSLConnectionSocketFactory.TLS);
            ctx.init(null, new TrustManager[]{trustManager}, null);
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(ctx, NoopHostnameVerifier.INSTANCE);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", socketFactory).build();
            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            connectionManager.setDefaultMaxPerRoute(200);
            connectionManager.setMaxTotal(400);
            return connectionManager;

        } catch (Exception e) {
            LogUtils.LOG.error("创建PoolingHttpClientConnectionManager失败", e);
        }
        return new PoolingHttpClientConnectionManager();
    }
}
