package com.shuzijun.leetcode.plugin.adapter.defaults;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.shuzijun.leetcode.plugin.spi.NavigatorContribution;
import com.shuzijun.leetcode.plugin.window.navigator.SimpleNavigatorPanel;
import org.jetbrains.annotations.NotNull;

public final class DefaultPageNavigatorContribution implements NavigatorContribution {

    @Override
    public @NotNull String getId() {
        return "page";
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public @NotNull SimpleNavigatorPanel createPanel(
            @NotNull ToolWindow toolWindow, @NotNull Project project) {
        return new SimpleNavigatorPanel(toolWindow, project);
    }
}
