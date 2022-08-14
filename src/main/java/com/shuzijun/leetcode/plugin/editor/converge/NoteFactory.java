package com.shuzijun.leetcode.plugin.editor.converge;

import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.shuzijun.leetcode.extension.ConvergeEditorFactory;
import com.shuzijun.leetcode.platform.RepositoryService;
import org.jetbrains.annotations.NotNull;

/**
 * @author shuzijun
 */
public class NoteFactory implements ConvergeEditorFactory {

    @Override
    public @NotNull String getName() {
        return "Note";
    }

    @Override
    public @NotNull FileEditorProvider createEditorProvider(RepositoryService repositoryService) {
        return new NoteProvider(repositoryService);
    }
}
