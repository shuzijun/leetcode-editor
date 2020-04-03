package com.shuzijun.leetcode.plugin.window;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTextField;
import com.shuzijun.leetcode.plugin.model.Session;

import javax.swing.*;
import java.util.List;

/**
 * @author shuzijun
 */
public class ProgressPanel {
    private JPanel mainPanel;
    private JComboBox sessionBox;
    private JBTextField todoField;
    private JBTextField solvedField;
    private JBTextField attemptedField;
    private JBTextField easyField;
    private JBTextField mediumField;
    private JBTextField hardField;
    private JBTextField expField;
    private JBTextField pointField;

    private Project project;

    public ProgressPanel(List<Session> sessionList, Project project) {
        this.project = project;
        initUI(sessionList);
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public Session select(){
      return (Session) sessionBox.getSelectedItem();
    }

    public void update(List<Session> sessionList) {
        initUI(sessionList);
    }

    private void initUI(List<Session> sessionList) {
        sessionBox.removeAll();
        for (Session session : sessionList) {
            sessionBox.addItem(session);
        }
        Session session = sessionList.get(0);
        todoField.setText(String.valueOf(session.getUnsolved()));
        solvedField.setText(session.getSolvedTotal() + "/" + session.getQuestionTotal());
        attemptedField.setText(String.valueOf(session.getAttempted()));
        easyField.setText(String.valueOf(session.getEasy()));
        mediumField.setText(String.valueOf(session.getMedium()));
        hardField.setText(String.valueOf(session.getHard()));
        expField.setText(String.valueOf(session.getXP()));
        pointField.setText(String.valueOf(session.getPoint()));
    }
}
