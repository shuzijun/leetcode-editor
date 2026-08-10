package com.shuzijun.leetcode.plugin.window;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.shuzijun.leetcode.plugin.product.ProductProfiles;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import icons.LeetCodeEditorIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author shuzijun
 */
public class ConsoleWindowFactory implements ToolWindowFactory, DumbAware {

    @NotNull
    public static String id() {
        return ProductProfiles.current().consoleToolWindowId();
    }


    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        ConsolePanel consolePanel = new ConsolePanel(toolWindow, project);
        Content content = toolWindow.getContentManager().getFactory().createContent(consolePanel, "", true);
        toolWindow.getContentManager().addContent(content);
        if (PersistentConfig.getInstance().getInitConfig() != null && !PersistentConfig.getInstance().getInitConfig().getShowToolIcon()) {
            toolWindow.setIcon(LeetCodeEditorIcons.EMPEROR_NEW_CLOTHES);
        }
    }

    public static ConsoleView getConsoleView(@NotNull Project project) {
        ToolWindow leetcodeToolWindows = ToolWindowManager.getInstance(project).getToolWindow(id());
        if (leetcodeToolWindows == null) {
            return null;
        }
        if (leetcodeToolWindows.getContentManagerIfCreated() == null) {
            return null;
        }
        Content content = leetcodeToolWindows.getContentManagerIfCreated().getContent(0);
        if (content == null || !(content.getComponent() instanceof ConsolePanel)) {
            return null;
        }
        return ((ConsolePanel) content.getComponent()).getConsoleView();
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return false;
    }
}
