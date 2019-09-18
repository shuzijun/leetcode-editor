package com.shuzijun.leetcode.plugin.listener;

import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBDimension;
import com.shuzijun.leetcode.plugin.renderer.ProblemsListRenderer;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;

/**
 * @author shuzijun
 */
public class QueryKeyListener implements KeyListener {

    private final static Logger logger = LoggerFactory.getLogger(QueryKeyListener.class);

    private JTextField jTextField;
    private JBScrollPane contentScrollPanel;
    private ToolWindow toolWindow;
    private JPanel queryPanel;
    private JBPopup jbPopup; // 搜索题目下拉弹窗

    public QueryKeyListener(JTextField jTextField, JBScrollPane contentScrollPanel, ToolWindow toolWindow, JPanel queryPanel) {
        this.jTextField = jTextField;
        this.contentScrollPanel = contentScrollPanel;
        this.toolWindow = toolWindow;
        this.queryPanel = queryPanel;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == KeyEvent.VK_ENTER) {

            String selectText = jTextField.getText();
            if (StringUtils.isBlank(selectText)) {
                if (jbPopup != null) {
                    jbPopup.dispose();
                }
                return;
            }

            JViewport viewport = contentScrollPanel.getViewport();
            JTree tree = (JTree) viewport.getView();

            DefaultTreeModel treeMode = (DefaultTreeModel) tree.getModel();
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeMode.getRoot();
            if (root.isLeaf() || root.getChildAt(0).isLeaf()) {
                MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.INFO, "info", "not question");
                if (jbPopup != null) {
                    jbPopup.dispose();
                }
                return;
            }

            DefaultMutableTreeNode all = (DefaultMutableTreeNode) root.getChildAt(0);

            // 找到all里面所有符合条件的叶子结点，即叶子结点的字符里面包括查找字符
            DefaultListModel<DefaultMutableTreeNode> data = new DefaultListModel<>();
            for (int i = 0; i < all.getChildCount(); i++) {
                DefaultMutableTreeNode singleProblem = (DefaultMutableTreeNode) all.getChildAt(i);
                if (singleProblem.isLeaf() && singleProblem.getUserObject().toString().toUpperCase().contains(selectText.toUpperCase())) {
                    data.addElement(singleProblem);
                }
            }
            showMatchedQuestions(tree, data);

            DefaultMutableTreeNode selectNode = null;
            TreePath selectionPathPath = tree.getSelectionPath();
            Boolean isSelectOne = Boolean.FALSE;
            if (selectionPathPath != null && all.isNodeChild((DefaultMutableTreeNode) selectionPathPath.getLastPathComponent())) {
                selectNode = (DefaultMutableTreeNode) selectionPathPath.getLastPathComponent();
            } else {
                selectNode = (DefaultMutableTreeNode) all.getChildAt(0);
                isSelectOne = Boolean.TRUE;
            }
            int index = all.getIndex(selectNode);

            for (int i = index + 1; i != index; i++) {
                if (isSelectOne) {
                    i = i - 1;
                    isSelectOne = Boolean.FALSE;
                }
                if (i >= all.getChildCount()) {
                    i = -1;
                    continue;
                }
                DefaultMutableTreeNode temp = (DefaultMutableTreeNode) all.getChildAt(i);
                if (temp.getUserObject().toString().toUpperCase().replace(" ","").contains(selectText.toUpperCase().replace(" ",""))) {
                    tree.setSelectionPath(new TreePath(temp.getPath()));
                    Point point = new Point(0, i < 3 ? 0 : (i - 3) * tree.getRowHeight());
                    viewport.setViewPosition(point);
                    return;
                }
            }
            MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.INFO, "info", "not find next");
            if (jbPopup != null) {
                jbPopup.dispose();
            }
            return;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    private void showMatchedQuestions(JTree tree, DefaultListModel<DefaultMutableTreeNode> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        if (jbPopup != null) { // 匹配新的关键词时，要把上一次的弹窗关闭。
            jbPopup.dispose();
        }
        JBList<DefaultMutableTreeNode> list = new JBList<>(data);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(-1);
        list.setToolTipText("Double click to jump");
        list.setCellRenderer(new ProblemsListRenderer());
        JBScrollPane listScroller = new JBScrollPane(list);
        jbPopup = JBPopupFactory.getInstance().createComponentPopupBuilder(listScroller, null).setTitle("Similar Problems").createPopup();
        jbPopup.setSize(new JBDimension(queryPanel.getWidth(), 150));
        // 双击选中题目，关闭popup并跳转到相应位置
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                JBList<DefaultMutableTreeNode> list = (JBList<DefaultMutableTreeNode>) e.getSource();
                if (e.getClickCount() == 2) {
                    int index = list.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        DefaultMutableTreeNode node = list.getModel().getElementAt(index);
                        jumpToTree(tree, node); // 跳转方法
                        jbPopup.dispose();
                    }
                }
            }
        });
        jbPopup.showUnderneathOf(queryPanel);
    }

    private void jumpToTree(JTree tree, DefaultMutableTreeNode node) {
        if (tree == null || node == null) {
            return;
        }
        TreePath path = new TreePath(node.getPath());
        tree.setSelectionPath(path);
        tree.scrollPathToVisible(path);
        JViewport viewport = contentScrollPanel.getViewport();
        int selectedRow = tree.getLeadSelectionRow();
        int height = selectedRow < 3 ? 0 : (selectedRow - 3) * tree.getRowHeight();
        Point point = new Point(0, height);
        viewport.setViewPosition(point);
    }
}
