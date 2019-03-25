package com.shuzijun.leetcode.plugin.listener;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBList;
import com.shuzijun.leetcode.plugin.model.MenuItem;
import com.shuzijun.leetcode.plugin.model.Question;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author shuzijun
 */
public class TreeMouse extends MouseAdapter {

    private JTree tree;

    private ToolWindow toolWindow;

    private Project project;

    public TreeMouse(JTree tree, ToolWindow toolWindow, Project project) {
        this.tree = tree;
        this.toolWindow = toolWindow;
        this.project = project;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
        if (selPath != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
            Question question = (Question) node.getUserObject();
            if (question.isLeaf()) {
                if (e.getButton() == 3) { //鼠标右键
                    JBList<MenuItem> list = new JBList<>();
                    MenuItem open = new MenuItem("open", new OpenMenuRunnable(node, toolWindow, project));
                    MenuItem submit = new MenuItem("submit", new SubmitMenuRunnable(question, toolWindow));
                    MenuItem test = new MenuItem("test", new TestMenuRunnable(question, toolWindow));
                    MenuItem clear = new MenuItem("clear", new ClearMenuRunnable(question, toolWindow));
                    MenuItem[] menuItems = new MenuItem[]{open, submit, test, clear};
                    list.setListData(menuItems);

                    JBPopup popup = new PopupChooserBuilder(list).setItemChoosenCallback(new Runnable() {
                        @Override
                        public void run() {
                            MenuItem value = list.getSelectedValue();
                            value.getRunnable().run();
                        }
                    }).createPopup();
                    Dimension dimension = popup.getContent().getPreferredSize();
                    popup.setSize(new Dimension(150, dimension.height));
                    // 显示
                    popup.show(new RelativePoint(e));
                } else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    new OpenMenuRunnable(node, toolWindow, project).run();
                }

            }
        }


    }

}
