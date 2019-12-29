package com.shuzijun.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;
import com.shuzijun.leetcode.plugin.window.LoginFrame;
import com.shuzijun.leetcode.plugin.window.WindowFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.cookie.BasicClientCookie;

import javax.swing.*;
import java.util.List;

/**
 * @author shuzijun
 */
public class LoginAction extends AbstractAsynAction {
    @Override
    public void perform(AnActionEvent anActionEvent, Config config) {

        JTree tree = WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_TREE);

        if (StringUtils.isBlank(HttpClientUtils.getToken())) {
            HttpGet httpget = new HttpGet(URLUtils.getLeetcodeVerify());
            CloseableHttpResponse response = HttpClientUtils.executeGet(httpget);
            httpget.abort();
            if (response == null) {
                MessageUtils.showWarnMsg("warning", PropertiesUtils.getInfo("request.failed"));
                return;
            }
            if (response.getStatusLine().getStatusCode() != 200) {
                ViewManager.loadServiceData(tree);
                MessageUtils.showWarnMsg("warning", PropertiesUtils.getInfo("request.failed"));
                return;
            }
        } else {
            if (HttpClientUtils.isLogin()) {
                MessageUtils.showWarnMsg("info", PropertiesUtils.getInfo("login.exist"));
                return;
            }
        }

        if (StringUtils.isBlank(config.getLoginName())) {
            MessageUtils.showWarnMsg("info", PropertiesUtils.getInfo("config.user"));
            return;
        }

        if (StringUtils.isNotBlank(config.getCookie(config.getUrl() + config.getLoginName()))) {
            List<BasicClientCookie> cookieList = CookieUtils.toCookie(config.getCookie(config.getUrl() + config.getLoginName()));
            HttpClientUtils.setCookie(cookieList);
            if (HttpClientUtils.isLogin()) {
                MessageUtils.showInfoMsg("login", PropertiesUtils.getInfo("login.success"));
                ViewManager.loadServiceData(tree);
                return;
            } else {
                config.addCookie(config.getUrl() + config.getLoginName(), null);
                PersistentConfig.getInstance().setInitConfig(config);
            }
        }

        LoginFrame loginFrame = new LoginFrame(anActionEvent.getProject(),tree);
        if (URLUtils.leetcodecn.equals(URLUtils.getLeetcodeHost())) {
            if(!loginFrame.ajaxLogin(config)){
                loginFrame.loadComponent();
                loginFrame.show();
            }
        } else {
            loginFrame.loadComponent();
            loginFrame.show();
        }

    }




}
