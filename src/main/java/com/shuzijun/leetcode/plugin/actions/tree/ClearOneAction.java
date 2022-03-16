package com.shuzijun.leetcode.plugin.actions.tree;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.LogUtils;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.utils.VelocityUtils;
import com.shuzijun.leetcode.plugin.window.NavigatorTable;

import java.io.File;

/**
 * @author shuzijun
 */
public class ClearOneAction extends AbstractTreeAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config, NavigatorTable navigatorTable, Question question) {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            String codeType = config.getCodeType();
            CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnum(codeType);
            if (codeTypeEnum == null) {
                MessageUtils.getInstance(anActionEvent.getProject()).showWarnMsg("info", PropertiesUtils.getInfo("config.code"));
                return;
            }

            String fold = VelocityUtils.convert(config.getCustomFileName(), question).split("/")[0];
            String filePath = PersistentConfig.getInstance().getTempFilePath() + fold;   // + codeTypeEnum.getSuffix();
            String contentPath = PersistentConfig.getInstance().getTempFilePath() + "doc/content/" + fold;
            String solutionPath = PersistentConfig.getInstance().getTempFilePath() + "doc/solution/" + fold;  // 没有fold中间路径
            String submissionPath = PersistentConfig.getInstance().getTempFilePath() + "doc/submission/" + fold;
            File file = new File(filePath);
            File file2 = new File(contentPath);
            File file3 = new File(solutionPath);
            File file4 = new File(submissionPath);
            delFile(file, anActionEvent.getProject());
            delFile(file2, anActionEvent.getProject());
            delFile(file3, anActionEvent.getProject());
            delFile(file4, anActionEvent.getProject());
//        if (file.exists()) {
//            ApplicationManager.getApplication().invokeAndWait(() -> {
//                VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
//                if (FileEditorManager.getInstance(anActionEvent.getProject()).isFileOpen(vf)) {
//                    FileEditorManager.getInstance(anActionEvent.getProject()).closeFile(vf);
//                }
//                file.delete();
//            });
//        }
            MessageUtils.getInstance(anActionEvent.getProject()).showInfoMsg(question.getFormTitle(), PropertiesUtils.getInfo("clear.success"));
        });
    }
    public void delFile(File file, Project project) {
        if (!file.exists()) {
            return;
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                delFile(f, project);
            }
        }
        try {
            VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
            if (FileEditorManager.getInstance(project).isFileOpen(vf)) {
                FileEditorManager.getInstance(project).closeFile(vf);
            }
            file.delete();
        } catch (Exception e) {
            LogUtils.LOG.error("Error deleting file", e);
        }

    }

}
