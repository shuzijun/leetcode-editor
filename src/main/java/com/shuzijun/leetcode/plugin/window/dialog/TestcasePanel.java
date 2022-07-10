package com.shuzijun.leetcode.plugin.window.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.shuzijun.leetcode.plugin.model.Question;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author shuzijun
 */
public class TestcasePanel extends DialogWrapper {

    private Question question;
    private JPanel jpanel;
    private JTextArea caseText;

    public TestcasePanel(@Nullable Project project, Question question) {
        super(project, true);
        this.question = question;
        jpanel = new JBPanel();
        jpanel.setLayout(new BorderLayout());
        caseText = new JTextArea();
        JBScrollPane caseTextScroll =  new JBScrollPane(caseText, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        caseTextScroll.setMinimumSize(new Dimension(400, 200));
        caseTextScroll.setPreferredSize(new Dimension(400, 200));
        jpanel.add(caseTextScroll, BorderLayout.CENTER);
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

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                caseText.setText(question.getExampleTestcases());
            }

            @Override
            public Object getValue(String key) {
                if(Action.NAME.equals(key)){
                    return "&Use Example Testcases";
                }else {
                    return super.getValue(key);
                }
            }
        },getOKAction(),getCancelAction()};
    }

    public String testcaseText() {
        return caseText.getText();
    }

    public void setText(String text) {
        caseText.setText(text);
    }
}
