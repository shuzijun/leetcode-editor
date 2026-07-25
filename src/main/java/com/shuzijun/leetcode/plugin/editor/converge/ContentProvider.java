package com.shuzijun.leetcode.plugin.editor.converge;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.shuzijun.leetcode.plugin.editor.LCVProvider;
import com.shuzijun.leetcode.plugin.model.LeetcodeEditor;
import com.shuzijun.leetcode.plugin.setting.ProjectConfig;
import org.jetbrains.annotations.NotNull;

import java.io.File;

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
            return new PsiAwareTextEditorProvider().createEditor(project, file);
        }
        // Generated content is refreshed when it is created. Avoid waiting on a background Future
        // from the editor-creation path, which is normally invoked on the UI thread.
        File contentFile = new File(leetcodeEditor.getContentPath());
        VirtualFile contentVf = LocalFileSystem.getInstance().findFileByIoFile(contentFile);
        if (contentVf == null && contentFile.isFile()) {
            contentVf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(contentFile);
        }
        if (contentVf == null) {
            return new PsiAwareTextEditorProvider().createEditor(project, file);
        }
        return super.createEditor(project, contentVf);
    }
}
