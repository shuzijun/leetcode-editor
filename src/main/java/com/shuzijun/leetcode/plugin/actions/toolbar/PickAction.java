package com.shuzijun.leetcode.plugin.actions.toolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.shuzijun.leetcode.platform.extension.NavigatorAction;
import com.shuzijun.leetcode.plugin.actions.AbstractAction;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.service.RepositoryServiceImpl;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.window.WindowFactory;

/**
 * @author shuzijun
 */
public class PickAction extends AbstractAction implements DumbAware {
    private int i = 0;

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config) {
        NavigatorAction navigatorAction = WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_NAVIGATORACTION);
        RepositoryServiceImpl.getInstance(anActionEvent.getProject()).getViewService().pick(navigatorAction.getPageInfo());
    }
}
