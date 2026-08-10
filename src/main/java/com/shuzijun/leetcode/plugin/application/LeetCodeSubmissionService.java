package com.shuzijun.leetcode.plugin.application;

import com.shuzijun.lc.LcClient;
import com.shuzijun.lc.RequestContext;
import com.shuzijun.lc.errors.LcException;
import com.shuzijun.lc.model.SubmissionDetail;
import com.shuzijun.lc.model.Submission;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class LeetCodeSubmissionService {

    @NotNull
    public List<Submission> list(@NotNull String titleSlug) throws LcException {
        return client().api().submissions()
                .list(titleSlug, 0, 100, RequestContext.DEFAULT);
    }

    @NotNull
    public SubmissionDetail detail(@NotNull String submissionId) throws LcException {
        LcClient client = client();
        return DetailRequestCoordinator.load(
                new SingleFlightRequestRegistry.RequestKey(
                        URLUtils.getLeetcodeHost(),
                        submissionId,
                        "",
                        "plain-text",
                        "submission-detail"
                ),
                context -> client.api().submissions().detail(submissionId, context),
                LeetCodeSubmissionService::copy
        );
    }

    @NotNull
    private LcClient client() {
        return LcClientFactory.create();
    }

    static SubmissionDetail copy(SubmissionDetail source) {
        SubmissionDetail copy = new SubmissionDetail();
        copy.setRuntime(source.getRuntime());
        copy.setMemory(source.getMemory());
        copy.setTotalTestcases(source.getTotalTestcases());
        copy.setTotalCorrect(source.getTotalCorrect());
        copy.setInputFormatted(source.getInputFormatted());
        copy.setExpectedOutput(source.getExpectedOutput());
        copy.setCodeOutput(source.getCodeOutput());
        copy.setRuntimeError(source.getRuntimeError());
        copy.setLastTestcase(source.getLastTestcase());
        copy.setCompileError(source.getCompileError());
        copy.setSubmissionCode(source.getSubmissionCode());
        return copy;
    }
}
