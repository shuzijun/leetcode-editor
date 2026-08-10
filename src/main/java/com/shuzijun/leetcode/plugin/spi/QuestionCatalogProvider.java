package com.shuzijun.leetcode.plugin.spi;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.model.PageInfo;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.lc.model.QuestionView;
import org.jetbrains.annotations.NotNull;

public interface QuestionCatalogProvider extends OrderedContribution {

    ExtensionPointName<QuestionCatalogProvider> EP_NAME =
            ExtensionPointName.create("leetcode-editor.questionCatalog");

    boolean supports(@NotNull String sourceId);

    @NotNull PageInfo<QuestionView> loadPage(@NotNull Project project, @NotNull PageInfo<QuestionView> request);

    Question loadQuestion(@NotNull Project project, @NotNull String titleSlug);
}
