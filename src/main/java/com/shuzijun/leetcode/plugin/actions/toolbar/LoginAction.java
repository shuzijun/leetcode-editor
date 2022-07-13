package com.shuzijun.leetcode.plugin.actions.toolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.shuzijun.leetcode.plugin.actions.AbstractAction;
import com.shuzijun.leetcode.plugin.listener.LoginNotifier;
import com.shuzijun.leetcode.plugin.manager.NavigatorAction;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.HttpRequest;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;
import com.shuzijun.leetcode.plugin.window.NavigatorTabsPanel;
import com.shuzijun.leetcode.plugin.window.WindowFactory;
import com.shuzijun.leetcode.plugin.window.login.HttpLogin;
import com.shuzijun.leetcode.plugin.window.login.LoginPanel;
import org.apache.commons.lang.StringUtils;

import java.net.HttpCookie;
import java.util.List;

/**
 * @author shuzijun
 */
public class LoginAction extends AbstractAction implements DumbAware {

    @Override
    public synchronized void actionPerformed(AnActionEvent anActionEvent, Config config) {

        NavigatorAction navigatorAction = WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_NAVIGATORACTION);

        if (StringUtils.isBlank(HttpRequestUtils.getToken())) {
            HttpResponse response = HttpRequest.builderGet(URLUtils.getLeetcodeVerify()).request();
            if (response.getStatusCode() != 200) {
                MessageUtils.getInstance(anActionEvent.getProject()).showWarnMsg("warning", PropertiesUtils.getInfo("request.failed"));
                return;
            }
        } else {
            if (HttpRequestUtils.isLogin(anActionEvent.getProject())) {
                MessageUtils.getInstance(anActionEvent.getProject()).showWarnMsg("info", PropertiesUtils.getInfo("login.exist"));
                NavigatorTabsPanel.loadUser(true);
                if (navigatorAction.getPageInfo().getRowTotal() == 0) {
                    ApplicationManager.getApplication().getMessageBus().syncPublisher(LoginNotifier.TOPIC).login(anActionEvent.getProject(), config.getUrl());
                }
                return;
            }
        }

        if (StringUtils.isBlank(config.getLoginName())) {
            MessageUtils.getInstance(anActionEvent.getProject()).showWarnMsg("info", PropertiesUtils.getInfo("config.user"));
            return;
        }

        if (StringUtils.isNotBlank(config.getCookie(config.getUrl() + config.getLoginName()))) {
            List<HttpCookie> cookieList = CookieUtils.toHttpCookie(config.getCookie(config.getUrl() + config.getLoginName()));
            HttpRequestUtils.setCookie(cookieList);
            if (HttpRequestUtils.isLogin(anActionEvent.getProject())) {
                MessageUtils.getInstance(anActionEvent.getProject()).showInfoMsg("login", PropertiesUtils.getInfo("login.success"));
                NavigatorTabsPanel.loadUser(true);
                ApplicationManager.getApplication().getMessageBus().syncPublisher(LoginNotifier.TOPIC).login(anActionEvent.getProject(), config.getUrl());
                return;
            } else {
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
