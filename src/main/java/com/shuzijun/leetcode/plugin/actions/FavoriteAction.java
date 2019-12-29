package com.shuzijun.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.shuzijun.leetcode.plugin.manager.FavoriteManager;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.model.Tag;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.window.WindowFactory;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author shuzijun
 */
public class FavoriteAction extends ToggleAction {

    private Tag tag;

    public FavoriteAction(@Nullable String text, Tag tag) {
        super(text);
        this.tag = tag;
    }

    @Override
    public boolean isSelected(AnActionEvent anActionEvent) {

        JTree tree = WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_TREE);
        Question question = ViewManager.getTreeQuestion(tree);
        if (question == null) {
            return false;
        }
        return tag.getQuestions().contains(question.getQuestionId());
    }

    @Override
    public void setSelected(AnActionEvent anActionEvent, boolean b) {
        JTree tree = WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_TREE);
        Question question = ViewManager.getTreeQuestion(tree);
        if (question == null) {
            return;
        }
        if (b) {
            FavoriteManager.addQuestionToFavorite(tag, question);
        }else {
            FavoriteManager.removeQuestionFromFavorite(tag, question);
        }

    }
}
