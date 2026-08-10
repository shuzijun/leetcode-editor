package com.shuzijun.leetcode.plugin.actions.toolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.shuzijun.leetcode.plugin.actions.AbstractAction;
import com.shuzijun.leetcode.plugin.application.CacheInvalidationCoordinator;
import com.shuzijun.leetcode.plugin.application.CacheInvalidationReason;
import com.shuzijun.leetcode.plugin.application.LeetCodeServices;
import com.shuzijun.leetcode.plugin.application.LoginGenerationTracker;
import com.shuzijun.leetcode.plugin.listener.LoginNotifier;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.utils.*;
import com.shuzijun.leetcode.plugin.window.NavigatorTabsPanel;

/**
 * @author shuzijun
 */
public class LogoutAction extends AbstractAction implements DumbAware {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config) {

        LoginGenerationTracker.next();
        CacheInvalidationCoordinator.invalidate(
                CacheInvalidationReason.LOGOUT,
                config.getUrl(),
                "https://" + config.getUrl()
        );
        try {
            LeetCodeServices.login().clearCookies();
        } catch (Exception exception) {
            LogUtils.LOG.warn("Failed to close the LeetCode session", exception);
        }
        MessageUtils.getInstance(anActionEvent.getProject()).showInfoMsg("info", PropertiesUtils.getInfo("login.out"));
        NavigatorTabsPanel.loadUser(false);
        ApplicationManager.getApplication().getMessageBus().syncPublisher(LoginNotifier.TOPIC).logout(anActionEvent.getProject(), config.getUrl());
    }
}
