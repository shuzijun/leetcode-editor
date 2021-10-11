package com.shuzijun.leetcode.plugin.listener;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.manager.CodeManager;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.window.NavigatorTable;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author shuzijun
 */
public class TreeMouseListener extends MouseAdapter {


    private NavigatorTable navigatorTable;
    private Project project;

    public TreeMouseListener(NavigatorTable navigatorTable, Project project) {
        this.navigatorTable = navigatorTable;
        this.project = project;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        Question question = navigatorTable.getSelectedRowData();
        if (question != null) {
            if ("lock".equals(question.getStatus())) {
                return;
            }
            if (question.isLeaf()) {
                if (e.getButton() == MouseEvent.BUTTON3) { //鼠标右键
                    final ActionManager actionManager = ActionManager.getInstance();
                    final ActionGroup actionGroup = (ActionGroup) actionManager.getAction(PluginConstant.LEETCODE_NAVIGATOR_ACTIONS_MENU);
                    if (actionGroup != null) {
                        actionManager.createActionPopupMenu("", actionGroup).getComponent().show(e.getComponent(), e.getX(), e.getY());
                    }
                } else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    ProgressManager.getInstance().run(new Task.Backgroundable(project, PluginConstant.LEETCODE_EDITOR_OPEN_CODE, false) {
                        @Override
                        public void run(@NotNull ProgressIndicator progressIndicator) {
                            CodeManager.openCode(question, project);
                        }
                    });
                }
            }
        }


    }
}
