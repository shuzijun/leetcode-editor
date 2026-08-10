package com.shuzijun.leetcode.plugin.spi;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;

public interface NavigatorContribution extends OrderedContribution {

    ExtensionPointName<NavigatorContribution> EP_NAME =
            ExtensionPointName.create("leetcode-editor.navigatorContribution");

    @NotNull SimpleToolWindowPanel createPanel(@NotNull ToolWindow toolWindow, @NotNull Project project);
}
