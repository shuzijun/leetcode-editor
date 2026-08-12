package com.shuzijun.leetcode.plugin.actions.toolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.shuzijun.leetcode.plugin.actions.AbstractAction;
import com.shuzijun.leetcode.plugin.application.LeetCodeServices;
import com.shuzijun.leetcode.plugin.manager.NavigatorAction;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;
import com.shuzijun.leetcode.plugin.window.WindowFactory;
import com.shuzijun.leetcode.plugin.window.login.HttpLogin;
import com.shuzijun.leetcode.plugin.window.login.LoginPanel;
import org.apache.commons.lang3.StringUtils;

import java.net.HttpCookie;
import java.util.List;

/**
 * @author shuzijun
 */
public class LoginAction extends AbstractAction implements DumbAware {

    @Override
    public synchronized void actionPerformed(AnActionEvent anActionEvent, Config config) {

        NavigatorAction navigatorAction = WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_NAVIGATORACTION);

        if (StringUtils.isBlank(LeetCodeServices.login().csrfToken())) {
            if (!LeetCodeServices.login().verify()) {
                MessageUtils.getInstance(anActionEvent.getProject()).showWarnMsg("warning", PropertiesUtils.getInfo("request.failed"));
                return;
            }
        } else {
            if (LeetCodeServices.login().isLoggedIn()) {
                MessageUtils.getInstance(anActionEvent.getProject()).showWarnMsg("info", PropertiesUtils.getInfo("login.exist"));
                HttpLogin.notifyLoginAfterUserLoaded(anActionEvent.getProject(), config.getUrl());
                return;
            }
        }

        if (StringUtils.isBlank(config.getLoginName())) {
            MessageUtils.getInstance(anActionEvent.getProject()).showWarnMsg("info", PropertiesUtils.getInfo("config.user"));
            return;
        }

        if (StringUtils.isNotBlank(config.getCookie(config.getUrl() + config.getLoginName()))) {
            List<HttpCookie> cookieList = CookieUtils.toHttpCookie(config.getCookie(config.getUrl() + config.getLoginName()));
            boolean loggedIn = false;
            try {
                LeetCodeServices.login().setCookies(cookieList);
                loggedIn = LeetCodeServices.login().isLoggedIn();
                if (loggedIn) {
                    MessageUtils.getInstance(anActionEvent.getProject()).showInfoMsg("login", PropertiesUtils.getInfo("login.success"));
                    HttpLogin.notifyLoginAfterUserLoaded(anActionEvent.getProject(), config.getUrl());
                    return;
                }
            } catch (Exception exception) {
                LogUtils.LOG.warn("Failed to restore the saved LeetCode login cookies", exception);
            }
            if (!loggedIn) {
                config.addCookie(config.getUrl() + config.getLoginName(), null);
                PersistentConfig.getInstance().setInitConfig(config);
            }
        }

        if (!HttpLogin.ajaxLogin(config, navigatorAction, anActionEvent.getProject())) {
            ApplicationManager.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {
                    LoginPanel loginPanel = new LoginPanel(anActionEvent.getProject());
                    loginPanel.show();
                }
            });
        }

    }


}
