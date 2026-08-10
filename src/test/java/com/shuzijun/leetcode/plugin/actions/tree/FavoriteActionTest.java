package com.shuzijun.leetcode.plugin.actions.tree;

import com.shuzijun.leetcode.plugin.model.Tag;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FavoriteActionTest {

    @Test
    public void usesSelectedRowIdForTheFavoriteSelectionState() {
        Tag tag = new Tag();
        tag.addQuestion("53");

        assertTrue(FavoriteAction.isQuestionFavorite(tag, "53"));
    }

    @Test
    public void returnsFalseWhenTheFrontendQuestionIdIsNotInTheFavoriteList() {
        Tag tag = new Tag();
        tag.addQuestion("53");

        assertFalse(FavoriteAction.isQuestionFavorite(tag, "1"));
    }
}
