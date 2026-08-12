package com.shuzijun.leetcode.plugin.spi;

import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface ConsolePresenter extends ConsoleWorkbenchProvider {

    void configure(@NotNull TextConsoleBuilder consoleBuilder);

    void info(@NotNull Project project, @NotNull String title, @NotNull String message);

    void warning(@NotNull Project project, @NotNull String title, @NotNull String message);

    void error(@NotNull Project project, @NotNull String title, @NotNull String message);
}
