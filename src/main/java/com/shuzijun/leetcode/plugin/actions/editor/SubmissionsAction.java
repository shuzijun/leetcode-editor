package com.shuzijun.leetcode.plugin.actions.editor;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.DialogWrapper;
import com.shuzijun.leetcode.plugin.editor.ConvergePreview;
import com.shuzijun.leetcode.plugin.manager.SubmissionManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.model.Submission;
import com.shuzijun.leetcode.plugin.window.dialog.SubmissionsPanel;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * @author shuzijun
 */
public class SubmissionsAction extends AbstractEditAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config, Question question) {
        List<Submission> submissionList = SubmissionManager.getSubmissionService(question.getTitleSlug(), anActionEvent.getProject());
        if (submissionList == null || submissionList.isEmpty()) {
            return;
        }
        ApplicationManager.getApplication().invokeLater(() -> {
            SubmissionsPanel.TableModel tableModel = new SubmissionsPanel.TableModel(submissionList);
            SubmissionsPanel dialog = new SubmissionsPanel(anActionEvent.getProject(), tableModel);
            dialog.setTitle(question.getFormTitle() + " Submissions");
            dialog.addTableMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                        int row = dialog.getSelectedRow();
                        openSubmission(anActionEvent, config, question, submissionList, row);
                        dialog.close(DialogWrapper.CANCEL_EXIT_CODE);
                    }
                }
            });
            dialog.addTableKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                        int row = dialog.getSelectedRow();
                        openSubmission(anActionEvent, config, question, submissionList, row);
                        dialog.close(DialogWrapper.CANCEL_EXIT_CODE);
                    }

                }
            });
            dialog.show();
        });
    }

    private void openSubmission(AnActionEvent anActionEvent, Config config, Question question, List<Submission> submissionList, int row) {
        Submission submission = submissionList.get(row);
        if (submission != null) {
            if (config.getConvergeEditor() && openConvergeEditor(anActionEvent, new ConvergePreview.TabSelectFileEditorState("Submissions", submission.getId()))) {
                return;
            }
            ProgressManager.getInstance().run(new Task.Backgroundable(anActionEvent.getProject(), anActionEvent.getActionManager().getId(this), false) {
                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    SubmissionManager.openSubmission(submission, question.getTitleSlug(), anActionEvent.getProject(), true);
                }
            });

        }
    }
}
