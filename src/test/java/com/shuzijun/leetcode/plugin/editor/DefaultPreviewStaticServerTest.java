package com.shuzijun.leetcode.plugin.editor;

import com.shuzijun.leetcode.plugin.product.DefaultProductProfile;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefaultPreviewStaticServerTest {

    @Test
    public void usesTheDefaultPreviewPrefix() {
        assertEquals(
                "/leetcode/",
                PreviewStaticServer.prefix(new DefaultProductProfile())
        );
    }
}
