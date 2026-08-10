package com.shuzijun.leetcode.plugin.application;

import com.shuzijun.lc.LcClient;
import com.shuzijun.lc.RequestContext;
import com.shuzijun.lc.errors.LcException;
import com.shuzijun.lc.model.FavoriteResult;
import org.jetbrains.annotations.NotNull;

public final class LeetCodeFavoriteService {

    @NotNull
    public FavoriteResult add(@NotNull String favoriteIdHash, @NotNull String questionId)
            throws LcException {
        return client().api().favorites()
                .add(favoriteIdHash, questionId, RequestContext.DEFAULT);
    }

    @NotNull
    public FavoriteResult remove(@NotNull String favoriteIdHash, @NotNull String questionId)
            throws LcException {
        return client().api().favorites()
                .remove(favoriteIdHash, questionId, RequestContext.DEFAULT);
    }

    @NotNull
    private LcClient client() {
        return LcClientFactory.create();
    }

}
