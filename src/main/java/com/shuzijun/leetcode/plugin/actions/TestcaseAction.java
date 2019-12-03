package com.shuzijun.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.shuzijun.leetcode.plugin.manager.CodeManager;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

/**
 * @author shuzijun
 */
public class TestcaseAction extends AbstractAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config) {

        JTree tree = anActionEvent.getData(DataKeys.LEETCODE_PROJECTS_TREE);
        DefaultMutableTreeNode note = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        Question question = (Question) note.getUserObject();

        if (StringUtils.isBlank(question.getTestCase())) {
            String codeType = PersistentConfig.getInstance().getInitConfig().getCodeType();
            CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnum(codeType);

            CodeManager.setTestCaeAndLang(question, codeTypeEnum);
        }
        TestcasePanel dialog = new TestcasePanel(anActionEvent.getProject());
        dialog.setTitle(question.getFormTitle() + " Testcase");
        dialog.setText(question.getTestCase());
        if (dialog.showAndGet()) {
            String text = dialog.testcaeText();
            if (StringUtils.isBlank(text)) {
                MessageUtils.showWarnMsg("info", PropertiesUtils.getInfo("test.case"));
                return;
            } else {

                question.setTestCase(text);
                CodeManager.RuncodeCode(question);
            }


        }
    }

    private class TestcasePanel extends DialogWrapper {


        private JPanel jpanel;
        private JTextArea caseText;

        protected TestcasePanel(@Nullable Project project) {
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
}
