package com.shuzijun.leetcode.plugin.spi;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface ConsoleWorkbenchProvider {

    @Nullable
    default JComponent createWorkbench(@NotNull Project project) {
        return null;
    }
}
