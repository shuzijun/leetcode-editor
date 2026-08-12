package com.shuzijun.leetcode.plugin.window.navigator;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.shuzijun.leetcode.plugin.window.NavigatorPanelAction;

public abstract class NavigatorPanel extends SimpleToolWindowPanel
        implements NavigatorPanelAction, Disposable {

    public NavigatorPanel(boolean vertical) {
        super(vertical);
    }

    public NavigatorPanel(boolean vertical, boolean borderless) {
        super(vertical, borderless);
    }
}
