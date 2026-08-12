package com.shuzijun.leetcode.plugin.window.navigator;

import org.junit.Test;

import java.text.NumberFormat;
import java.util.Locale;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SimpleNavigatorTableTest {

    @Test
    public void formatsMissingPercentageAsEmptyText() {
        NumberFormat numberFormat = NumberFormat.getPercentInstance(Locale.US);

        assertEquals("", SimpleNavigatorTable.formatPercentage(numberFormat, null));
        assertEquals("25%", SimpleNavigatorTable.formatPercentage(numberFormat, 0.25d));
    }

    @Test
    public void showsFrequencyOnlyForChinaSite() {
        assertTrue(SimpleNavigatorTable.showFrequencyColumn("leetcode.cn"));
        assertFalse(SimpleNavigatorTable.showFrequencyColumn("leetcode.com"));
    }
}
