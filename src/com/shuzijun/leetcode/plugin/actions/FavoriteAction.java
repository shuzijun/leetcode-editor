package com.shuzijun.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.shuzijun.leetcode.plugin.manager.FavoriteManager;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.model.Tag;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

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

        JTree tree = anActionEvent.getData(DataKeys.LEETCODE_PROJECTS_TREE);
        DefaultMutableTreeNode note = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        Question question = (Question) note.getUserObject();
        return tag.getQuestions().contains(question.getQuestionId());
    }

    @Override
    public void setSelected(AnActionEvent anActionEvent, boolean b) {
        JTree tree = anActionEvent.getData(DataKeys.LEETCODE_PROJECTS_TREE);
        DefaultMutableTreeNode note = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        Question question = (Question) note.getUserObject();
        if (b) {
            FavoriteManager.addQuestionToFavorite(tag, question);
        }else {
            FavoriteManager.removeQuestionFromFavorite(tag, question);
        }

    }
}
