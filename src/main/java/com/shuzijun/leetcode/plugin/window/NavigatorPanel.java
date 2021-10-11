package com.shuzijun.leetcode.plugin.window;


import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBTextField;
import com.shuzijun.leetcode.plugin.listener.QueryKeyListener;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.utils.DataKeys;

import javax.swing.*;

/**
 * @author shuzijun
 */
public class NavigatorPanel extends SimpleToolWindowPanel implements DataProvider {


    private JPanel queryPanel;
    private NavigatorTable navigatorTable;
    private ActionToolbar findToolbar;
    private ActionToolbar actionSortToolbar;

    public NavigatorPanel(ToolWindow toolWindow, Project project) {
        super(Boolean.TRUE, Boolean.TRUE);
        final ActionManager actionManager = ActionManager.getInstance();

        navigatorTable = new NavigatorTable(project);

        ActionToolbar actionToolbar = actionManager.createActionToolbar(PluginConstant.ACTION_PREFIX + " Toolbar",
                (DefaultActionGroup) actionManager.getAction(PluginConstant.LEETCODE_NAVIGATOR_ACTIONS_TOOLBAR),
                true);
        actionToolbar.setTargetComponent(navigatorTable);
        setToolbar(actionToolbar.getComponent());

        SimpleToolWindowPanel toolWindowPanel = new SimpleToolWindowPanel(Boolean.TRUE, Boolean.TRUE);

        toolWindowPanel.setContent(navigatorTable);

        queryPanel = new JPanel();
        queryPanel.setLayout(new BoxLayout(queryPanel, BoxLayout.Y_AXIS));
        JTextField queryField = new JBTextField();
        queryField.setToolTipText("Enter Search");
        queryField.addKeyListener(new QueryKeyListener(queryField, navigatorTable, project));
        queryPanel.add(queryField);

        findToolbar = actionManager.createActionToolbar(PluginConstant.LEETCODE_FIND_TOOLBAR,
                (DefaultActionGroup) actionManager.getAction(PluginConstant.LEETCODE_FIND_TOOLBAR),
                true);
        findToolbar.setTargetComponent(navigatorTable);
        actionSortToolbar = actionManager.createActionToolbar(PluginConstant.LEETCODE_FIND_SORT_TOOLBAR,
                (DefaultActionGroup) actionManager.getAction(PluginConstant.LEETCODE_FIND_SORT_TOOLBAR),
                true);
        actionSortToolbar.setTargetComponent(navigatorTable);
        queryPanel.add(findToolbar.getComponent());
        queryPanel.add(actionSortToolbar.getComponent());

        queryPanel.setVisible(false);
        toolWindowPanel.setToolbar(queryPanel);
        setContent(toolWindowPanel);

    }

    @Override
    public Object getData(String dataId) {
        if (DataKeys.LEETCODE_PROJECTS_TREE.is(dataId)) {
            return navigatorTable;
        }

        if (DataKeys.LEETCODE_PROJECTS_TERRFIND.is(dataId)) {
            return queryPanel;
        }

        if (DataKeys.LEETCODE_TOOLBAR_FIND.is(dataId)) {
            return findToolbar;
        }
        if (DataKeys.LEETCODE_TOOLBAR_SORT.is(dataId)) {
            return actionSortToolbar;
        }
        return super.getData(dataId);
    }
}
