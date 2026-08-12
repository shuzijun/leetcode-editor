package com.shuzijun.leetcode.plugin.adapter.defaults;

import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.spi.ConsolePresenter;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import org.jetbrains.annotations.NotNull;

public final class DefaultConsolePresenter implements ConsolePresenter {

    @Override
    public void configure(@NotNull TextConsoleBuilder consoleBuilder) {
    }

    @Override
    public void info(@NotNull Project project, @NotNull String title, @NotNull String message) {
        MessageUtils.getInstance(project).showConsoleInfo(title, message);
    }

    @Override
    public void warning(@NotNull Project project, @NotNull String title, @NotNull String message) {
        MessageUtils.getInstance(project).showConsoleWarning(title, message);
    }

    @Override
    public void error(@NotNull Project project, @NotNull String title, @NotNull String message) {
        MessageUtils.getInstance(project).showConsoleError(title, message);
    }
}
