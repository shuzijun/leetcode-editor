package com.shuzijun.leetcode.plugin.manager;

import com.shuzijun.lc.model.SubmissionDetail;
import com.shuzijun.leetcode.plugin.application.LeetCodeSubmissionService;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.lc.model.Submission;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SubmissionManagerTest {

    @Test
    public void formatsAcceptedAndWrongAnswerDetails() {
        Submission accepted = submission("Accepted");
        String acceptedText = SubmissionManager.formatSubmission(
                details(), accepted, CodeTypeEnum.JAVA);
        assertTrue(acceptedText.contains("class Solution {}"));
        assertTrue(acceptedText.contains("//runtime:4 ms"));
        assertTrue(acceptedText.contains("//memory:42 MB"));

        Submission wrongAnswer = submission("Wrong Answer");
        String wrongAnswerText = SubmissionManager.formatSubmission(
                details(), wrongAnswer, CodeTypeEnum.JAVA);
        assertTrue(wrongAnswerText.contains("//total_testcases:3"));
        assertTrue(wrongAnswerText.contains("//expected_output:[0,1]"));
        assertTrue(wrongAnswerText.contains("//last_testcase:[3,2,4]"));
    }

    @Test
    public void formatsRuntimeAndCompileErrorsWithDiagnosticFields() {
        Submission runtimeError = submission("Runtime Error");
        String runtimeErrorText = SubmissionManager.formatSubmission(
                details(), runtimeError, CodeTypeEnum.JAVA);
        assertTrue(runtimeErrorText.contains("//runtime_error:NullPointerException"));
        assertTrue(runtimeErrorText.contains("//last_testcase:[3,2,4] 9"));

        Submission compileError = submission("Compile Error");
        String compileErrorText = SubmissionManager.formatSubmission(
                details(), compileError, CodeTypeEnum.JAVA);
        assertTrue(compileErrorText.contains("//total_correct:1"));
        assertTrue(compileErrorText.contains("//compile_error:missing return"));
    }

    @Test
    public void fallsBackToJavaCommentsForUnknownSubmissionLanguage() {
        String text = SubmissionManager.formatSubmission(
                details(), submission("Accepted"), null);

        assertTrue(text.contains("//runtime:4 ms"));
        assertTrue(text.contains("//memory:42 MB"));
    }

    private static Submission submission(String status) {
        Submission submission = new Submission();
        submission.setStatus(status);
        return submission;
    }

    private static SubmissionDetail details() {
        SubmissionDetail detail = new SubmissionDetail();
        detail.setSubmissionCode("class Solution {}");
        detail.setRuntime("4 ms");
        detail.setMemory("42 MB");
        detail.setTotalTestcases("3");
        detail.setTotalCorrect("1");
        detail.setInputFormatted("[2,7]\n9");
        detail.setExpectedOutput("[0,1]");
        detail.setCodeOutput("[1,0]");
        detail.setRuntimeError("NullPointerException");
        detail.setLastTestcase("[3,2,4]\n9");
        detail.setCompileError("missing return");
        return detail;
    }
}
