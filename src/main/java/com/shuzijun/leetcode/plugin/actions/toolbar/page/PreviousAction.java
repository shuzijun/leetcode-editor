package com.shuzijun.leetcode.plugin.actions.toolbar.page;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.platform.extension.NavigatorAction;

/**
 * @author shuzijun
 */
public class PreviousAction extends AbstractPageAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent, NavigatorAction navigatorAction) {
        navigatorAction.getPagePanel().clickPrevious();
    }
}
