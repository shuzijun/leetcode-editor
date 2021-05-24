package com.shuzijun.leetcode.plugin.actions.tree;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.window.WindowFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author hongjinfeng
 * @date 2021/5/19 4:32 下午
 */
public abstract class AbstractSortAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        JTree tree = WindowFactory.getDataContext(Objects.requireNonNull(anActionEvent.getProject())).getData(DataKeys.LEETCODE_PROJECTS_TREE);
        assert tree != null;
        DefaultMutableTreeNode note = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        Enumeration<TreeNode> children = note.children();
        List<MutableTreeNode> childrenForSort = new LinkedList<>();
        while (children.hasMoreElements()) {
            childrenForSort.add((MutableTreeNode) children.nextElement());
        }
        Question tag = (Question) note.getUserObject();
        sortChildren(tag, childrenForSort);
        note.setUserObject(tag);
        note.removeAllChildren();
        for (TreeNode treeNode : childrenForSort) {
            note.add((MutableTreeNode) treeNode);
        }
        tree.updateUI();
    }

    public abstract void sortChildren(Question tag, List<MutableTreeNode> childrenForSort);
}
