package com.shuzijun.leetcode.plugin.actions;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.model.Config;

/**
 * @author shuzijun
 */
public class HelpAction extends AbstractAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config) {
        BrowserUtil.browse("https://github.com/shuzijun/leetcode-editor");
    }

}
