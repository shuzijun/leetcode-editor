package com.shuzijun.leetcode.plugin.application;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public class LeetCodeSubmissionServiceTest {

    @Test
    public void mapsTypedSubmissionDetailWithoutDroppingDiagnostics() {
        com.shuzijun.lc.model.SubmissionDetail sdkDetail =
                new com.shuzijun.lc.model.SubmissionDetail();
        sdkDetail.setSubmissionCode("class Solution {}");
        sdkDetail.setRuntime("4 ms");
        sdkDetail.setMemory("42 MB");
        sdkDetail.setTotalTestcases("3");
        sdkDetail.setTotalCorrect("1");
        sdkDetail.setInputFormatted("[2,7]\n9");
        sdkDetail.setExpectedOutput("[0,1]");
        sdkDetail.setCodeOutput("[1,0]");
        sdkDetail.setRuntimeError("runtime failure");
        sdkDetail.setLastTestcase("[3,2,4]\n9");
        sdkDetail.setCompileError("compile failure");

        com.shuzijun.lc.model.SubmissionDetail copy =
                LeetCodeSubmissionService.copy(sdkDetail);

        assertNotSame(sdkDetail, copy);
        assertEquals("class Solution {}", copy.getSubmissionCode());
        assertEquals("4 ms", copy.getRuntime());
        assertEquals("42 MB", copy.getMemory());
        assertEquals("3", copy.getTotalTestcases());
        assertEquals("1", copy.getTotalCorrect());
        assertEquals("[2,7]\n9", copy.getInputFormatted());
        assertEquals("[0,1]", copy.getExpectedOutput());
        assertEquals("[1,0]", copy.getCodeOutput());
        assertEquals("runtime failure", copy.getRuntimeError());
        assertEquals("[3,2,4]\n9", copy.getLastTestcase());
        assertEquals("compile failure", copy.getCompileError());
    }
}
