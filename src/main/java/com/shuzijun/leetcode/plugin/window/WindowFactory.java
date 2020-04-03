package com.shuzijun.leetcode.plugin.window;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.shuzijun.leetcode.plugin.listener.UpdatePluginListener;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author shuzijun
 */
public class WindowFactory implements ToolWindowFactory {

    public static String ID = "leetcode";

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        JComponent navigatorPanel=  new NavigatorPanel(toolWindow,project);
        navigatorPanel.addAncestorListener(new UpdatePluginListener());
        Content content = contentFactory.createContent(navigatorPanel, "", false);
        toolWindow.getContentManager().addContent(content);

    }

    public static DataContext getDataContext(@NotNull Project project) {
        ToolWindow leetcodeToolWindows = ToolWindowManager.getInstance(project).getToolWindow(ID);
        DataContext dataContext = DataManager.getInstance().getDataContext(leetcodeToolWindows.getContentManager().getContent(0).getComponent());
        return dataContext;
    }

    public static void updateTitle(@NotNull Project project, String userName) {
        ToolWindow leetcodeToolWindows = ToolWindowManager.getInstance(project).getToolWindow(ID);
        if (StringUtils.isNotBlank(userName)) {
            leetcodeToolWindows.setTitle("[" + userName + "]");
        } else {
            leetcodeToolWindows.setTitle("");
        }
    }

}
