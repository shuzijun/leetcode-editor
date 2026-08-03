package com.shuzijun.leetcode.plugin.manager;

import com.alibaba.fastjson.JSONObject;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Submission;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SubmissionManagerTest {

    @Test
    public void parsesSubmissionHistoryFields() {
        List<Submission> submissions = SubmissionManager.parseSubmissions(
                "{\"data\":{\"submissionList\":{\"submissions\":[{" +
                        "\"id\":\"100\",\"statusDisplay\":\"Accepted\",\"lang\":\"java\"," +
                        "\"runtime\":\"4 ms\",\"timestamp\":\"1720000000\",\"memory\":\"42 MB\"}]}}}");

        assertEquals(1, submissions.size());
        Submission submission = submissions.get(0);
        assertEquals("100", submission.getId());
        assertEquals("Accepted", submission.getStatus());
        assertEquals("java", submission.getLang());
        assertEquals("4 ms", submission.getRuntime());
        assertEquals("1720000000", submission.getTime());
        assertEquals("42 MB", submission.getMemory());
    }

    @Test
    public void formatsAcceptedAndWrongAnswerDetails() {
        Submission accepted = submission("Accepted");
        String acceptedText = SubmissionManager.formatSubmission("class Solution {}", details(), accepted, CodeTypeEnum.JAVA);
        assertTrue(acceptedText.contains("class Solution {}"));
        assertTrue(acceptedText.contains("//runtime:4 ms"));
        assertTrue(acceptedText.contains("//memory:42 MB"));

        Submission wrongAnswer = submission("Wrong Answer");
        String wrongAnswerText = SubmissionManager.formatSubmission("class Solution {}", details(), wrongAnswer, CodeTypeEnum.JAVA);
        assertTrue(wrongAnswerText.contains("//total_testcases:3"));
        assertTrue(wrongAnswerText.contains("//expected_output:[0,1]"));
        assertTrue(wrongAnswerText.contains("//last_testcase:[3,2,4]"));
    }

    @Test
    public void formatsRuntimeAndCompileErrorsWithDiagnosticFields() {
        Submission runtimeError = submission("Runtime Error");
        String runtimeErrorText = SubmissionManager.formatSubmission("class Solution {}", details(), runtimeError, CodeTypeEnum.JAVA);
        assertTrue(runtimeErrorText.contains("//runtime_error:NullPointerException"));
        assertTrue(runtimeErrorText.contains("//last_testcase:[3,2,4] 9"));

        Submission compileError = submission("Compile Error");
        String compileErrorText = SubmissionManager.formatSubmission("class Solution {}", details(), compileError, CodeTypeEnum.JAVA);
        assertTrue(compileErrorText.contains("//total_correct:1"));
        assertTrue(compileErrorText.contains("//compile_error:missing return"));
    }

    private static Submission submission(String status) {
        Submission submission = new Submission();
        submission.setStatus(status);
        return submission;
    }

    private static JSONObject details() {
        JSONObject details = new JSONObject();
        details.put("runtime", "4 ms");
        details.put("memory", "42 MB");
        details.put("total_testcases", "3");
        details.put("total_correct", "1");
        details.put("input_formatted", "[2,7]\n9");
        details.put("expected_output", "[0,1]");
        details.put("code_output", "[1,0]");
        details.put("runtime_error", "NullPointerException");
        details.put("last_testcase", "[3,2,4]\n9");
        details.put("compile_error", "missing return");
        return details;
    }
}
