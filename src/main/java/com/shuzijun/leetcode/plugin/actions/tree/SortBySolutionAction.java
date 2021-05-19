package com.shuzijun.leetcode.plugin.actions.tree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.window.WindowFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author hongjinfeng
 * @date 2021/5/19 4:35 下午
 */
public class SortBySolutionAction extends AbstractSortAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        JTree tree = WindowFactory.getDataContext(Objects.requireNonNull(anActionEvent.getProject())).getData(DataKeys.LEETCODE_PROJECTS_TREE);
        assert tree != null;
        DefaultMutableTreeNode note = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        Enumeration<TreeNode> children = note.children();
        List<TreeNode> childrenForSort = new LinkedList<>();
        while (children.hasMoreElements()) {
            childrenForSort.add(children.nextElement());
        }
        Question tag = (Question) note.getUserObject();
        childrenForSort.sort((o1, o2) -> {
            DefaultMutableTreeNode item1 = (DefaultMutableTreeNode) o1;
            Question question1 = (Question) item1.getUserObject();
            DefaultMutableTreeNode item2 = (DefaultMutableTreeNode) o2;
            Question question2 = (Question) item2.getUserObject();
            return tag.getSolutionSortTrend() * (question1.getTotalSolutionCount() - question2.getTotalSolutionCount());
        });
        tag.setSolutionSortTrend(-tag.getSolutionSortTrend());
        note.setUserObject(tag);
        note.removeAllChildren();
        for (TreeNode treeNode : childrenForSort) {
            note.add((MutableTreeNode) treeNode);
        }
        tree.updateUI();
    }
}
