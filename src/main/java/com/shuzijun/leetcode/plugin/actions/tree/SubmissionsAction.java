package com.shuzijun.leetcode.plugin.actions.tree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.shuzijun.leetcode.plugin.manager.SubmissionManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.model.Submission;
import com.shuzijun.leetcode.plugin.window.SubmissionsPanel;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author shuzijun
 */
public class SubmissionsAction extends AbstractTreeAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config, JTree tree, Question question) {

        List<Submission> submissionList = SubmissionManager.getSubmissionService(question, anActionEvent.getProject());
        if (submissionList == null || submissionList.isEmpty()) {
            return;
        }
        AtomicReference<Submission> submission = new AtomicReference<>();
        ApplicationManager.getApplication().invokeAndWait(() -> {
            SubmissionsPanel.TableModel tableModel = new SubmissionsPanel.TableModel(submissionList);
            SubmissionsPanel dialog = new SubmissionsPanel(anActionEvent.getProject(), tableModel);
            dialog.setTitle(question.getFormTitle() + " Submissions");

            if (dialog.showAndGet()) {
                submission.set(submissionList.get(dialog.getSelectedRow()));
            }
        });
        if(submission.get() !=null){
            SubmissionManager.openSubmission(submission.get(), question, anActionEvent.getProject());
        }

    }

}
