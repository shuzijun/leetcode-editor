package com.shuzijun.leetcode.plugin.actions.editor;


import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.vfs.VirtualFile;
import com.shuzijun.leetcode.plugin.actions.AbstractAction;
import com.shuzijun.leetcode.plugin.manager.NavigatorAction;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.LeetcodeEditor;
import com.shuzijun.leetcode.plugin.setting.ProjectConfig;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.window.WindowFactory;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author shuzijun
 */
public class PositionAction extends AbstractAction implements DumbAware {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

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
        e.getPresentation().setEnabled(StringUtils.isNotBlank(findOpenQuestionTitleSlug(e)));
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config) {
        String titleSlug = findOpenQuestionTitleSlug(anActionEvent);
        if (StringUtils.isBlank(titleSlug)) {
            MessageUtils.getInstance(anActionEvent.getProject()).showInfoMsg("info", PropertiesUtils.getInfo("tree.null"));
            return;
        }
        NavigatorAction navigatorAction = WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_NAVIGATORACTION);
        if (navigatorAction == null) {
            return;
        }

        if (navigatorAction.position(titleSlug)) {
            ApplicationManager.getApplication().invokeLater(() -> {
                WindowFactory.activateToolWindow(anActionEvent.getProject());
            });
        }

    }

    private String findOpenQuestionTitleSlug(AnActionEvent event) {
        if (event.getProject() == null) {
            return null;
        }
        ProjectConfig projectConfig = ProjectConfig.getInstance(event.getProject());
        if (projectConfig == null) {
            return null;
        }
        FileEditorManager editorManager = FileEditorManager.getInstance(event.getProject());
        for (VirtualFile file : editorManager.getSelectedFiles()) {
            LeetcodeEditor editor = projectConfig.getEditor(file.getPath());
            if (editor != null && StringUtils.isNotBlank(editor.getTitleSlug())) {
                return editor.getTitleSlug();
            }
        }
        for (VirtualFile file : editorManager.getOpenFiles()) {
            LeetcodeEditor editor = projectConfig.getEditor(file.getPath());
            if (editor != null && StringUtils.isNotBlank(editor.getTitleSlug())) {
                return editor.getTitleSlug();
            }
        }
        return projectConfig.getLastOpenedQuestionTitleSlug();
    }
}
