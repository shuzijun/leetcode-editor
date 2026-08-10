package com.shuzijun.leetcode.plugin.spi;

import org.jetbrains.annotations.NotNull;

public interface OrderedContribution {

    @NotNull String getId();

    default int getOrder() {
        return 0;
    }
}
