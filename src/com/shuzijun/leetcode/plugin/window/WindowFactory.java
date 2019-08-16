package com.shuzijun.leetcode.plugin.window;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.shuzijun.leetcode.plugin.listener.UpdatePluginListener;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author shuzijun
 */
public class WindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        JComponent navigatorPanel=  new NavigatorPanel(toolWindow,project);
        navigatorPanel.addAncestorListener(new UpdatePluginListener());
        Content content = contentFactory.createContent(navigatorPanel, "", false);
        toolWindow.getContentManager().addContent(content);

    }

}
