package com.shuzijun.leetcode.plugin.listener;

import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * @author shuzijun
 */
public class QueryKeyListener implements KeyListener {

    private final static Logger logger = LoggerFactory.getLogger(QueryKeyListener.class);

    private JTextField jTextField;
    private JBScrollPane contentScrollPanel;
    private ToolWindow toolWindow;

    public QueryKeyListener(JTextField jTextField, JBScrollPane contentScrollPanel, ToolWindow toolWindow) {
        this.jTextField = jTextField;
        this.contentScrollPanel = contentScrollPanel;
        this.toolWindow = toolWindow;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == KeyEvent.VK_ENTER) {

            String selectText = jTextField.getText();
            if (StringUtils.isBlank(selectText)) {
                return;
            }

            JViewport viewport = contentScrollPanel.getViewport();
            JTree tree = (JTree) viewport.getView();

            DefaultTreeModel treeMode = (DefaultTreeModel) tree.getModel();
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeMode.getRoot();
            if (root.isLeaf() || root.getChildAt(0).isLeaf()) {
                MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.INFO, "info", "not question");
                return;
            }

            DefaultMutableTreeNode all = (DefaultMutableTreeNode) root.getChildAt(0);
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
            return;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
