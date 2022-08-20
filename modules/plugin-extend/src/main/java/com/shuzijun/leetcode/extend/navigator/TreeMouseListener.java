package com.shuzijun.leetcode.extend.navigator;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.ui.treeStructure.SimpleTree;
import com.shuzijun.leetcode.platform.RepositoryService;
import com.shuzijun.leetcode.platform.model.QuestionView;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author shuzijun
 */
public class TreeMouseListener extends MouseAdapter {


    private SimpleTree tree;
    private RepositoryService repositoryService;

    public TreeMouseListener(SimpleTree tree, RepositoryService repositoryService) {
        this.tree = tree;
        this.repositoryService = repositoryService;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
        if (selPath != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
            QuestionView question = (QuestionView) node.getUserObject();
            if ("lock".equals(question.getStatus())) {
                return;
            }

            if (e.getButton() == 3) { //鼠标右键
                final ActionManager actionManager = ActionManager.getInstance();
                final ActionGroup actionGroup = (ActionGroup) actionManager.getAction("leetcode.NavigatorActionsMenu");
                if (actionGroup != null) {
                    actionManager.createActionPopupMenu("", actionGroup).getComponent().show(e.getComponent(), e.getX(), e.getY());
                }
            } else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                ProgressManager.getInstance().run(new Task.Backgroundable(repositoryService.getProject(),"leetcode.editor.openCode",false) {
                    @Override
                    public void run(@NotNull ProgressIndicator progressIndicator) {
                        repositoryService.getCodeService().openCode(question.getTitleSlug());
                    }
                });
            }

        }


    }
}
