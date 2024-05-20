package com.shuzijun.leetcode.plugin.window;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
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

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        JComponent navigatorPanel = new NavigatorTabsPanel(toolWindow, project);
        Content content = contentFactory.createContent(navigatorPanel, "", false);
        toolWindow.getContentManager().addContent(content);
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
    public static DataContext getDataContext(@NotNull Project project) {
        ToolWindow leetcodeToolWindows = ToolWindowManager.getInstance(project).getToolWindow(ID);
        if (leetcodeToolWindows == null) {
            return DataContext.EMPTY_CONTEXT;
        }
        ContentManager navigatorContentManager = leetcodeToolWindows.getContentManagerIfCreated();
        if (navigatorContentManager == null) {
            return DataContext.EMPTY_CONTEXT;
        }
        Content navigatorContent= navigatorContentManager.getContent(0);
        if (navigatorContent == null) {
            return DataContext.EMPTY_CONTEXT;
        }
        JComponent navigatorPanel =  navigatorContent.getComponent();
        if (navigatorPanel instanceof DataProvider){
            return new MyDataContext((DataProvider) navigatorPanel);
        }

        return DataContext.EMPTY_CONTEXT;
    }

    public static void updateTitle(@NotNull Project project, String userName) {
        ToolWindow leetcodeToolWindows = ToolWindowManager.getInstance(project).getToolWindow(ID);
        ApplicationManager.getApplication().invokeLater(() -> {
            if (StringUtils.isNotBlank(userName)) {
                leetcodeToolWindows.setTitle("[" + userName + "]");
            } else {
                leetcodeToolWindows.setTitle("");
            }
        });

    }

    public static void activateToolWindow(@NotNull Project project) {
        ToolWindow leetcodeToolWindows = ToolWindowManager.getInstance(project).getToolWindow(ID);
        leetcodeToolWindows.activate(null);
    }

    public static class  MyDataContext implements DataContext{
        private final DataProvider dataProvider;

        public MyDataContext(DataProvider dataProvider) {
            this.dataProvider = dataProvider;
        }

        @Override
        public @Nullable Object getData(@NotNull String dataId) {
            return dataProvider.getData(dataId);
        }
    }

}
