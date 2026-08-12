package com.shuzijun.leetcode.plugin.adapter.defaults;

import com.shuzijun.leetcode.plugin.spi.ConsoleOutputFormatter;
import org.jetbrains.annotations.NotNull;

public final class DefaultConsoleOutputFormatter implements ConsoleOutputFormatter {

    @Override
    public @NotNull String format(@NotNull String message) {
        return message;
    }
}
