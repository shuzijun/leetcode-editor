package com.shuzijun.leetcode.plugin.actions.tree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.setting.ProjectConfig;
import com.shuzijun.leetcode.plugin.utils.LogUtils;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.utils.VelocityUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author shuzijun
 */
public class ClearOneAction extends AbstractTreeAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config, Question question) {

        String codeType = config.getCodeType();
        CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnum(codeType);
        if (codeTypeEnum == null) {
            MessageUtils.getInstance(anActionEvent.getProject()).showWarnMsg("info", PropertiesUtils.getInfo("config.code"));
            return;
        }

        String filePath = PersistentConfig.getInstance().getTempFilePath() + VelocityUtils.convert(config.getCustomFileName(), question) + codeTypeEnum.getSuffix();

        File file = new File(filePath);
        if (file.exists()) {
            VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(file);
            ApplicationManager.getApplication().invokeAndWait(() -> {
                if (vf != null && FileEditorManager.getInstance(anActionEvent.getProject()).isFileOpen(vf)) {
                    FileEditorManager.getInstance(anActionEvent.getProject()).closeFile(vf);
                }
            });
            try {
                Files.deleteIfExists(file.toPath());
                ProjectConfig projectConfig = ProjectConfig.getInstance(anActionEvent.getProject());
                if (projectConfig != null) {
                    projectConfig.removeEditor(file.getPath(), anActionEvent.getProject().getBasePath());
                }
                if (vf != null && vf.getParent() != null) {
                    vf.getParent().refresh(true, false);
                }
            } catch (IOException e) {
                LogUtils.LOG.error("Error deleting generated LeetCode file", e);
                MessageUtils.getInstance(anActionEvent.getProject()).showErrorMsg(question.getFormTitle(), PropertiesUtils.getInfo("clear.failed"));
                return;
            }
        }
        MessageUtils.getInstance(anActionEvent.getProject()).showInfoMsg(question.getFormTitle(), PropertiesUtils.getInfo("clear.success"));

    }

}
