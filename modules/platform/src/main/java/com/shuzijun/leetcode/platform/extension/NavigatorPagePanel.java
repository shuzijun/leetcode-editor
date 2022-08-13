package com.shuzijun.leetcode.platform.extension;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBPanel;
import com.shuzijun.leetcode.plugin.model.PageInfo;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

/**
 * 导航窗口分页面板
 *
 * @author shuzijun
 */
public abstract class NavigatorPagePanel extends JBPanel {

    private JComboBox<Integer> pageSizeBox;
    private JButton previous;
    private JButton next;
    private JButton go;
    private JComboBox<Integer> page;

    public NavigatorPagePanel(Project project, PageInfo pageInfo) {
        super(new BorderLayout());
        pageSizeBox = new JComboBox(pageSizeData());
        pageSizeBox.setPreferredSize(new Dimension(60, -1));
        pageSizeBox.setSelectedItem(pageInfo.getPageSize());
        pageSizeBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                pageInfo.setPageSize((Integer) e.getItem());
            }
        });
        add(pageSizeBox, BorderLayout.WEST);

        JPanel control = new JPanel(new BorderLayout());
        previous = new JButton("<");
        previous.setToolTipText("Previous");
        previous.setPreferredSize(new Dimension(50, -1));
        previous.setMaximumSize(new Dimension(50, -1));
        previous.addActionListener(event -> {
            if (page.getItemCount() <= 0 || (int) page.getSelectedItem() < 2) {
            } else {
                pageInfo.setPageIndex((int) page.getSelectedItem() - 1);
                ProgressManager.getInstance().run(new Task.Backgroundable(project, "Previous", false) {
                    @Override
                    public void run(@NotNull ProgressIndicator progressIndicator) {
                        previousRunnable();
                    }
                });
            }

        });
        control.add(previous, BorderLayout.WEST);
        next = new JButton(">");
        next.setToolTipText("Next");
        next.setPreferredSize(new Dimension(50, -1));
        next.setMaximumSize(new Dimension(50, -1));
        next.addActionListener(event -> {
            if (page.getItemCount() <= 0 || (int) page.getSelectedItem() >= page.getItemCount()) {
                return;
            } else {
                pageInfo.setPageIndex((int) page.getSelectedItem() + 1);
                ProgressManager.getInstance().run(new Task.Backgroundable(project, "Next", false) {
                    @Override
                    public void run(@NotNull ProgressIndicator progressIndicator) {
                        nextRunnable();
                    }
                });
            }

        });
        control.add(next, BorderLayout.EAST);
        page = new JComboBox();
        control.add(page, BorderLayout.CENTER);
        add(control, BorderLayout.CENTER);

        go = new JButton("Go");
        go.setPreferredSize(new Dimension(50, -1));
        go.setMaximumSize(new Dimension(50, -1));
        go.addActionListener(event -> {
            if (page.getItemCount() <= 0) {
                return;
            } else {
                pageInfo.setPageIndex((int) page.getSelectedItem());
                ProgressManager.getInstance().run(new Task.Backgroundable(project, "Go to", false) {
                    @Override
                    public void run(@NotNull ProgressIndicator progressIndicator) {
                        goRunnable();
                    }
                });
            }

        });
        add(go, BorderLayout.EAST);
    }

    public abstract Integer[] pageSizeData();

    public abstract void previousRunnable();

    public abstract void nextRunnable();

    public abstract void goRunnable();

    public int getPageIndex() {
        if (page.getItemCount() <= 0) {
            return 1;
        } else {
            return (int) page.getSelectedItem();
        }
    }

    public void focusedPageSize() {
        pageSizeBox.requestFocusInWindow();
    }

    public void focusedPage() {
        page.requestFocusInWindow();
    }

    public void clickPrevious() {
        previous.doClick();
    }

    public void clickNext() {
        next.doClick();
    }

    public void clickGo() {
        go.doClick();
    }

    public JComboBox<Integer> getPageSizeBox() {
        return pageSizeBox;
    }

    public JButton getPrevious() {
        return previous;
    }

    public JButton getNext() {
        return next;
    }

    public JButton getGo() {
        return go;
    }

    public JComboBox<Integer> getPage() {
        return page;
    }
}
