package com.shuzijun.leetcode.plugin.listener;

import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.ToolWindow;
import com.shuzijun.leetcode.plugin.utils.HttpClientUtils;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author shuzijun
 */
public class LoginOutListener implements ActionListener {

    private ToolWindow toolWindow;

    public LoginOutListener(ToolWindow toolWindow) {
        this.toolWindow = toolWindow;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        HttpGet httpget = new HttpGet(URLUtils.getLeetcodeLogout());
        CloseableHttpResponse response = HttpClientUtils.executeGet(httpget);
        httpget.abort();
        MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.INFO, "提示", "退出成功");
    }
}
