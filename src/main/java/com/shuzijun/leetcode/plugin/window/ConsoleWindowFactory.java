package com.shuzijun.leetcode.plugin.window;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import icons.LeetCodeEditorIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author shuzijun
 */
public class ConsoleWindowFactory implements ToolWindowFactory, DumbAware {

    public static String ID = PluginConstant.CONSOLE_WINDOW_ID;


    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        ConsolePanel consolePanel = new ConsolePanel(toolWindow, project);
        Content content = toolWindow.getContentManager().getFactory().createContent(consolePanel, "", true);
        toolWindow.getContentManager().addContent(content);
        if (PersistentConfig.getInstance().getInitConfig() != null && !PersistentConfig.getInstance().getInitConfig().getShowToolIcon()) {
            toolWindow.setIcon(LeetCodeEditorIcons.EMPEROR_NEW_CLOTHES);
        }
    }

    public static DataContext getDataContext(@NotNull Project project) {
        ToolWindow leetcodeToolWindows = ToolWindowManager.getInstance(project).getToolWindow(ID);
        ConsolePanel consolePanel = (ConsolePanel) leetcodeToolWindows.getContentManager().getContent(0).getComponent();
        return DataManager.getInstance().getDataContext(consolePanel);
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return false;
    }
}
