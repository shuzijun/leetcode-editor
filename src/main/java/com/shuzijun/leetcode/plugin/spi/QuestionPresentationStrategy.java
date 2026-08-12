package com.shuzijun.leetcode.plugin.spi;

import com.shuzijun.lc.model.Question;
import org.jetbrains.annotations.NotNull;

public interface QuestionPresentationStrategy {

    @NotNull String renderContent(
            @NotNull Question question,
            boolean translatedContent,
            boolean showTopics
    );
}
