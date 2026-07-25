package com.shuzijun.leetcode.plugin.editor;

import com.shuzijun.leetcode.plugin.model.Question;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QuestionEditorTabTitleProviderTest {

    @Test
    public void usesLocalFileNameWhenQuestionIsNotCached() {
        assertEquals("two-sum", QuestionEditorTabTitleProvider.resolveLocalTitle(null, "two-sum"));
    }

    @Test
    public void usesCachedQuestionWithoutLoadingFromNetwork() {
        Question question = new Question();
        question.setFrontendQuestionId("1");
        question.setTitle("Two Sum");

        assertEquals("[1]Two Sum", QuestionEditorTabTitleProvider.resolveLocalTitle(question, "two-sum"));
    }
}
