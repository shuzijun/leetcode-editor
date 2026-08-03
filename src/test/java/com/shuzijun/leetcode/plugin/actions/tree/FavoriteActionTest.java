package com.shuzijun.leetcode.plugin.actions.tree;

import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.model.Tag;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FavoriteActionTest {

    @Test
    public void usesFrontendQuestionIdForTheFavoriteSelectionState() {
        Tag tag = new Tag();
        tag.addQuestion("53");
        Question question = new Question();
        question.setQuestionId("101");
        question.setFrontendQuestionId("53");

        assertTrue(FavoriteAction.isQuestionFavorite(tag, question));
    }

    @Test
    public void returnsFalseWhenTheFrontendQuestionIdIsNotInTheFavoriteList() {
        Tag tag = new Tag();
        tag.addQuestion("53");
        Question question = new Question();
        question.setQuestionId("53");
        question.setFrontendQuestionId("1");

        assertFalse(FavoriteAction.isQuestionFavorite(tag, question));
    }
}
