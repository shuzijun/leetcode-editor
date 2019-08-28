package com.shuzijun.leetcode.plugin.actions;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.shuzijun.leetcode.plugin.manager.ViewManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;

import javax.swing.*;
import java.io.IOException;

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


        HttpPost post = new HttpPost(URLUtils.getLeetcodeLogin());
        try {
            HttpEntity ent = MultipartEntityBuilder.create()
                    .addTextBody("csrfmiddlewaretoken", HttpClientUtils.getToken())
                    .addTextBody("login", config.getLoginName())
                    .addTextBody("password", PersistentConfig.getInstance().getPassword())
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
                examineEmail();
                MessageUtils.showInfoMsg("info", PropertiesUtils.getInfo("login.success"));
            } else {
                HttpClientUtils.resetHttpclient();
                MessageUtils.showInfoMsg("info", PropertiesUtils.getInfo("login.unknown"));
                SentryUtils.submitErrorReport(null,String.format("login.unknown:\nStatusCode:%s\nbody:%s",loginResponse.getStatusLine().getStatusCode(),body));
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

    private void examineEmail() {
        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            @Override
            public void run() {
                HttpPost post = new HttpPost(URLUtils.getLeetcodeGraphql());
                try {
                    StringEntity entity = new StringEntity("{\"operationName\":\"user\",\"variables\":{},\"query\":\"query user {\\n  user {\\n    socialAccounts\\n    username\\n    emails {\\n      email\\n      primary\\n      verified\\n      __typename\\n    }\\n    phone\\n    profile {\\n      rewardStats\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}");
                    post.setEntity(entity);
                    post.setHeader("Accept", "application/json");
                    post.setHeader("Content-type", "application/json");
                    CloseableHttpResponse response = HttpClientUtils.executePost(post);
                    if (response != null && response.getStatusLine().getStatusCode() == 200) {

                        String body = EntityUtils.toString(response.getEntity(), "UTF-8");

                        JSONArray jsonArray = JSONObject.parseObject(body).getJSONObject("data").getJSONObject("user").getJSONArray("emails");
                        if (jsonArray != null && jsonArray.size() > 0) {
                            for (int i = 0; i < jsonArray.size(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                if (object.getBoolean("verified")) {
                                    return;
                                }
                            }

                        }
                        MessageUtils.showWarnMsg("info", PropertiesUtils.getInfo("user.email"));
                    }
                } catch (IOException i) {
                    LogUtils.LOG.error("验证邮箱错误");
                } finally {
                    post.abort();
                }
            }
        });
    }
}
