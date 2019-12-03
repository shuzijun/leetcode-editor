package com.shuzijun.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;
import com.shuzijun.leetcode.plugin.window.LoginPanel;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import javax.swing.*;

/**
 * @author shuzijun
 */
public class LoginAction extends AbstractAsynAction {
    @Override
    public void perform(AnActionEvent anActionEvent, Config config) {

        if (StringUtils.isBlank(HttpClientUtils.getToken())) {
            HttpGet httpget = new HttpGet(URLUtils.getLeetcodeVerify());
            CloseableHttpResponse response = HttpClientUtils.executeGet(httpget);
            httpget.abort();
            if (response == null) {
                MessageUtils.showWarnMsg("warning", PropertiesUtils.getInfo("request.failed"));
                return;
            }
            if (response.getStatusLine().getStatusCode() != 200) {
                JTree tree = anActionEvent.getData(DataKeys.LEETCODE_PROJECTS_TREE);
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

        if (StringUtils.isBlank(config.getLoginName()) || StringUtils.isBlank(PersistentConfig.getInstance().getPassword())) {
            MessageUtils.showWarnMsg("info", PropertiesUtils.getInfo("config.user"));
            return;
        }

        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginPanel dialog = new LoginPanel(anActionEvent.getProject(),anActionEvent.getData(DataKeys.LEETCODE_PROJECTS_TREE));
                dialog.setTitle("login");
                dialog.show();
            }
        });

    }

}
