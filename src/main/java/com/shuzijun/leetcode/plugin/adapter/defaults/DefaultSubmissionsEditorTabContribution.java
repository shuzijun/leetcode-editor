package com.shuzijun.leetcode.plugin.adapter.defaults;

import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.shuzijun.leetcode.plugin.editor.converge.SubmissionsProvider;
import com.shuzijun.leetcode.plugin.spi.EditorTabContribution;
import org.jetbrains.annotations.NotNull;

public final class DefaultSubmissionsEditorTabContribution implements EditorTabContribution {

    @Override
    public @NotNull String getId() {
        return "submissions";
    }

    @Override
    public int getOrder() {
        return 30;
    }

    @Override
    public @NotNull String getName() {
        return "Submissions";
    }

    @Override
    public @NotNull FileEditorProvider createEditorProvider() {
        return new SubmissionsProvider();
    }
}
