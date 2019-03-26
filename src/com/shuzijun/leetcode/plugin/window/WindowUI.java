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
import java.awt.*;


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


        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new Question("root"));
        JTree tree = new Tree(new DefaultTreeModel(root));
        tree.setOpaque(false);
        tree.setCellRenderer(new CustomTreeCellRenderer());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setRootVisible(false);
        tree.addMouseListener(new TreeMouse(tree, toolWindow, project));
        tree.addTreeWillExpandListener(new TreeeWillListener(tree,toolWindow));
        contentScrollPanel = new JBScrollPane(tree, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        contentScrollPanel.setName("contentScrollPanel");

        rootJPanel.add(createHeaderPanel(toolWindow)); // Header
        rootJPanel.add(createQueryPanel(toolWindow)); // query
        rootJPanel.add(Box.createVerticalStrut(5));
        rootJPanel.add(contentScrollPanel); // Content scroll


    }

    private JPanel createHeaderPanel(ToolWindow toolWindow) {
        final JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));


        JButton loginButton = new JButton();
        loginButton.setIcon(new ImageIcon(getClass().getResource("/image/login16.png")));
        loginButton.setPreferredSize(new Dimension(40,30) );
        loginButton.setMaximumSize(new Dimension(40,30) );
        loginButton.setToolTipText("Sign in");
        loginButton.addActionListener(new LoginListener(toolWindow, contentScrollPanel));

        JButton outButton = new JButton();
        outButton.setIcon(new ImageIcon(getClass().getResource("/image/out16.png")));
        outButton.setPreferredSize(new Dimension(40,30) );
        outButton.setMaximumSize(new Dimension(40,30) );
        outButton.setToolTipText("Sign out");
        outButton.addActionListener(new LoginOutListener(toolWindow));


        /*JButton downButton = new JButton();
        downButton.setIcon(new ImageIcon(getClass().getResource("/image/down16.png")));
        downButton.setToolTipText("down");*/


        JButton loadButton = new JButton();
        loadButton.setIcon(new ImageIcon(getClass().getResource("/image/load16.png")));
        loadButton.setPreferredSize(new Dimension(40,30) );
        loadButton.setMaximumSize(new Dimension(40,30) );
        loadButton.setToolTipText("Load question");
        loadButton.addActionListener(new LoadListener(toolWindow, contentScrollPanel));


        JButton clearButton = new JButton();
        clearButton.setIcon(new ImageIcon(getClass().getResource("/image/delete16.png")));
        clearButton.setPreferredSize(new Dimension(40,30) );
        clearButton.setMaximumSize(new Dimension(40,30) );
        clearButton.setToolTipText("Clear Cache");
        clearButton.addActionListener(new ClearListener(toolWindow));

        headerPanel.add(loginButton);
        headerPanel.add(outButton);
        //headerPanel.add(downButton);
        headerPanel.add(loadButton);
        headerPanel.add(clearButton);

        return headerPanel;
    }

    private JPanel createQueryPanel(ToolWindow toolWindow) {
        final JPanel queryPanel = new JPanel();
        queryPanel.setLayout(new BoxLayout(queryPanel, BoxLayout.X_AXIS));
        queryPanel.setMinimumSize(new Dimension(160, 35));

        JTextField queryField = new JTextField(8);
        queryField.setMaximumSize(new Dimension(170, 30));
        queryField.setToolTipText("Enter Search");
        queryField.addKeyListener(new QueryKeyListener(queryField, contentScrollPanel, toolWindow));
        queryPanel.add(queryField);

/*        JLabel jLabel = new JBLabel(new ImageIcon(getClass().getResource("/image/search16.png")));
        jLabel.setToolTipText("Enter Query");
        queryPanel.add(jLabel);*/
        return queryPanel;
    }

    public JPanel getContent() {
        return rootJPanel;
    }

    public JBScrollPane getContentScrollPanel() {
        return contentScrollPanel;
    }
}
