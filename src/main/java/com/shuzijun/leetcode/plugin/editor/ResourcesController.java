package com.shuzijun.leetcode.plugin.editor;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtilRt;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.io.FileResponses;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author shuzijun
 */
public class ResourcesController extends BaseController {

    private static final Logger LOG = Logger.getInstance(ResourcesController.class);

    private final String controllerPath = "resources";

    @Override
    public String getControllerPath() {
        return controllerPath;
    }

    @Override
    public FullHttpResponse get(@NotNull QueryStringDecoder urlDecoder, @NotNull FullHttpRequest request, @NotNull ChannelHandlerContext context) {
        String resourceName = getResourceName(urlDecoder);
        byte[] data;
        try (InputStream inputStream = PreviewStaticServer.class.getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, Unpooled.EMPTY_BUFFER);
            }
            data = FileUtilRt.loadBytes(inputStream);
        } catch (IOException e) {
            LOG.warn(e);
            return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.EMPTY_BUFFER);
        }

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(data));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, FileResponses.INSTANCE.getContentType(resourceName) + "; charset=utf-8");
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, "max-age=3600, public");
        response.headers().set(HttpHeaderNames.ETAG, Long.toString(LAST_MODIFIED));
        return response;
    }
}
