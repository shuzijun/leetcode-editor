package com.shuzijun.leetcode.plugin.application;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LeetCodeSolutionServiceTest {

    @Test
    public void formatsSdkTagsForExistingSolutionUi() {
        assertEquals(
                "[Array] [Hash Table] ",
                LeetCodeSolutionService.formatTags("Array,Hash Table")
        );
        assertEquals("", LeetCodeSolutionService.formatTags(null));
    }
}
