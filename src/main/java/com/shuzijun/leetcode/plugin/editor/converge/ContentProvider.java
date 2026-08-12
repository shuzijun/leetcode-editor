package com.shuzijun.leetcode.plugin.editor.converge;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.JBUI;
import com.shuzijun.leetcode.plugin.editor.LCVProvider;
import com.shuzijun.leetcode.plugin.editor.LCVPreview;
import com.shuzijun.leetcode.plugin.editor.QuestionPreviewRenderMode;
import com.shuzijun.leetcode.plugin.model.LeetcodeEditor;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.setting.ProjectConfig;
import com.shuzijun.leetcode.plugin.utils.AsyncUiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.beans.PropertyChangeListener;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author shuzijun
 */
public class ContentProvider extends LCVProvider {

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        return true;
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        LeetcodeEditor leetcodeEditor = ProjectConfig.getInstance(project).getEditor(file.getPath());
        if (leetcodeEditor == null) {
            throw new IllegalStateException("Missing LeetCode editor metadata for " + file.getPath());
        }
        if (leetcodeEditor.getContentPath() == null) {
            return new LCVPreview(project, file, QuestionPreviewRenderMode.QUESTION_HTML);
        }
        return new DeferredContentPreview(project, file, leetcodeEditor.getContentPath());
    }

    private static final class DeferredContentPreview extends UserDataHolderBase implements FileEditor {
        private final Project project;
        private final VirtualFile sourceFile;
        private final String contentPath;
        private final AtomicBoolean loadingStarted = new AtomicBoolean();
        private final JComponent component = JBUI.Panels.simplePanel();
        @Nullable
        private LCVPreview preview;

        private DeferredContentPreview(@NotNull Project project, @NotNull VirtualFile sourceFile, @NotNull String contentPath) {
            this.project = project;
            this.sourceFile = sourceFile;
            this.contentPath = contentPath;
        }

        @Override
        public @NotNull JComponent getComponent() {
            if (loadingStarted.compareAndSet(false, true)) {
                component.add(new JLabel("Loading......"));
                AsyncUiUtils.load(project, this, () -> readContent(contentPath), (content, error) -> {
                    component.removeAll();
                    if (error != null || content == null) {
                        component.add(new JLabel("Unable to load question content."));
                    } else {
                        preview = new LCVPreview(
                                project,
                                sourceFile,
                                content,
                                contentPath,
                                QuestionPreviewRenderMode.QUESTION_HTML
                        );
                        component.add(preview.getComponent());
                    }
                    component.revalidate();
                    component.repaint();
                });
            }
            return component;
        }

        private static String readContent(@NotNull String contentPath) throws IOException {
            return Files.readString(Path.of(contentPath), StandardCharsets.UTF_8);
        }

        @Override
        public @Nullable JComponent getPreferredFocusedComponent() {
            return preview == null ? null : preview.getPreferredFocusedComponent();
        }

        @Override
        public @NotNull String getName() {
            return PluginConstant.LEETCODE_EDITOR_VIEW;
        }

        @Override
        public void setState(@NotNull FileEditorState state) {
        }

        @Override
        public boolean isModified() {
            return false;
        }

        @Override
        public boolean isValid() {
            return sourceFile.isValid();
        }

        @Override
        public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
        }

        @Override
        public @Nullable FileEditorLocation getCurrentLocation() {
            return null;
        }

        @Override
        public @Nullable VirtualFile getFile() {
            return sourceFile;
        }

        @Override
        public void dispose() {
            if (preview != null) {
                Disposer.dispose(preview);
            }
        }
    }
}
