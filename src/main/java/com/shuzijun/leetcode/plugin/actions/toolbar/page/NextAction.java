package com.shuzijun.leetcode.plugin.actions.toolbar.page;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.manager.NavigatorAction;

/**
 * @author shuzijun
 */
public class NextAction extends AbstractPageAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent, NavigatorAction navigatorAction) {
        navigatorAction.getPagePanel().clickNext();
    }
}
