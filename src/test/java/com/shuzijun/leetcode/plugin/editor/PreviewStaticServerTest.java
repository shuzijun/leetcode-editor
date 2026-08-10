package com.shuzijun.leetcode.plugin.editor;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PreviewStaticServerTest {

    @Test
    public void keepsRoutesProductNeutralUntilRequestTime() {
        assertFalse(PreviewStaticServer.route.isEmpty());
        assertTrue(PreviewStaticServer.route.containsKey("resources"));
        assertFalse(PreviewStaticServer.route.containsKey("/leetcode/resources"));
        assertFalse(PreviewStaticServer.route.containsKey("/leetcode-pro/resources"));
    }
}
