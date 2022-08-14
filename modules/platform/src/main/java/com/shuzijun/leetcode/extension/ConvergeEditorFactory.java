package com.shuzijun.leetcode.extension;

import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.shuzijun.leetcode.platform.RepositoryService;
import org.jetbrains.annotations.NotNull;

public interface ConvergeEditorFactory {

    @NotNull String getName();

    @NotNull FileEditorProvider createEditorProvider(RepositoryService repositoryService);
}
