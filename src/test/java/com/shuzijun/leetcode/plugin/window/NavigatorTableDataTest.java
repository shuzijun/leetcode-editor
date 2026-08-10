package com.shuzijun.leetcode.plugin.window;

import com.shuzijun.leetcode.plugin.model.Config;
import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

public class NavigatorTableDataTest {

    @Test
    public void resolvesConfiguredDifficultyColorsDuringInitialization() {
        Config config = new Config();
        config.setLevelColour("#123456;#654321;#abcdef");

        assertArrayEquals(
                new Color[]{
                        new Color(0x12, 0x34, 0x56),
                        new Color(0x65, 0x43, 0x21),
                        new Color(0xab, 0xcd, 0xef)
                },
                NavigatorTableData.resolveLevelColors(config)
        );
    }

    @Test
    public void ignoresMissingConfigurationDuringInitialization() {
        assertNull(NavigatorTableData.resolveLevelColors(null));
    }
}
