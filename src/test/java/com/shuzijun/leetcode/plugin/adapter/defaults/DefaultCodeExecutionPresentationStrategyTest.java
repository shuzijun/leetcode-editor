package com.shuzijun.leetcode.plugin.adapter.defaults;

import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Question;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefaultCodeExecutionPresentationStrategyTest {

    @Test
    public void keepsDefaultFailureMessageWithoutPrivateCodeLink() {
        Question question = new Question();
        question.setTitleSlug("two-sum");

        String prefix = new DefaultCodeExecutionPresentationStrategy()
                .failurePrefix(question, CodeTypeEnum.JAVA, "class Solution {}");

        assertEquals("", prefix);
    }
}
