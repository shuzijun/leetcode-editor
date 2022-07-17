package com.shuzijun.leetcode.plugin.actions.toolbar;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;


/**
 * @author shuzijun
 */
public class HelpAction extends AnAction implements DumbAware {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        BrowserUtil.browse("https://github.com/shuzijun/leetcode-editor");
    }

}
