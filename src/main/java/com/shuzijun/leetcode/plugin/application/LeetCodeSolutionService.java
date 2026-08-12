package com.shuzijun.leetcode.plugin.application;

import com.shuzijun.lc.LcClient;
import com.shuzijun.lc.RequestContext;
import com.shuzijun.lc.errors.LcException;
import com.shuzijun.lc.model.Solution;
import com.shuzijun.leetcode.plugin.utils.DevelopmentTools;
import com.shuzijun.leetcode.plugin.utils.LogUtils;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class LeetCodeSolutionService {

    @NotNull
    public List<Solution> loadPage(
            @NotNull String titleSlug,
            int first,
            int skip
    ) throws LcException {
        List<com.shuzijun.lc.model.Solution> sdkSolutions = client().api().solutions()
                .list(titleSlug, first, skip, RequestContext.DEFAULT);
        if (sdkSolutions == null) {
            return new ArrayList<>();
        }
        for (com.shuzijun.lc.model.Solution sdkSolution : sdkSolutions) {
            sdkSolution.setTags(formatTags(sdkSolution.getTags()));
        }
        return sdkSolutions;
    }

    @Nullable
    public String loadArticle(@NotNull String articleId) throws LcException {
        if (DevelopmentTools.isEnabled()) {
            String operationName = URLUtils.isCn()
                    ? "solutionDetailArticle"
                    : "ugcArticleSolutionArticle";
            LogUtils.LOG.info("[NETWORK] graphql operation=" + operationName
                    + " articleId=" + articleId);
        }
        LcClient client = client();
        return DetailRequestCoordinator.load(
                new SingleFlightRequestRegistry.RequestKey(
                        URLUtils.getLeetcodeHost(),
                        articleId,
                        contentLanguage(),
                        "markdown",
                        "solution-article"
                ),
                context -> client.api().solutions().article(articleId, context),
                Function.identity()
        );
    }

    @NotNull
    static String formatTags(String tags) {
        if (StringUtils.isBlank(tags)) {
            return "";
        }
        StringBuilder formatted = new StringBuilder();
        for (String tag : StringUtils.split(tags, ',')) {
            String value = StringUtils.trim(tag);
            if (StringUtils.isNotBlank(value)) {
                formatted.append('[').append(value).append("] ");
            }
        }
        return formatted.toString();
    }

    @NotNull
    private LcClient client() {
        return LcClientFactory.create();
    }

    private static String contentLanguage() {
        return URLUtils.getDescContent();
    }
}
