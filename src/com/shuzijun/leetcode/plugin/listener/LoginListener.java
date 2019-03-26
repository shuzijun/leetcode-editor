package com.shuzijun.leetcode.plugin.listener;

import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.HttpClientUtils;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * @author shuzijun
 */
public class LoginListener implements ActionListener {

    private final static Logger logger = LoggerFactory.getLogger(LoginListener.class);

    private ToolWindow toolWindow;
    private JBScrollPane contentScrollPanel;

    public LoginListener(ToolWindow toolWindow, JBScrollPane contentScrollPanel) {
        this.toolWindow = toolWindow;
        this.contentScrollPanel = contentScrollPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        logger.error("开始进行登陆");
        if (StringUtils.isBlank(HttpClientUtils.getToken())) {
            HttpGet httpget = new HttpGet(URLUtils.getLeetcodeUrl());
            CloseableHttpResponse response = HttpClientUtils.executeGet(httpget);
            httpget.abort();
            if (response == null) {
                MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.ERROR, "error", PropertiesUtils.getInfo("request.failed"));
                return;
            }
            if (response.getStatusLine().getStatusCode() != 200) {
                MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.ERROR, "error", PropertiesUtils.getInfo("request.failed"));
                return;
            }
            logger.info("主页请求成功");
        } else {
            if (HttpClientUtils.isLogin()) {
                MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.INFO, "info", PropertiesUtils.getInfo("login.exist"));
                return;
            }
        }
        logger.info("进行登陆");
        Config config = PersistentConfig.getInstance().getInitConfig();
        HttpPost post = new HttpPost(URLUtils.getLeetcodeLogin());
        HttpEntity ent = MultipartEntityBuilder.create()
                .addTextBody("csrfmiddlewaretoken", HttpClientUtils.getToken())
                .addTextBody("login", config.getLoginName())
                .addTextBody("password", config.getPassword())
                .addTextBody("next", "/problems")
                .build();
        post.setEntity(ent);
        CloseableHttpResponse loginResponse = HttpClientUtils.executePost(post);
        post.abort();
        if (loginResponse == null) {
            MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.ERROR, "error", PropertiesUtils.getInfo("request.failed"));
            return;
        }
        if (loginResponse.getStatusLine().getStatusCode() == 302 || loginResponse.getStatusLine().getStatusCode() == 200) {
            MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.INFO, "info", PropertiesUtils.getInfo("login.success"));
        } else {
            try {
                System.out.println(EntityUtils.toString(loginResponse.getEntity(), "UTF-8"));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.INFO, "info", PropertiesUtils.getInfo("login.failed"));
            logger.error("请求登陆失败");
            return;
        }

        new LoadListener(toolWindow, contentScrollPanel).actionPerformed(null);


    }

}
