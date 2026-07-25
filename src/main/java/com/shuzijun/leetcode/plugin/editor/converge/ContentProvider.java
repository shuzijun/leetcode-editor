package com.shuzijun.leetcode.plugin.editor.converge;

import com.intellij.openapi.fileEditor.FileEditor;
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
        if (leetcodeEditor == null || leetcodeEditor.getContentPath() == null) {
            throw new IllegalStateException("Missing LeetCode editor metadata for " + file.getPath());
        }
        // Generated content is refreshed when it is created. Avoid waiting on a background Future
        // from the editor-creation path, which is normally invoked on the UI thread.
        VirtualFile contentVf = LocalFileSystem.getInstance().findFileByIoFile(new File(leetcodeEditor.getContentPath()));
        if (contentVf == null) {
            throw new IllegalStateException("LeetCode content file is not available in VFS: " + leetcodeEditor.getContentPath());
        }
        return super.createEditor(project, contentVf);
    }
}
