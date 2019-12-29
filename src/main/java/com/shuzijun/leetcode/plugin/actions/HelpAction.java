package com.shuzijun.leetcode.plugin.actions;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;


/**
 * @author shuzijun
 */
public class HelpAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        BrowserUtil.browse("https://github.com/shuzijun/leetcode-editor");
    }

}
