package com.shuzijun.leetcode.platform.repository;

import com.shuzijun.leetcode.platform.model.Submission;

import java.io.File;
import java.util.List;

/**
 * @author shuzijun
 */
public interface SubmissionService extends Service {

    /**
     * 获取提交列表
     *
     * @param titleSlug
     * @return
     */
    List<Submission> getSubmissionService(String titleSlug);

    /**
     * 获取一次提交
     *
     * @param submission
     * @param titleSlug
     * @param isOpenEditor
     * @return
     */
    File openSubmission(Submission submission, String titleSlug, Boolean isOpenEditor);
}
