package com.shuzijun.leetcode.plugin.editor.converge;

import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.shuzijun.leetcode.extension.ConvergeEditorFactory;
import com.shuzijun.leetcode.platform.RepositoryService;
import org.jetbrains.annotations.NotNull;

/**
 * @author shuzijun
 */
public class ContentFactory implements ConvergeEditorFactory {

    @Override
    public @NotNull String getName() {
        return "Content";
    }

    @Override
    public @NotNull FileEditorProvider createEditorProvider(RepositoryService repositoryService) {
        return new ContentProvider();
    }
}
