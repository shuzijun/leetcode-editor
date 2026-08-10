package com.shuzijun.leetcode.plugin.adapter.defaults;

import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.shuzijun.leetcode.plugin.editor.converge.SolutionProvider;
import com.shuzijun.leetcode.plugin.spi.EditorTabContribution;
import org.jetbrains.annotations.NotNull;

public final class DefaultSolutionEditorTabContribution implements EditorTabContribution {

    @Override
    public @NotNull String getId() {
        return "solution";
    }

    @Override
    public int getOrder() {
        return 20;
    }

    @Override
    public @NotNull String getName() {
        return "Solution";
    }

    @Override
    public @NotNull FileEditorProvider createEditorProvider() {
        return new SolutionProvider();
    }
}
