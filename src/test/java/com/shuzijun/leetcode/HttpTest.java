package com.shuzijun.leetcode;

import com.shuzijun.lc.errors.LcException;
import com.shuzijun.lc.http.DefaultExecutoHttp;
import okhttp3.Authenticator;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.List;

public class HttpTest {

    @Test
    public void testVerify() throws LcException, IOException {

        DefaultExecutoHttp defaultExecutoHttp = new DefaultExecutoHttp();
        OkHttpClient httpClient = defaultExecutoHttp.getRequestClient().newBuilder().proxySelector(new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8888));
                return Collections.singletonList(proxy);
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {

            }
        }).proxyAuthenticator(new Authenticator() {
            @Nullable
            @Override
            public Request authenticate(@Nullable Route route, @NotNull Response response) throws IOException {
                PasswordAuthentication authentication =new PasswordAuthentication("test", "test12345".toCharArray());
                final String credential = Credentials.basic(authentication.getUserName(),new String(authentication.getPassword()));

                for (Challenge challenge : response.challenges()) {
                    if (challenge.scheme().equalsIgnoreCase("OkHttp-Preemptive")) {
                        return response.request().newBuilder()
                                .header("Proxy-Authorization", credential)
                                .build();
                    }
                }
                return null;
            }
        }).build();
        Response response = httpClient.newCall( (new Request.Builder()).url("https://www.baidu.com").method("get",null).build()).execute();
        System.out.println(response.body());
    }
}
