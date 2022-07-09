package com.shuzijun.leetcode.plugin.listener;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.manager.CodeManager;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.model.QuestionView;
import com.shuzijun.leetcode.plugin.window.NavigatorTableData;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * @author shuzijun
 */
public class JTableKeyAdapter extends KeyAdapter {

    private NavigatorTableData<? extends QuestionView> navigatorTable;
    private Project project;

    public JTableKeyAdapter(NavigatorTableData<? extends QuestionView> navigatorTable, Project project) {
        this.navigatorTable = navigatorTable;
        this.project = project;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        QuestionView question = navigatorTable.getSelectedRowData();
        if (question != null) {
            if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                ProgressManager.getInstance().run(new Task.Backgroundable(project, PluginConstant.LEETCODE_EDITOR_OPEN_CODE, false) {
                    @Override
                    public void run(@NotNull ProgressIndicator progressIndicator) {
                        CodeManager.openCode(question.getTitleSlug(), project);
                    }
                });
            }
        }
    }
}
