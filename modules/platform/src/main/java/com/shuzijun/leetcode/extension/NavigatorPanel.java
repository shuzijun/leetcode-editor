package com.shuzijun.leetcode.extension;

import com.intellij.openapi.ui.SimpleToolWindowPanel;

public abstract class NavigatorPanel extends SimpleToolWindowPanel implements NavigatorPanelAction {
    public NavigatorPanel(boolean vertical) {
        super(vertical);
    }

    public NavigatorPanel(boolean vertical, boolean borderless) {
        super(vertical, borderless);
    }
}
