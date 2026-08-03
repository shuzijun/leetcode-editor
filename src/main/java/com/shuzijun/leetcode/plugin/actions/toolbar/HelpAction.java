package com.shuzijun.leetcode.plugin.actions.toolbar;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.shuzijun.leetcode.plugin.utils.BrowserUtils;


/**
 * @author shuzijun
 */
public class HelpAction extends AnAction implements DumbAware {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        BrowserUtils.browse("https://github.com/shuzijun/leetcode-editor");
    }

}
