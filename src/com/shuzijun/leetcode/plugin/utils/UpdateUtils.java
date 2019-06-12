package com.shuzijun.leetcode.plugin.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;
import com.shuzijun.leetcode.plugin.model.Config;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;


/**
 * @author shuzijun
 */
public class UpdateUtils{

    public static Boolean isCheck = true;

    public static void examine(Config config) {
        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            @Override
            public void run() {
                if (config != null && config.getUpdate() && isCheck) {
                    UpdateUtils.isCheck = false;
                    CloseableHttpClient httpClient = HttpClients.custom().build();
                    HttpGet httpget = null;
                    try {
                        String[] version = PluginManager.getPlugin(PluginId.getId("leetcode-editor")).getVersion().split("\\.");
                        httpget = new HttpGet("https://plugins.jetbrains.com/api/plugins/12132/updates");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        String body = EntityUtils.toString(response.getEntity(), "UTF-8");
                        JSONArray jsonArray = JSONObject.parseArray(body);
                        for (int i = 0; i < jsonArray.size(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            if (jsonObject.getBoolean("approve")) {
                                String[] nweVersion = jsonObject.getString("version").split("\\.");
                                if (Integer.valueOf(version[0]) < Integer.valueOf(nweVersion[0])) {
                                    MessageUtils.showInfoMsg("info", PropertiesUtils.getInfo("updata", jsonObject.getString("version")));
                                    break;
                                } else if (Integer.valueOf(version[0]).equals(Integer.valueOf(nweVersion[0]))) {
                                    if (Integer.valueOf(version[1]) < Integer.valueOf(nweVersion[1])) {
                                        MessageUtils.showInfoMsg("info", PropertiesUtils.getInfo("updata", jsonObject.getString("version")));
                                        break;
                                    }
                                }
                            }
                        }

                    } catch (Exception e) {

                    } finally {
                        if (httpget != null) {
                            httpget.abort();
                        }
                        try {
                            httpClient.close();
                        } catch (IOException e) {
                        }
                    }

                }
            }
        });

    }

}
