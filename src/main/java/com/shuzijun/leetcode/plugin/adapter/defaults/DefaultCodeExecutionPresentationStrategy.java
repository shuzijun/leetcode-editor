package com.shuzijun.leetcode.plugin.adapter.defaults;

import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.spi.CodeExecutionPresentationStrategy;
import org.jetbrains.annotations.NotNull;

public final class DefaultCodeExecutionPresentationStrategy
        implements CodeExecutionPresentationStrategy {

    @Override
    public @NotNull String failurePrefix(
            @NotNull Question question,
            @NotNull CodeTypeEnum codeType,
            @NotNull String code
    ) {
        return "";
    }
}
