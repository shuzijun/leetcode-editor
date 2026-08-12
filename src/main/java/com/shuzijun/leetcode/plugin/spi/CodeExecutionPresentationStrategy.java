package com.shuzijun.leetcode.plugin.spi;

import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Question;
import org.jetbrains.annotations.NotNull;

public interface CodeExecutionPresentationStrategy {

    @NotNull String failurePrefix(
            @NotNull Question question,
            @NotNull CodeTypeEnum codeType,
            @NotNull String code
    );
}
