package com.shuzijun.leetcode.plugin.actions.editor;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import com.shuzijun.leetcode.plugin.actions.AbstractAction;
import com.shuzijun.leetcode.plugin.manager.SubmissionManager;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.LeetcodeEditor;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.model.Submission;
import com.shuzijun.leetcode.plugin.setting.ProjectConfig;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.window.SubmissionsPanel;

import java.util.List;

/**
 * @author shuzijun
 */
public class SubmissionsAction extends AbstractAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config) {
        VirtualFile vf = anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE);
        LeetcodeEditor leetcodeEditor = ProjectConfig.getInstance(anActionEvent.getProject()).getEditor(vf.getPath());
        if (leetcodeEditor == null) {
            return;
        }
        Question question = ViewManager.getQuestionById(leetcodeEditor.getQuestionId(), anActionEvent.getProject());
        if (question == null) {
            MessageUtils.getInstance(anActionEvent.getProject()).showInfoMsg("info", PropertiesUtils.getInfo("tree.null"));
            return;
        }
        List<Submission> submissionList = SubmissionManager.getSubmissionService(question, anActionEvent.getProject());
        if (submissionList == null || submissionList.isEmpty()) {
            return;
        }
        SubmissionsPanel.TableModel tableModel = new SubmissionsPanel.TableModel(submissionList);
        SubmissionsPanel dialog = new SubmissionsPanel(anActionEvent.getProject(), tableModel);
        dialog.setTitle(question.getFormTitle() + " Submissions");

        if (dialog.showAndGet()) {
            SubmissionManager.openSubmission(submissionList.get(dialog.getSelectedRow()), question, anActionEvent.getProject());
        }
    }

}
