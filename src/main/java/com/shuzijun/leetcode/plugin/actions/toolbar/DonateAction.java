package com.shuzijun.leetcode.plugin.actions.toolbar;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.utils.BrowserUtils;


/**
 * @author shuzijun
 */
public class DonateAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        BrowserUtils.browse("https://shuzijun.cn/donate.html");
    }

}
