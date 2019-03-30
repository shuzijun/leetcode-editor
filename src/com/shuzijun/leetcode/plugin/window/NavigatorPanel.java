package com.shuzijun.leetcode.plugin.window;


import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.SimpleTree;
import com.shuzijun.leetcode.plugin.listener.QueryKeyListener;
import com.shuzijun.leetcode.plugin.listener.TreeMouseListener;
import com.shuzijun.leetcode.plugin.listener.TreeeWillListener;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.renderer.CustomTreeCellRenderer;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

/**
 * @author shuzijun
 */
public class NavigatorPanel extends SimpleToolWindowPanel implements DataProvider {


    private JPanel queryPanel;
    private SimpleTree tree;

    public NavigatorPanel(ToolWindow toolWindow, Project project) {
        super(Boolean.TRUE, Boolean.TRUE);
        final ActionManager actionManager = ActionManager.getInstance();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new Question("root"));
        tree = new SimpleTree(new DefaultTreeModel(root)) {

            private final JTextPane myPane = new JTextPane();

            {
                myPane.setOpaque(false);
                String addIconText = "'login'";
                String refreshIconText = "'refresh'";
                String message = PropertiesUtils.getInfo("config.load", addIconText, refreshIconText);
                int addIconMarkerIndex = message.indexOf(addIconText);
                myPane.replaceSelection(message.substring(0, addIconMarkerIndex));
                myPane.insertIcon(AllIcons.General.Web);
                int refreshIconMarkerIndex = message.indexOf(refreshIconText);
                myPane.replaceSelection(message.substring(addIconMarkerIndex + addIconText.length(), refreshIconMarkerIndex));
                myPane.insertIcon(AllIcons.Actions.Refresh);
                myPane.replaceSelection(message.substring(refreshIconMarkerIndex + refreshIconText.length()));

            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
                if (!root.isLeaf()) {
                    return;
                }

                myPane.setFont(getFont());
                myPane.setBackground(getBackground());
                myPane.setForeground(getForeground());
                Rectangle bounds = getBounds();
                myPane.setBounds(0, 0, bounds.width - 10, bounds.height);

                Graphics g2 = g.create(bounds.x + 10, bounds.y + 20, bounds.width, bounds.height);
                try {
                    myPane.paint(g2);
                } finally {
                    g2.dispose();
                }
            }
        };
        tree.getEmptyText().clear();
        tree.setOpaque(false);
        tree.setCellRenderer(new CustomTreeCellRenderer());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setRootVisible(false);
        tree.addMouseListener(new TreeMouseListener(tree,project));
        tree.addTreeWillExpandListener(new TreeeWillListener(tree, toolWindow));


        ActionToolbar actionToolbar = actionManager.createActionToolbar("leetcode Toolbar",
                (DefaultActionGroup) actionManager.getAction("leetcode.NavigatorActionsToolbar"),
                true);

        actionToolbar.setTargetComponent(tree);
        setToolbar(actionToolbar.getComponent());

        SimpleToolWindowPanel treePanel = new SimpleToolWindowPanel(Boolean.TRUE, Boolean.TRUE);

        JPanel groupPanel = new JPanel();
        groupPanel.setLayout(new BoxLayout(groupPanel, BoxLayout.Y_AXIS));
        JBScrollPane contentScrollPanel = new JBScrollPane(tree, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        groupPanel.add(contentScrollPanel);

       /* JTextArea Submissions=new JTextArea();
        JBScrollPane logTextScrollPanelScrollPanel = new JBScrollPane(Submissions, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        groupPanel.add(logTextScrollPanelScrollPanel);*/

      /*  JTextArea logText =new JTextArea();
        logText.setEditable(false);
        logTextScrollPanel = new JBScrollPane(logText, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER){
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(treePanel.HEIGHT, 50);//括号内参数，可以根据需要更改
            }
        };
        logTextScrollPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        logTextScrollPanel.setVisible(false);
        groupPanel.add(logTextScrollPanel);
*/
        treePanel.setContent(groupPanel);

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
