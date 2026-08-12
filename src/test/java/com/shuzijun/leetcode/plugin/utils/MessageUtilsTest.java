package com.shuzijun.leetcode.plugin.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MessageUtilsTest {

    @Test
    public void formatsLegacySeverityAsAnsiColorSequence() {
        assertEquals(
                "\033[31mcompile failed\033[0m",
                MessageUtils.format("compile failed", "E")
        );
    }

    @Test
    public void formatsNullBodyAsEmptyAnsiColorSequence() {
        assertEquals(
                "\033[31m\033[0m",
                MessageUtils.format(null, "E")
        );
    }

    @Test
    public void highlightsOnlyDifferentOutputWithAnsiRed() {
        String formatted = MessageUtils.formatDiff("answer", "ansXer");

        assertEquals("ans\033[31mX\033[0mer", formatted);
        assertTrue(formatted.endsWith("er"));
    }
}
