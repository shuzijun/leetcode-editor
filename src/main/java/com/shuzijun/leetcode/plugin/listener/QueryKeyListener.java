package com.shuzijun.leetcode.plugin.listener;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.manager.NavigatorAction;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * @author shuzijun
 */
public class QueryKeyListener implements KeyListener {

    private final static Logger logger = LoggerFactory.getLogger(QueryKeyListener.class);

    private JTextField jTextField;
    private NavigatorAction navigatorAction;
    private Project project;

    public QueryKeyListener(JTextField jTextField, NavigatorAction navigatorAction, Project project) {
        this.jTextField = jTextField;
        this.navigatorAction = navigatorAction;
        this.project = project;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == KeyEvent.VK_ENTER) {
            ProgressManager.getInstance().run(new Task.Backgroundable(project, "Search", false) {
                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    String selectText = jTextField.getText();
                    navigatorAction.getPageInfo().disposeFilters("searchKeywords", selectText, StringUtils.isNotBlank(selectText));
                    navigatorAction.getPageInfo().setPageIndex(1);
                    navigatorAction.loadServiceData();
                }
            });
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
