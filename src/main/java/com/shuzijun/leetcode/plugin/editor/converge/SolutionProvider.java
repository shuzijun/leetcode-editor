package com.shuzijun.leetcode.plugin.editor.converge;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.WeighedFileEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.shuzijun.leetcode.platform.RepositoryService;
import com.shuzijun.leetcode.platform.model.LeetcodeEditor;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import org.jetbrains.annotations.NotNull;

/**
 * @author shuzijun
 */
public class SolutionProvider extends WeighedFileEditorProvider {

    private final RepositoryService repositoryService;

    public SolutionProvider(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        return true;
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        LeetcodeEditor leetcodeEditor = repositoryService.getLeetcodeEditor(file.getPath());
        return new SolutionPreview(repositoryService, leetcodeEditor);
    }

    @Override
    public @NotNull String getEditorTypeId() {
        return PluginConstant.LEETCODE_EDITOR_TAB_VIEW + " Solution view";
    }

    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }
}
