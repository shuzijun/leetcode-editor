package com.shuzijun.leetcode.plugin.editor;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.WeighedFileEditorProvider;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.jcef.JBCefApp;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import org.jetbrains.annotations.NotNull;

/**
 * @author shuzijun
 */
public class LCVProvider extends WeighedFileEditorProvider {


    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        FileType fileType = file.getFileType();
        return fileType == LCVFileType.INSTANCE && JBCefApp.isSupported();
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return new LCVPreview(project, file);
    }

    @Override
    public @NotNull String getEditorTypeId() {
        return PluginConstant.LEETCODE_EDITOR_VIEW + " view";
    }

    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }
}
