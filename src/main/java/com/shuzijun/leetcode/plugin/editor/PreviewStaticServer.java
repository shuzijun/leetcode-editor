package com.shuzijun.leetcode.plugin.editor;

import com.intellij.openapi.diagnostic.Logger;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.ide.HttpRequestHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shuzijun
 */
public class PreviewStaticServer extends HttpRequestHandler {

    private static final Logger LOG = Logger.getInstance(PreviewStaticServer.class);

    public static final String PREFIX = "/3e41525e-e74a-4590-ba1d-982b8f638478/";

    public static final Map<String, BaseController> route = new HashMap<>();
    static {
        new ResourcesController().addRoute(route);
    }

    public static PreviewStaticServer getInstance() {
        return HttpRequestHandler.Companion.getEP_NAME().findExtension(PreviewStaticServer.class);
    }

    @Override
    public boolean isAccessible(@NotNull HttpRequest request) {
        return true;
    }

    @Override
    public boolean isSupported(@NotNull FullHttpRequest request) {
        return (request.method() == HttpMethod.GET || request.method() == HttpMethod.HEAD || request.method() == HttpMethod.POST) && request.uri().startsWith(PREFIX);
    }

    @Override
    public boolean process(@NotNull QueryStringDecoder urlDecoder,
                           @NotNull FullHttpRequest request,
                           @NotNull ChannelHandlerContext context) throws IOException {
        final String path = urlDecoder.path();
        if (!path.startsWith(PREFIX)) {
            throw new IllegalStateException("prefix should have been checked by #isSupported");
        }

        for (String controllerPath : route.keySet()) {
            if (path.startsWith(controllerPath)) {
                route.get(controllerPath).process(urlDecoder, request, context);
                return true;
            }
        }
        return false;
    }


}