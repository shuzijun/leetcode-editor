package com.shuzijun.leetcode.plugin.window;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.shuzijun.leetcode.plugin.model.PluginConstant;

import javax.swing.*;
import java.awt.*;

/**
 * @author shuzijun
 */
public class ConsolePanel extends SimpleToolWindowPanel implements Disposable {

    private final ConsoleView consoleView;

    public ConsolePanel(ToolWindow toolWindow, Project project) {
        super(Boolean.FALSE, Boolean.TRUE);
        this.consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        setContent(createContent());
        final DefaultActionGroup consoleGroup = new DefaultActionGroup(consoleView.createConsoleActions());
        ActionToolbar consoleToolbar = ActionManager.getInstance().createActionToolbar(PluginConstant.ACTION_PREFIX + " ConsoleToolbar", consoleGroup, true);
        consoleToolbar.setTargetComponent(consoleView.getComponent());
        setToolbar(consoleToolbar.getComponent());
    }

    private JComponent createContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.add(createHeader(), BorderLayout.NORTH);
        content.add(consoleView.getComponent(), BorderLayout.CENTER);
        return content;
    }

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout(JBUI.scale(8), 0));
        header.setBorder(JBUI.Borders.empty(6, 10, 6, 8));
        header.setBackground(UIUtil.getPanelBackground());

        JBLabel title = new JBLabel("LeetCode Output");
        title.setFont(JBUI.Fonts.label().deriveFont(Font.BOLD));
        header.add(title, BorderLayout.WEST);

        JBLabel description = new JBLabel("Run, submit, and sync results");
        description.setForeground(UIUtil.getContextHelpForeground());
        header.add(description, BorderLayout.EAST);
        return header;
    }

    public ConsoleView getConsoleView() {
        return consoleView;
    }

    @Override
    public void dispose() {
        Disposer.dispose(consoleView);
    }
}
