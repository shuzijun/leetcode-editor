package com.shuzijun.leetcode.plugin.spi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ConsoleWorkbench {

    enum Level {
        INFO,
        WARNING,
        ERROR
    }

    void append(@NotNull Level level, @NotNull String title, @NotNull String body);

    void appendExecutionResult(
            @NotNull String title,
            @NotNull String input,
            @NotNull String expected,
            @NotNull String actual,
            @NotNull String standardOutput,
            boolean failed,
            @Nullable Runnable openCode
    );
}
