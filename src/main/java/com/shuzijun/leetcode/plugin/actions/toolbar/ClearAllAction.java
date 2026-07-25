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
import com.shuzijun.leetcode.plugin.setting.ProjectConfig;
import com.shuzijun.leetcode.plugin.utils.LogUtils;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Clears generated files. AbstractAction already invokes this method on a background thread, so
 * only the confirmation dialog and editor operations are marshalled to the UI thread.
 */
public class ClearAllAction extends AbstractAction implements DumbAware {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config) {
        Project project = anActionEvent.getProject();
        AtomicBoolean confirmed = new AtomicBoolean();
        ApplicationManager.getApplication().invokeAndWait(() -> {
            ClearAllWarningPanel dialog = new ClearAllWarningPanel(project);
            dialog.setTitle("Clear All");
            confirmed.set(dialog.showAndGet());
        });
        if (!confirmed.get()) {
            return;
        }

        Path root;
        try {
            root = Paths.get(PersistentConfig.getInstance().getTempFilePath()).toAbsolutePath().normalize();
        } catch (RuntimeException e) {
            showFailure(project, e);
            return;
        }
        if (!Files.isDirectory(root)) {
            showSuccess(project);
            return;
        }
        // Never allow a misconfigured cache path to delete an entire filesystem volume.
        if (root.getParent() == null) {
            showFailure(project, new IOException("Refusing to clear a filesystem root: " + root));
            return;
        }

        VirtualFile rootFile = LocalFileSystem.getInstance().findFileByIoFile(root.toFile());
        VirtualFile refreshTarget = rootFile == null ? null : rootFile.getParent();
        closeEditorsUnder(project, root);
        try {
            deleteTree(root);
            ProjectConfig projectConfig = ProjectConfig.getInstance(project);
            if (projectConfig != null) {
                projectConfig.pruneStaleEntries(project.getBasePath());
            }
            refreshAsync(refreshTarget);
            showSuccess(project);
        } catch (IOException e) {
            LogUtils.LOG.error("Error clearing generated LeetCode files", e);
            refreshAsync(refreshTarget);
            MessageUtils.getInstance(project).showErrorMsg("error", PropertiesUtils.getInfo("clear.failed"));
        }
    }

    private static void closeEditorsUnder(Project project, Path root) {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            FileEditorManager editorManager = FileEditorManager.getInstance(project);
            for (VirtualFile file : editorManager.getOpenFiles()) {
                try {
                    Path openPath = Paths.get(file.getPath()).toAbsolutePath().normalize();
                    if (openPath.startsWith(root)) {
                        editorManager.closeFile(file);
                    }
                } catch (RuntimeException ignored) {
                    // Non-local virtual files are unrelated to the generated-file directory.
                }
            }
        });
    }

    private static void deleteTree(Path root) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) {
                    throw exc;
                }
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void refreshAsync(@Nullable VirtualFile refreshTarget) {
        if (refreshTarget != null) {
            refreshTarget.refresh(true, false);
        }
    }

    private static void showSuccess(Project project) {
        MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("clear.success"));
    }

    private static void showFailure(Project project, Exception error) {
        LogUtils.LOG.error("Invalid LeetCode generated-file directory", error);
        MessageUtils.getInstance(project).showErrorMsg("error", PropertiesUtils.getInfo("clear.failed"));
    }

    private static class ClearAllWarningPanel extends DialogWrapper {
        private final JPanel panel;

        ClearAllWarningPanel(@Nullable Project project) {
            super(project, true);
            panel = new JBPanel<>();
            panel.add(new JLabel("Clear all generated LeetCode files?"));
            panel.setMinimumSize(new Dimension(260, 100));
            setModal(true);
            init();
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            return panel;
        }
    }
}
