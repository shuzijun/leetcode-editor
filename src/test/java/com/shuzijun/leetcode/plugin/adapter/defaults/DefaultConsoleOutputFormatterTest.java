package com.shuzijun.leetcode.plugin.adapter.defaults;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefaultConsoleOutputFormatterTest {

    @Test
    public void preservesThePublicConsoleOutput() {
        String message = "Your input:\n[1,2]\nOutput:\n3";

        assertEquals(message, new DefaultConsoleOutputFormatter().format(message));
    }
}
