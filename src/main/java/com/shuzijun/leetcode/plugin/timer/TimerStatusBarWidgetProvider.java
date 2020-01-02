package com.shuzijun.leetcode.plugin.timer;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * @author shuzijun
 */
public class TimerStatusBarWidgetProvider implements StatusBarWidgetProvider {
    @Nullable
    @Override
    public StatusBarWidget getWidget(@NotNull Project project) {
        return new TimerBarWidget(project);
    }
}
