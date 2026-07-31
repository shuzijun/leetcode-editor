package com.shuzijun.leetcode.plugin.model;

import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.assertArrayEquals;

public class ConfigLevelColourTest {

    @Test
    public void usesTheRefinedDefaultDifficultyPalette() {
        Config config = new Config();

        assertArrayEquals(
                new Color[]{
                        new Color(106, 171, 115),
                        new Color(217, 164, 65),
                        new Color(199, 84, 80)
                },
                config.getFormatLevelColour()
        );
    }

    @Test
    public void migratesTheLegacyDefaultPaletteWithoutOverwritingCustomColors() {
        Config config = new Config();
        config.setLevelColour(Constant.LEGACY_LEVEL_COLOUR);

        assertArrayEquals(
                new Color[]{
                        new Color(106, 171, 115),
                        new Color(217, 164, 65),
                        new Color(199, 84, 80)
                },
                config.getFormatLevelColour()
        );

        config.setLevelColour("#123456;#654321;#abcdef");

        assertArrayEquals(
                new Color[]{
                        new Color(0x12, 0x34, 0x56),
                        new Color(0x65, 0x43, 0x21),
                        new Color(0xab, 0xcd, 0xef)
                },
                config.getFormatLevelColour()
        );
    }
}
