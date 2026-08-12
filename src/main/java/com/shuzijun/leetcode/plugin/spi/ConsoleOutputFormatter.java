package com.shuzijun.leetcode.plugin.spi;

import org.jetbrains.annotations.NotNull;

public interface ConsoleOutputFormatter {

    @NotNull String format(@NotNull String message);
}
