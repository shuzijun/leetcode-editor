package com.shuzijun.leetcode.plugin.listener;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.SimpleTree;
import com.shuzijun.leetcode.plugin.manager.CodeManager;
import com.shuzijun.leetcode.plugin.model.Question;



import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author shuzijun
 */
public class TreeMouseListener extends MouseAdapter {


    private SimpleTree tree;
    private Project project;

    public TreeMouseListener(SimpleTree tree, Project project) {
        this.tree = tree;
        this.project = project;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
        if (selPath != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
            Question question = (Question) node.getUserObject();
            if ("lock".equals(question.getStatus())) {
                return;
            }
            if (question.isLeaf()) {
                if (e.getButton() == 3) { //鼠标右键
                    final ActionManager actionManager = ActionManager.getInstance();
                    final ActionGroup actionGroup = (ActionGroup) actionManager.getAction("leetcode.NavigatorActionsMenu");
                    if (actionGroup != null) {
                        actionManager.createActionPopupMenu("", actionGroup).getComponent().show(e.getComponent(), e.getX(), e.getY());
                    }
                } else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    CodeManager.openCode(question, project);
                }
            }
        }


    }
}
