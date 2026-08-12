package com.shuzijun.leetcode.plugin.manager;

import com.intellij.openapi.project.Project;
import com.shuzijun.lc.model.SubmissionDetail;
import com.shuzijun.leetcode.plugin.application.LanguageTemplateService;
import com.shuzijun.leetcode.plugin.application.LeetCodeServices;
import com.shuzijun.leetcode.plugin.application.LeetCodeSubmissionService;
import com.shuzijun.lc.model.CodeMetaData;
import com.shuzijun.lc.model.CodeSnippet;
import com.shuzijun.lc.model.QuestionView;
import com.shuzijun.lc.model.Session;
import com.shuzijun.lc.model.Solution;
import com.shuzijun.lc.model.Submission;
import com.shuzijun.lc.model.User;
import com.shuzijun.leetcode.plugin.model.*;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;

import java.io.File;
import java.util.List;

/**
 * @author shuzijun
 */
public class SubmissionManager {

    public static List<Submission> getSubmissionService(String titleSlug, Project project) {

        if (!LeetCodeServices.login().isLoggedIn()) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
            return null;
        }

        try {
            return submissionService().list(titleSlug);
        } catch (Exception exception) {
            LogUtils.LOG.error("获取提交列表失败", exception);
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("request.failed"));
            return java.util.Collections.emptyList();
        }
    }

    static String formatSubmission(SubmissionDetail detail, Submission submission,
                                   CodeTypeEnum codeTypeEnum) {
        CodeTypeEnum effectiveCodeType = codeTypeEnum == null ? CodeTypeEnum.JAVA : codeTypeEnum;
        StringBuilder result = new StringBuilder();
        result.append(detail.getSubmissionCode().replace("\\u000A", "\n")).append("\n");

        if ("Accepted".equals(submission.getStatus())) {
            append(result, effectiveCodeType, "runtime", detail.getRuntime());
            append(result, effectiveCodeType, "memory", detail.getMemory());
        } else if ("Wrong Answer".equals(submission.getStatus())) {
            append(result, effectiveCodeType, "total_testcases", detail.getTotalTestcases());
            append(result, effectiveCodeType, "total_correct", detail.getTotalCorrect());
            append(result, effectiveCodeType, "input_formatted", detail.getInputFormatted());
            append(result, effectiveCodeType, "expected_output", detail.getExpectedOutput());
            append(result, effectiveCodeType, "code_output", detail.getCodeOutput());
            append(result, effectiveCodeType, "last_testcase", detail.getLastTestcase());
        } else if ("Runtime Error".equals(submission.getStatus())) {
            append(result, effectiveCodeType, "runtime_error", detail.getRuntimeError());
            append(result, effectiveCodeType, "last_testcase", oneLine(detail.getLastTestcase()));
        } else if ("Compile Error".equals(submission.getStatus())) {
            append(result, effectiveCodeType, "total_correct", detail.getTotalCorrect());
            append(result, effectiveCodeType, "compile_error", detail.getCompileError());
        } else {
            append(result, effectiveCodeType, "runtime", detail.getRuntime());
            append(result, effectiveCodeType, "memory", detail.getMemory());
            append(result, effectiveCodeType, "total_testcases", detail.getTotalTestcases());
            append(result, effectiveCodeType, "total_correct", detail.getTotalCorrect());
            append(result, effectiveCodeType, "input_formatted", detail.getInputFormatted());
            append(result, effectiveCodeType, "expected_output", detail.getExpectedOutput());
            append(result, effectiveCodeType, "code_output", detail.getCodeOutput());
            append(result, effectiveCodeType, "runtime_error", detail.getRuntimeError());
            if (detail.getLastTestcase() != null) {
                append(result, effectiveCodeType, "last_testcase", oneLine(detail.getLastTestcase()));
            }
        }
        return result.toString();
    }

    private static void append(StringBuilder result, CodeTypeEnum codeTypeEnum, String key, String value) {
        result.append(codeTypeEnum.getComment()).append(key).append(":").append(value).append("\n");
    }

    private static String oneLine(String value) {
        return value == null ? null : value.replaceAll("(\\r|\\r\\n|\\n\\r|\\n)", " ");
    }

    public static File openSubmission(Submission submission, String titleSlug, Project project, Boolean isOpenEditor) {

        if (!LeetCodeServices.login().isLoggedIn()) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
            return null;
        }
        Question question = QuestionManager.getQuestionByTitleSlug(titleSlug, project);
        CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnumByLangSlug(submission.getLang());
        String filePath = PersistentConfig.getInstance().getTempFilePath()
                + Constant.DOC_SUBMISSION
                + LanguageTemplateService.fileName("submission", question)
                + submission.getId()
                + ".txt";

        File file = new File(filePath);
        if (file.exists()) {
            if (isOpenEditor) {
                FileUtils.openFileEditor(file, project);
            }
            return file;
        } else {
            try {
                SubmissionDetail detail = submissionService()
                        .detail(submission.getId());
                FileUtils.saveFile(file, formatSubmission(detail, submission, codeTypeEnum));
                if (isOpenEditor) {
                    FileUtils.openFileEditor(file, project);
                }
                return file;
            } catch (Exception e) {
                LogUtils.LOG.error("获取提交详情失败", e);
                MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.failed"));
                return file;
            }

        }

    }

    private static LeetCodeSubmissionService submissionService() {
        return LeetCodeServices.submission();
    }
}
