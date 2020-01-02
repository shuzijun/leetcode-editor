package com.shuzijun.leetcode.plugin.window;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @author shuzijun
 */
public class TestcasePanel extends DialogWrapper {


    private JPanel jpanel;
    private JTextArea caseText;

    public TestcasePanel(@Nullable Project project) {
        super(project, true);
        jpanel = new JBPanel();
        caseText = new JTextArea();
        caseText.setMinimumSize(new Dimension(400, 200));
        caseText.setPreferredSize(new Dimension(400, 200));
        jpanel.add(new JBScrollPane(caseText, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

        setModal(true);
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return jpanel;
    }

    @NotNull
    @Override
    protected Action getOKAction() {
        Action action = super.getOKAction();
        action.putValue(Action.NAME, "&Run code");
        return action;
    }

    public String testcaeText() {
        return caseText.getText();
    }

    public void setText(String text) {
        caseText.setText(text);
    }
}
