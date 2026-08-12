package com.shuzijun.leetcode.plugin.manager;

import com.shuzijun.lc.model.FavoriteResult;
import com.shuzijun.leetcode.plugin.application.LeetCodeFavoriteService;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.model.Tag;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class FavoriteManagerTest {

    @Test
    public void updatesFavoriteIdsOnlyWhenTheMutationSucceeds() {
        Tag tag = new Tag();
        Question question = question();

        String addError = FavoriteManager.applyFavoriteResult(
                tag,
                question,
                favoriteResult(true, null),
                true
        );

        assertNull(addError);
        assertEquals(true, tag.getQuestions().contains("53"));

        String removeError = FavoriteManager.applyFavoriteResult(
                tag,
                question,
                favoriteResult(true, null),
                false
        );

        assertNull(removeError);
        assertFalse(tag.getQuestions().contains("53"));
    }

    @Test
    public void preservesFavoriteIdsWhenTheMutationReturnsAnError() {
        Tag tag = new Tag();
        tag.addQuestion("53");

        String error = FavoriteManager.applyFavoriteResult(
                tag,
                question(),
                favoriteResult(false, "denied"),
                false
        );

        assertEquals("denied", error);
        assertEquals(true, tag.getQuestions().contains("53"));
    }

    private static Question question() {
        Question question = new Question();
        question.setQuestionId("101");
        question.setFrontendQuestionId("53");
        return question;
    }

    private static FavoriteResult favoriteResult(boolean success, String error) {
        FavoriteResult result = new FavoriteResult();
        result.setOk(success);
        result.setError(error);
        return result;
    }
}
