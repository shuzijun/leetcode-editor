package com.shuzijun.leetcode.plugin.window;

import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import icons.LeetCodeEditorIcons;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.swing.*;

/**
 * @author shuzijun
 */
public class WindowFactory implements ToolWindowFactory, DumbAware {

    public static String ID = PluginConstant.TOOL_WINDOW_ID;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        ContentFactory contentFactory = ContentFactory.getInstance();
        NavigatorTabsPanel navigatorPanel = new NavigatorTabsPanel(toolWindow, project);
        Content content = contentFactory.createContent(navigatorPanel, "", false);
        toolWindow.getContentManager().addContent(content);
        ApplicationManager.getApplication().getMessageBus().connect(navigatorPanel)
                .subscribe(ToolWindowManagerListener.TOPIC, new ToolWindowManagerListener() {
                    @Override
                    public void toolWindowShown(@NotNull ToolWindow shownToolWindow) {
                        if (shownToolWindow == toolWindow) {
                            navigatorPanel.refreshUser(project);
                        }
                    }
                });
        if (PersistentConfig.getInstance().getInitConfig() != null) {
            if (!PersistentConfig.getInstance().getInitConfig().getShowToolIcon()) {
                toolWindow.setIcon(LeetCodeEditorIcons.EMPEROR_NEW_CLOTHES);
            }
            if (!PersistentConfig.getInstance().getInitConfig().isLeftQuestionEditor()) {
                toolWindow.setAnchor(ToolWindowAnchor.RIGHT, null);
            }

        }
    }


    @NotNull
    public static PluginDataContext getDataContext(@Nullable Project project) {
        Project contextProject = project;
        if (contextProject == null) {
            Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
            if (openProjects.length != 1) {
                return PluginDataContext.EMPTY;
            }
            contextProject = openProjects[0];
        }
        ToolWindow leetcodeToolWindows = ToolWindowManager.getInstance(contextProject).getToolWindow(ID);
        if (leetcodeToolWindows == null) {
            return PluginDataContext.EMPTY;
        }
        ContentManager navigatorContentManager = leetcodeToolWindows.getContentManagerIfCreated();
        if (navigatorContentManager == null) {
            return PluginDataContext.EMPTY;
        }
        Content navigatorContent= navigatorContentManager.getContent(0);
        if (navigatorContent == null) {
            return PluginDataContext.EMPTY;
        }
        JComponent navigatorPanel = navigatorContent.getComponent();
        if (navigatorPanel instanceof NavigatorTabsPanel) {
            return new PluginDataContext((NavigatorTabsPanel) navigatorPanel);
        }
        return PluginDataContext.EMPTY;
    }

    public static void updateTitle(@NotNull Project project, String userName) {
        ToolWindow leetcodeToolWindows = ToolWindowManager.getInstance(project).getToolWindow(ID);
        ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed() || leetcodeToolWindows == null) {
                return;
            }
            if (StringUtils.isNotBlank(userName)) {
                leetcodeToolWindows.setTitle("[" + userName + "]");
            } else {
                leetcodeToolWindows.setTitle("");
            }
        }, ignored -> project.isDisposed());

    }

    public static void activateToolWindow(@NotNull Project project) {
        ToolWindow leetcodeToolWindows = ToolWindowManager.getInstance(project).getToolWindow(ID);
        leetcodeToolWindows.activate(null);
    }

    public static class PluginDataContext {
        private static final PluginDataContext EMPTY = new PluginDataContext(null);
        private final NavigatorTabsPanel navigatorTabsPanel;

        private PluginDataContext(@Nullable NavigatorTabsPanel navigatorTabsPanel) {
            this.navigatorTabsPanel = navigatorTabsPanel;
        }

        @Nullable
        @SuppressWarnings("unchecked")
        public <T> T getData(@NotNull DataKey<T> dataKey) {
            Object value = null;
            if (DataKeys.LEETCODE_PROJECTS_TABS.equals(dataKey)) {
                value = navigatorTabsPanel;
            } else if (DataKeys.LEETCODE_PROJECTS_NAVIGATORACTION.equals(dataKey)
                    && navigatorTabsPanel != null) {
                value = navigatorTabsPanel.getNavigatorAction();
            }
            return (T) value;
        }
    }
}
