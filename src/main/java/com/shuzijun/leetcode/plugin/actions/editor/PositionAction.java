package com.shuzijun.leetcode.plugin.actions.editor;


import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.shuzijun.leetcode.plugin.manager.NavigatorAction;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.LeetcodeEditor;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.setting.ProjectConfig;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.window.WindowFactory;
import org.jetbrains.annotations.NotNull;

/**
 * @author shuzijun
 */
public class PositionAction extends AbstractEditAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        if (e.getProject() == null) {
            return;
        }
        NavigatorAction navigatorAction = WindowFactory.getDataContext(e.getProject()).getData(DataKeys.LEETCODE_PROJECTS_NAVIGATORACTION);
        if (navigatorAction ==null || !navigatorAction.position(null)) {
            e.getPresentation().setEnabled(false);
            return;
        }
        VirtualFile vf = ArrayUtil.getFirstElement(FileEditorManager.getInstance(e.getProject()).getSelectedFiles());
        if (vf == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        LeetcodeEditor leetcodeEditor = ProjectConfig.getInstance(e.getProject()).getEditor(vf.getPath());
        if (leetcodeEditor == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        e.getPresentation().setEnabled(true);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config, Question question) {
        NavigatorAction navigatorAction = WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_NAVIGATORACTION);
        if (navigatorAction == null) {
            return;
        }

        if (navigatorAction.position(question.getTitleSlug())) {
            ApplicationManager.getApplication().invokeLater(() -> {
                WindowFactory.activateToolWindow(anActionEvent.getProject());
            });
        }

    }
}
