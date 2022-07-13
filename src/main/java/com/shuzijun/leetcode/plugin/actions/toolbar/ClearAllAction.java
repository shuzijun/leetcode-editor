package com.shuzijun.leetcode.plugin.actions.toolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBPanel;
import com.shuzijun.leetcode.plugin.actions.AbstractAction;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.LogUtils;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * @author shuzijun
 */
public class ClearAllAction extends AbstractAction implements DumbAware {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config) {

        ApplicationManager.getApplication().invokeAndWait(() -> {
            ClearAllWarningPanel dialog = new ClearAllWarningPanel(anActionEvent.getProject());
            dialog.setTitle("Clear All");

            if (dialog.showAndGet()) {
                String filePath = PersistentConfig.getInstance().getTempFilePath();

                File file = new File(filePath);
                if (!file.exists() || !file.isDirectory()) {
                    MessageUtils.getInstance(anActionEvent.getProject()).showInfoMsg("info", PropertiesUtils.getInfo("clear.success"));
                    return;
                }

                try {
                    delFile(file, anActionEvent.getProject());
                    MessageUtils.getInstance(anActionEvent.getProject()).showInfoMsg("info", PropertiesUtils.getInfo("clear.success"));
                } catch (Exception ee) {
                    LogUtils.LOG.error("清理文件错误", ee);
                    MessageUtils.getInstance(anActionEvent.getProject()).showErrorMsg("error", PropertiesUtils.getInfo("clear.failed"));
                }
            }
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

    private class ClearAllWarningPanel extends DialogWrapper {

        private JPanel jpanel;

        public ClearAllWarningPanel(@Nullable Project project) {
            super(project, true);
            jpanel = new JBPanel();
            jpanel.add(new JLabel("Clear All File？"));
            jpanel.setMinimumSize(new Dimension(200, 100));
            setModal(true);
            init();
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            return jpanel;
        }
    }
}
