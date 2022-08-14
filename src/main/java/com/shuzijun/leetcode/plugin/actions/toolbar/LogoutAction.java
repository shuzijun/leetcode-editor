package com.shuzijun.leetcode.plugin.actions.toolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.shuzijun.leetcode.platform.model.Config;
import com.shuzijun.leetcode.platform.model.HttpRequest;
import com.shuzijun.leetcode.platform.model.HttpResponse;
import com.shuzijun.leetcode.plugin.actions.AbstractAction;
import com.shuzijun.leetcode.plugin.model.PluginTopic;
import com.shuzijun.leetcode.plugin.service.RepositoryServiceImpl;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import com.shuzijun.leetcode.plugin.window.NavigatorTabsPanel;

/**
 * @author shuzijun
 */
public class LogoutAction extends AbstractAction implements DumbAware {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config) {

        HttpResponse httpResponse = HttpRequest.builder(RepositoryServiceImpl.getInstance(anActionEvent.getProject()).getHttpRequestService()).get(URLUtils.getLeetcodeLogout()).request();
        RepositoryServiceImpl.getInstance(anActionEvent.getProject()).getHttpRequestService().resetHttpclient();
        MessageUtils.getInstance(anActionEvent.getProject()).showInfoMsg("info", PropertiesUtils.getInfo("login.out"));
        NavigatorTabsPanel.loadUser(false, anActionEvent.getProject());
        ApplicationManager.getApplication().getMessageBus().syncPublisher(PluginTopic.LOGIN_TOPIC).logout(anActionEvent.getProject(), config.getUrl());
    }
}
