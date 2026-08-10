package com.shuzijun.leetcode.plugin.application;

import com.shuzijun.lc.model.CodeStartResult;
import com.shuzijun.lc.model.RunCodeParam;
import com.shuzijun.lc.model.RunCodeResult;
import com.shuzijun.lc.model.SubmitParam;
import com.shuzijun.lc.model.SubmitResult;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Question;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LeetCodeCodeServiceTest {

    @Test
    public void mapsPluginQuestionAndLanguageToTypedRunAndSubmitParameters() {
        Question question = question();

        RunCodeParam run = LeetCodeCodeService.toRunCodeParam(
                question,
                CodeTypeEnum.JAVA,
                "class Solution {}"
        );
        SubmitParam submit = LeetCodeCodeService.toSubmitParam(
                question,
                CodeTypeEnum.JAVA,
                "class Solution {}"
        );

        assertEquals("1", run.getQuestionId());
        assertEquals("two-sum", run.getTitleSlug());
        assertEquals("[2,7]\n9", run.getDataInput());
        assertEquals("java", run.getLang());
        assertEquals("large", run.getJudgeType());
        assertEquals("class Solution {}", run.getTypedCode());
        assertEquals("1", submit.getQuestionId());
        assertEquals("two-sum", submit.getTitleSlug());
        assertEquals("java", submit.getLang());
        assertEquals("class Solution {}", submit.getTypedCode());
    }

    @Test
    public void mapsTypedRunStartWithoutLosingExpectedExecution() {
        RunCodeResult sdkStart = new RunCodeResult();
        sdkStart.setHttpStatueCode(200);
        sdkStart.setInterpretId("run-1");
        sdkStart.setExpectedInterpretId("expected-1");
        sdkStart.setTestCase("[2,7]\n9");

        CodeStartResult start = CodeStartResult.fromRun(sdkStart);

        assertEquals(200, start.getStatusCode());
        assertEquals("run-1", start.getId());
        assertEquals("expected-1", start.getExpectedId());
        assertEquals("[2,7]\n9", start.getTestCase());
    }

    @Test
    public void mapsOpaqueSubmitId() {
        SubmitResult sdkStart = new SubmitResult();
        sdkStart.setHttpStatueCode(200);
        sdkStart.setSubmissionIdValue("submit-1");

        CodeStartResult start = CodeStartResult.fromSubmit(sdkStart);

        assertEquals(200, start.getStatusCode());
        assertEquals("submit-1", start.getId());
        assertNull(start.getExpectedId());
    }

    private static Question question() {
        Question question = new Question();
        question.setQuestionId("1");
        question.setTitleSlug("two-sum");
        question.setTestCase("[2,7]\n9");
        return question;
    }
}
