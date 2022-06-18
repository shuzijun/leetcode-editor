package com.shuzijun.leetcode.plugin.actions.tree;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.shuzijun.leetcode.plugin.manager.CodeManager;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.window.NavigatorTable;
import com.shuzijun.leetcode.plugin.window.TestcasePanel;

/**
 * @author shuzijun
 */
public class TestcaseAction extends AbstractTreeAction {
  @Override
  public void actionPerformed(AnActionEvent anActionEvent, Config config, NavigatorTable navigatorTable,
    Question question) {
    if (StringUtils.isBlank(question.getTestCase())) {
      String codeType = PersistentConfig.getInstance().getInitConfig().getCodeType();
      CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnum(codeType);

      CodeManager.setTestCaeAndLang(question, codeTypeEnum, anActionEvent.getProject());
    }
    AtomicReference<String> text = new AtomicReference<>(TestcaseAction.class.getName());
    ApplicationManager.getApplication().invokeAndWait(() -> {
      TestcasePanel dialog = new TestcasePanel(anActionEvent.getProject());
      dialog.setTitle(question.getFormTitle() + " Testcase");
      dialog.setText(question.getExampleTestcases());
      if (dialog.showAndGet()) {
        text.set(dialog.testcaseText());

      }
    });
    if (!TestcaseAction.class.getName().equals(text.get())) {
      if (StringUtils.isBlank(text.get())) {
        MessageUtils.getInstance(anActionEvent.getProject()).showWarnMsg("info", PropertiesUtils.getInfo("test.case"));
        return;
      } else {
        question.setTestCase(text.get());
        CodeManager.RunCodeCode(question, anActionEvent.getProject());
      }
    }

  }
}
