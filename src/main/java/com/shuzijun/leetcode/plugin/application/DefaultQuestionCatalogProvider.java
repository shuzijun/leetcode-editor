package com.shuzijun.leetcode.plugin.application;

import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.manager.QuestionManager;
import com.shuzijun.leetcode.plugin.model.PageInfo;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.lc.model.QuestionView;
import com.shuzijun.leetcode.plugin.spi.QuestionCatalogProvider;
import org.jetbrains.annotations.NotNull;

public final class DefaultQuestionCatalogProvider implements QuestionCatalogProvider {

    @Override
    public @NotNull String getId() {
        return "leetcode";
    }

    @Override
    public boolean supports(@NotNull String sourceId) {
        return "leetcode".equalsIgnoreCase(sourceId);
    }

    @Override
    public @NotNull PageInfo<QuestionView> loadPage(
            @NotNull Project project, @NotNull PageInfo<QuestionView> request) {
        return QuestionManager.getQuestionViewList(project, request);
    }

    @Override
    public Question loadQuestion(@NotNull Project project, @NotNull String titleSlug) {
        return QuestionManager.getQuestionByTitleSlug(titleSlug, project);
    }
}
