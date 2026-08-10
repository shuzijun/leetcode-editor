package com.shuzijun.leetcode.plugin.editor.converge;

import com.intellij.openapi.fileEditor.AsyncFileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.shuzijun.leetcode.plugin.editor.LCVProvider;
import com.shuzijun.leetcode.plugin.editor.LCVPreview;
import com.shuzijun.leetcode.plugin.editor.QuestionPreviewRenderMode;
import com.shuzijun.leetcode.plugin.model.LeetcodeEditor;
import com.shuzijun.leetcode.plugin.setting.ProjectConfig;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author shuzijun
 */
public class ContentProvider extends LCVProvider implements AsyncFileEditorProvider {

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        return true;
    }

    @Override
    public @NotNull Builder createEditorAsync(@NotNull Project project, @NotNull VirtualFile file) {
        LeetcodeEditor leetcodeEditor = ProjectConfig.getInstance(project).getEditor(file.getPath());
        if (leetcodeEditor == null) {
            throw new IllegalStateException("Missing LeetCode editor metadata for " + file.getPath());
        }
        if (leetcodeEditor.getContentPath() == null) {
            return editorBuilder(project, file, QuestionPreviewRenderMode.QUESTION_HTML, false);
        }
        File contentFile = new File(leetcodeEditor.getContentPath());
        VirtualFile contentVf = LocalFileSystem.getInstance().findFileByIoFile(contentFile);
        if (contentVf == null && contentFile.isFile()) {
            contentVf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(contentFile);
        }
        if (contentVf == null) {
            return editorBuilder(project, file, QuestionPreviewRenderMode.QUESTION_HTML, false);
        }
        return editorBuilder(project, contentVf, QuestionPreviewRenderMode.QUESTION_HTML, true);
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return createEditorAsync(project, file).build();
    }

    private static Builder editorBuilder(
            @NotNull Project project,
            @NotNull VirtualFile file,
            @NotNull QuestionPreviewRenderMode renderMode,
            boolean preview
    ) {
        return new Builder() {
            @Override
            public FileEditor build() {
                return preview
                        ? new LCVPreview(project, file, renderMode)
                        : new PsiAwareTextEditorProvider().createEditor(project, file);
            }
        };
    }
}
