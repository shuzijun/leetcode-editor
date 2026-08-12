package com.shuzijun.leetcode.plugin.editor;

import org.junit.Test;

import java.awt.Color;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LCVPanelTest {

    @Test
    public void classifiesEditorBackgroundBrightnessWithoutInternalSdkApi() {
        assertTrue(LCVPanel.isBright(Color.WHITE));
        assertTrue(LCVPanel.isBright(new Color(128, 128, 128)));
        assertFalse(LCVPanel.isBright(new Color(127, 127, 127)));
        assertFalse(LCVPanel.isBright(Color.BLACK));
    }
}
