package com.shuzijun.leetcode.plugin.utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UpdateUtilsTest {

    @Test
    public void detectsNewerMajorMinorAndPatchVersions() {
        assertTrue(UpdateUtils.isNewerVersion("v3.8.1", "v4.0.0"));
        assertTrue(UpdateUtils.isNewerVersion("3.8.1", "3.9.0"));
        assertTrue(UpdateUtils.isNewerVersion("3.8.1", "3.8.2"));
        assertTrue(UpdateUtils.isNewerVersion("3.8", "3.8.1"));
    }

    @Test
    public void ignoresEquivalentOlderAndInvalidVersions() {
        assertFalse(UpdateUtils.isNewerVersion("v3.8.1", "v3.8.1"));
        assertFalse(UpdateUtils.isNewerVersion("3.8.1", "3.8.1-beta"));
        assertFalse(UpdateUtils.isNewerVersion("3.8.1", "3.8.0"));
        assertFalse(UpdateUtils.isNewerVersion("3.8.1", "unknown"));
    }
}
