package com.shuzijun.leetcode.plugin.listener;

import com.intellij.openapi.project.Project;
import com.intellij.util.Alarm;
import com.shuzijun.leetcode.plugin.manager.NavigatorAction;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * @author shuzijun
 */
public class QueryKeyListener extends KeyAdapter implements DocumentListener {

    private static final int SEARCH_DELAY_MILLIS = 350;

    private final JTextField textField;
    private final NavigatorAction<?> navigatorAction;
    private final Project project;
    private final Alarm alarm;
    private boolean suppressChanges;

    public QueryKeyListener(JTextField textField, NavigatorAction<?> navigatorAction, Project project) {
        this.textField = textField;
        this.navigatorAction = navigatorAction;
        this.project = project;
        this.alarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, project);
        textField.getDocument().addDocumentListener(this);
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_ESCAPE && !textField.getText().isEmpty()) {
            textField.setText("");
            event.consume();
        } else if (event.getKeyCode() == KeyEvent.VK_ENTER) {
            scheduleSearch(0);
            event.consume();
        }
    }

    @Override
    public void insertUpdate(DocumentEvent event) {
        scheduleSearch(SEARCH_DELAY_MILLIS);
    }

    @Override
    public void removeUpdate(DocumentEvent event) {
        scheduleSearch(SEARCH_DELAY_MILLIS);
    }

    @Override
    public void changedUpdate(DocumentEvent event) {
        scheduleSearch(SEARCH_DELAY_MILLIS);
    }

    public void setTextSilently(String text) {
        suppressChanges = true;
        try {
            textField.setText(text);
        } finally {
            suppressChanges = false;
        }
        alarm.cancelAllRequests();
    }

    private void scheduleSearch(int delayMillis) {
        if (suppressChanges) {
            return;
        }
        alarm.cancelAllRequests();
        alarm.addRequest(() -> {
            if (project.isDisposed()) {
                return;
            }
            applySearch(navigatorAction, textField.getText());
            navigatorAction.loadServiceData();
        }, delayMillis);
    }

    static void applySearch(NavigatorAction<?> navigatorAction, String text) {
        String searchText = text == null ? "" : text.trim();
        navigatorAction.getPageInfo().disposeFilters(
                "searchKeywords",
                searchText,
                StringUtils.isNotBlank(searchText)
        );
        navigatorAction.getPageInfo().setPageIndex(1);
    }
}
