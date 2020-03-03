package com.shuzijun.leetcode.plugin.actions.editor;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.manager.CodeManager;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.window.TestcasePanel;
import org.apache.commons.lang.StringUtils;

/**
 * @author shuzijun
 */
public class TestcaseAction extends AbstractEditAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config, Question question) {
        if (StringUtils.isBlank(question.getTestCase())) {
            String codeType = PersistentConfig.getInstance().getInitConfig().getCodeType();
            CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnum(codeType);

            CodeManager.setTestCaeAndLang(question, codeTypeEnum, anActionEvent.getProject());
        }
        TestcasePanel dialog = new TestcasePanel(anActionEvent.getProject());
        dialog.setTitle(question.getFormTitle() + " Testcase");
        dialog.setText(question.getTestCase());
        if (dialog.showAndGet()) {
            String text = dialog.testcaseText();
            if (StringUtils.isBlank(text)) {
                MessageUtils.getInstance(anActionEvent.getProject()).showWarnMsg("info", PropertiesUtils.getInfo("test.case"));
                return;
            } else {

                question.setTestCase(text);
                CodeManager.RuncodeCode(question, anActionEvent.getProject());
            }


        }
    }
}
