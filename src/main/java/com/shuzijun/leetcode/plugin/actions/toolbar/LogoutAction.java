package com.shuzijun.leetcode.plugin.actions.toolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.shuzijun.leetcode.plugin.actions.AbstractAction;
import com.shuzijun.leetcode.plugin.listener.LoginNotifier;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.HttpRequest;
import com.shuzijun.leetcode.plugin.utils.*;
import com.shuzijun.leetcode.plugin.window.NavigatorTabsPanel;

/**
 * @author shuzijun
 */
public class LogoutAction extends AbstractAction implements DumbAware {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config) {

        HttpResponse httpResponse = HttpRequest.builderGet(URLUtils.getLeetcodeLogout()).request();
        HttpRequestUtils.resetHttpclient();
        MessageUtils.getInstance(anActionEvent.getProject()).showInfoMsg("info", PropertiesUtils.getInfo("login.out"));
        NavigatorTabsPanel.loadUser(false);
        ApplicationManager.getApplication().getMessageBus().syncPublisher(LoginNotifier.TOPIC).logout(anActionEvent.getProject(), config.getUrl());
    }
}
