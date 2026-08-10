package com.shuzijun.leetcode.plugin.adapter.defaults;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.shuzijun.leetcode.plugin.spi.NavigatorContribution;
import com.shuzijun.leetcode.plugin.window.navigator.TopNavigatorPanel;
import org.jetbrains.annotations.NotNull;

public final class DefaultCodeTopNavigatorContribution implements NavigatorContribution {

    @Override
    public @NotNull String getId() {
        return "codeTop";
    }

    @Override
    public int getOrder() {
        return 30;
    }

    @Override
    public @NotNull SimpleToolWindowPanel createPanel(
            @NotNull ToolWindow toolWindow, @NotNull Project project) {
        return new TopNavigatorPanel(toolWindow, project);
    }
}
