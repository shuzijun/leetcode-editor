package com.shuzijun.leetcode.plugin.utils;

import com.shuzijun.lc.http.HttpResponse;
import com.shuzijun.leetcode.plugin.model.Question;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CommonUtilityContractTest {

    @Test
    public void supportsTheLegacyQuestionAliasInVelocityTemplates() {
        Question question = new Question("Two Sum");
        question.setTitleSlug("two-sum");

        assertEquals(
                "two-sum:Two Sum",
                VelocityUtils.convert("${q.titleSlug}:${q.title}", question)
        );
    }

    @Test
    public void reportsOnlyHttp200AsSuccessful() {
        assertTrue(new HttpResponse(200).isCodeSuccess());
        assertFalse(new HttpResponse(204).isCodeSuccess());
        assertFalse(new HttpResponse(500).isCodeSuccess());
    }
}
