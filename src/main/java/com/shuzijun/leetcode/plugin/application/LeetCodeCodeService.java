package com.shuzijun.leetcode.plugin.application;

import com.shuzijun.lc.LcClient;
import com.shuzijun.lc.RequestContext;
import com.shuzijun.lc.errors.LcException;
import com.shuzijun.lc.model.CodeExecutionResult;
import com.shuzijun.lc.model.CodeStartResult;
import com.shuzijun.lc.model.RunCodeParam;
import com.shuzijun.lc.model.SubmitParam;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Question;
import org.jetbrains.annotations.NotNull;

public final class LeetCodeCodeService {

    @NotNull
    public CodeStartResult run(
            @NotNull Question question,
            @NotNull CodeTypeEnum codeType,
            @NotNull String code
    ) throws LcException {
        return run(question, codeType, code, RequestContext.DEFAULT);
    }

    @NotNull
    public CodeStartResult run(
            @NotNull Question question,
            @NotNull CodeTypeEnum codeType,
            @NotNull String code,
            @NotNull RequestContext context
    ) throws LcException {
        return client().api().code().startRun(
                toRunCodeParam(question, codeType, code),
                context
        );
    }

    @NotNull
    public CodeStartResult submit(
            @NotNull Question question,
            @NotNull CodeTypeEnum codeType,
            @NotNull String code
    ) throws LcException {
        return submit(question, codeType, code, RequestContext.DEFAULT);
    }

    @NotNull
    public CodeStartResult submit(
            @NotNull Question question,
            @NotNull CodeTypeEnum codeType,
            @NotNull String code,
            @NotNull RequestContext context
    ) throws LcException {
        return client().api().code().startSubmit(
                toSubmitParam(question, codeType, code),
                context
        );
    }

    @NotNull
    public CodeExecutionResult runResult(@NotNull String interpretId) throws LcException {
        return runResult(interpretId, RequestContext.DEFAULT);
    }

    @NotNull
    public CodeExecutionResult runResult(
            @NotNull String interpretId,
            @NotNull RequestContext context
    ) throws LcException {
        return client().api().code().checkRun(interpretId, context);
    }

    @NotNull
    public CodeExecutionResult submitResult(@NotNull String submissionId) throws LcException {
        return submitResult(submissionId, RequestContext.DEFAULT);
    }

    @NotNull
    public CodeExecutionResult submitResult(
            @NotNull String submissionId,
            @NotNull RequestContext context
    ) throws LcException {
        return client().api().code().checkSubmit(submissionId, context);
    }

    @NotNull
    static RunCodeParam toRunCodeParam(
            Question question,
            CodeTypeEnum codeType,
            String code
    ) {
        return new RunCodeParam(
                question.getQuestionId(),
                question.getTitleSlug(),
                question.getTestCase(),
                codeType.getLangSlug(),
                code
        );
    }

    @NotNull
    static SubmitParam toSubmitParam(
            Question question,
            CodeTypeEnum codeType,
            String code
    ) {
        return new SubmitParam(
                code,
                codeType.getLangSlug(),
                question.getTitleSlug(),
                question.getQuestionId()
        );
    }

    @NotNull
    private LcClient client() {
        return LcClientFactory.create();
    }

}
