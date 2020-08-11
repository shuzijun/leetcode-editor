package com.shuzijun.leetcode.plugin.actions.toolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.shuzijun.leetcode.plugin.actions.AbstractAction;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;
import com.shuzijun.leetcode.plugin.window.LoginFrame;
import com.shuzijun.leetcode.plugin.window.WindowFactory;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.net.HttpCookie;
import java.util.List;

/**
 * @author shuzijun
 */
public class LoginAction extends AbstractAction {

        @Override
        public void actionPerformed(AnActionEvent anActionEvent, Config config) {

        JTree tree = WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_TREE);

        if (StringUtils.isBlank(HttpRequestUtils.getToken())) {
            HttpRequest httpRequest = HttpRequest.get(URLUtils.getLeetcodeVerify());
            HttpResponse response = HttpRequestUtils.executeGet(httpRequest);
            if (response == null) {
                MessageUtils.getInstance(anActionEvent.getProject()).showWarnMsg("warning", PropertiesUtils.getInfo("request.failed"));
                return;
            }
            if (response.getStatusCode() != 200) {
                MessageUtils.getInstance(anActionEvent.getProject()).showWarnMsg("warning", PropertiesUtils.getInfo("request.failed"));
                return;
            }
        } else {
            if (HttpRequestUtils.isLogin()) {
                MessageUtils.getInstance(anActionEvent.getProject()).showWarnMsg("info", PropertiesUtils.getInfo("login.exist"));
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
            if (HttpRequestUtils.isLogin()) {
                MessageUtils.getInstance(anActionEvent.getProject()).showInfoMsg("login", PropertiesUtils.getInfo("login.success"));
                ViewManager.loadServiceData(tree, anActionEvent.getProject());
                return;
            } else {
                config.addCookie(config.getUrl() + config.getLoginName(), null);
                PersistentConfig.getInstance().setInitConfig(config);
            }
        }


        if (URLUtils.leetcodecn.equals(URLUtils.getLeetcodeHost())) {
            if (!LoginFrame.httpLogin.ajaxLogin(config, tree, anActionEvent.getProject())) {
                ApplicationManager.getApplication().invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        LoginFrame loginFrame = new LoginFrame(anActionEvent.getProject(), tree);
                        loginFrame.loadComponent();
                    }
                });
            }
        } else {
            ApplicationManager.getApplication().invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    LoginFrame loginFrame = new LoginFrame(anActionEvent.getProject(), tree);
                    loginFrame.loadComponent();
                }
            });
        }

    }


}
