package com.shuzijun.leetcode.plugin.window;


import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.SimpleTree;
import com.shuzijun.leetcode.plugin.listener.QueryKeyListener;
import com.shuzijun.leetcode.plugin.listener.TreeMouse;
import com.shuzijun.leetcode.plugin.listener.TreeeWillListener;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.renderer.CustomTreeCellRenderer;
import com.shuzijun.leetcode.plugin.utils.DataKeys;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

/**
 * @author shuzijun
 */
public class NavigatorPanel extends SimpleToolWindowPanel implements DataProvider {


    private JPanel queryPanel;
    private JTree tree;

    public NavigatorPanel(ToolWindow toolWindow, Project project) {
        super(Boolean.TRUE, Boolean.TRUE);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new Question("root"));
        tree = new SimpleTree(new DefaultTreeModel(root));
        tree.setOpaque(false);
        tree.setCellRenderer(new CustomTreeCellRenderer());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setRootVisible(false);
        tree.addMouseListener(new TreeMouse(tree, toolWindow, project));
        tree.addTreeWillExpandListener(new TreeeWillListener(tree, toolWindow));

        final ActionManager actionManager = ActionManager.getInstance();
        ActionToolbar actionToolbar = actionManager.createActionToolbar("leetcode Toolbar",
                (DefaultActionGroup) actionManager.getAction("leetcode.NavigatorActionsToolbar"),
                true);

        actionToolbar.setTargetComponent(tree);
        setToolbar(actionToolbar.getComponent());

        SimpleToolWindowPanel treePanel = new SimpleToolWindowPanel(Boolean.TRUE, Boolean.TRUE);
        JBScrollPane contentScrollPanel = new JBScrollPane(tree, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        treePanel.setContent(contentScrollPanel);
        queryPanel = new JPanel();
        queryPanel.setLayout(new BoxLayout(queryPanel, BoxLayout.X_AXIS));

        JTextField queryField = new JTextField();
        queryField.setToolTipText("Enter Search");
        queryField.addKeyListener(new QueryKeyListener(queryField, contentScrollPanel, toolWindow));
        queryPanel.add(queryField);
        queryPanel.setVisible(false);
        treePanel.setToolbar(queryPanel);
        setContent(treePanel);

    }


    @Override
    public Object getData(String dataId) {
        if (DataKeys.LEETCODE_PROJECTS_TREE.is(dataId)) {
            return tree;
        }

        if (DataKeys.LEETCODE_PROJECTS_TERRFIND.is(dataId)) {
            return queryPanel;
        }
        return super.getData(dataId);
    }
}
