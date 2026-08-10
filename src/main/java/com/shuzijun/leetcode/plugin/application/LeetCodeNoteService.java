package com.shuzijun.leetcode.plugin.application;

import com.shuzijun.lc.LcClient;
import com.shuzijun.lc.RequestContext;
import com.shuzijun.lc.errors.LcException;
import com.shuzijun.lc.model.CommonNotePage;
import com.shuzijun.lc.model.CommonNoteResult;
import com.shuzijun.lc.model.NoteUpdateResult;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public final class LeetCodeNoteService {

    public String get(@NotNull String titleSlug) throws LcException {
        LcClient client = client();
        return DetailRequestCoordinator.load(
                new SingleFlightRequestRegistry.RequestKey(
                        URLUtils.getLeetcodeHost(),
                        titleSlug,
                        URLUtils.getDescContent(),
                        "markdown",
                        "note"
                ),
                context -> client.api().notes().get(titleSlug, context),
                Function.identity()
        );
    }

    @NotNull
    public NoteUpdateResult update(@NotNull String titleSlug, String content) throws LcException {
        return client().api().notes().updateResult(
                titleSlug,
                content,
                RequestContext.DEFAULT
        );
    }

    @NotNull
    public CommonNotePage list(@NotNull String questionId) throws LcException {
        return client().api().notes().list(questionId, 100, 0, RequestContext.DEFAULT);
    }

    @NotNull
    public CommonNoteResult create(
            @NotNull String questionId,
            String content,
            String summary
    ) throws LcException {
        return client().api().notes().create(
                questionId,
                content,
                summary,
                RequestContext.DEFAULT
        );
    }

    @NotNull
    public CommonNoteResult updateCommon(
            @NotNull String noteId,
            String content,
            String summary
    ) throws LcException {
        return client().api().notes().updateCommon(
                noteId,
                content,
                summary,
                RequestContext.DEFAULT
        );
    }

    public boolean delete(@NotNull String noteId) throws LcException {
        return client().api().notes().delete(noteId, RequestContext.DEFAULT);
    }

    @NotNull
    private LcClient client() {
        return LcClientFactory.create();
    }

}
