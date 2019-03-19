package com.shuzijun.leetcode.plugin.window;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.shuzijun.leetcode.plugin.listener.*;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.renderer.CustomTreeCellRenderer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

/**
 * @author shuzijun
 */
public class WindowUI {

    private Project project;
    private ToolWindow toolWindow;

    private JPanel rootJPanel;
    private JBScrollPane contentScrollPanel;

    public WindowUI(ToolWindow toolWindow, Project project) {

        this.project = project;
        this.initUI(toolWindow);
    }

    public void initUI(ToolWindow toolWindow) {
        // create UI
        rootJPanel = new JPanel();
        rootJPanel.setLayout(new BoxLayout(rootJPanel, BoxLayout.Y_AXIS));


        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new Question("根节点"));
        JTree tree = new Tree(new DefaultTreeModel(root));
        tree.setOpaque(false);
        tree.setCellRenderer(new CustomTreeCellRenderer());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setRootVisible(false);
        tree.addMouseListener(new TreeMouse(tree, toolWindow, project));
        contentScrollPanel = new JBScrollPane(tree, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        contentScrollPanel.setName("contentScrollPanel");

        rootJPanel.add(createHeaderPanel(toolWindow)); // Header
        rootJPanel.add(contentScrollPanel); // Content scroll


    }

    private JPanel createHeaderPanel(ToolWindow toolWindow) {
        final JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));


        JButton loginButton = new JButton();
        loginButton.setIcon(new ImageIcon(getClass().getResource("/image/login16.png")));
        loginButton.setToolTipText("login");
        loginButton.addActionListener(new LoginListener(toolWindow, contentScrollPanel));

        JButton outButton = new JButton();
        outButton.setIcon(new ImageIcon(getClass().getResource("/image/out16.png")));
        outButton.setToolTipText("loginOut");
        outButton.addActionListener(new LoginOutListener(toolWindow));


        /*JButton downButton = new JButton();
        downButton.setIcon(new ImageIcon(getClass().getResource("/image/down16.png")));
        downButton.setToolTipText("down");*/


        JButton loadButton = new JButton();
        loadButton.setIcon(new ImageIcon(getClass().getResource("/image/load16.png")));
        loadButton.setToolTipText("load question");
        loadButton.addActionListener(new LoadListener(toolWindow, contentScrollPanel));


        JButton clearButton = new JButton();
        clearButton.setIcon(new ImageIcon(getClass().getResource("/image/delete16.png")));
        clearButton.setToolTipText("缓存");
        clearButton.addActionListener(new ClearListener(toolWindow));

        headerPanel.add(loginButton);
        headerPanel.add(outButton);
        //headerPanel.add(downButton);
        headerPanel.add(loadButton);
        headerPanel.add(clearButton);

        return headerPanel;
    }


    public JPanel getContent() {
        return rootJPanel;
    }

    public JBScrollPane getContentScrollPanel() {
        return contentScrollPanel;
    }
}
