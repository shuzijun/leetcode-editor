package com.shuzijun.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.HttpClientUtils;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

/**
 * @author shuzijun
 */
public class LogoutAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        if (!PersistentConfig.getInstance().isConfig(anActionEvent.getProject())) {
            return;
        }

        HttpGet httpget = new HttpGet(URLUtils.getLeetcodeLogout());
        CloseableHttpResponse response = HttpClientUtils.executeGet(httpget);
        httpget.abort();
        HttpClientUtils.resetHttpclient();
        MessageUtils.showInfoMsg("info", PropertiesUtils.getInfo("login.out"));
    }
}
