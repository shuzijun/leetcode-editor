package com.shuzijun.leetcode.plugin.application;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultQuestionCatalogProviderTest {

    @Test
    public void supportsTheSharedLeetCodeSourceIdCaseInsensitively() {
        DefaultQuestionCatalogProvider provider = new DefaultQuestionCatalogProvider();

        assertEquals("leetcode", provider.getId());
        assertTrue(provider.supports("leetcode"));
        assertTrue(provider.supports("LeetCode"));
        assertFalse(provider.supports("codetop"));
    }
}
