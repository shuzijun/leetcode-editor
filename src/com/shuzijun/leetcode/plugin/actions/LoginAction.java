package com.shuzijun.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;

import javax.swing.*;

/**
 * @author shuzijun
 */
public class LoginAction extends AbstractAsynAction {
    @Override
    public void perform(AnActionEvent anActionEvent,Config config) {

        if (StringUtils.isBlank(HttpClientUtils.getToken())) {
            HttpGet httpget = new HttpGet(URLUtils.getLeetcodeUrl());
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

        if (StringUtils.isBlank(config.getLoginName()) || StringUtils.isBlank(config.getLoginName())) {
            MessageUtils.showWarnMsg("info", PropertiesUtils.getInfo("config.user"));
            return;
        }


        HttpPost post = new HttpPost(URLUtils.getLeetcodeLogin());
        try {
            HttpEntity ent = MultipartEntityBuilder.create()
                    .addTextBody("csrfmiddlewaretoken", HttpClientUtils.getToken())
                    .addTextBody("login", config.getLoginName())
                    .addTextBody("password", PersistentConfig.getInstance().getPassword(config.getPassword()))
                    .addTextBody("next", "/problems")
                    .build();
            post.setEntity(ent);
            CloseableHttpResponse loginResponse = HttpClientUtils.executePost(post);

            if (loginResponse == null) {
                MessageUtils.showWarnMsg("warning", PropertiesUtils.getInfo("request.failed"));
                return;
            }

            String body = EntityUtils.toString(loginResponse.getEntity(), "UTF-8");

            if ((loginResponse.getStatusLine().getStatusCode() == 200 || loginResponse.getStatusLine().getStatusCode() == 302)
                    && StringUtils.isBlank(body)) {
                MessageUtils.showInfoMsg("info", PropertiesUtils.getInfo("login.success"));
            } else {
                MessageUtils.showInfoMsg("info", PropertiesUtils.getInfo("login.failed"));
                return;
            }
        } catch (Exception e) {
            LogUtils.LOG.error("登陆错误", e);
            MessageUtils.showInfoMsg("info", PropertiesUtils.getInfo("login.failed"));
            return;
        } finally {
            post.abort();
        }

        JTree tree = anActionEvent.getData(DataKeys.LEETCODE_PROJECTS_TREE);
        ViewManager.loadServiceData(tree);

    }
}
