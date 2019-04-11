package com.shuzijun.leetcode.plugin.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;
import com.shuzijun.leetcode.plugin.model.Config;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;


/**
 * @author shuzijun
 */
public class UpdataUtils {

    public static Boolean isCheck = true;

    public static void examine(Config config) {
        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            @Override
            public void run() {
                HttpGet httpget = null;
                try {
                    if (config != null && config.isUpdata() && isCheck) {
                        String[] version = PluginManager.getPlugin(PluginId.getId("leetcode-editor")).getVersion().split("\\.");
                        httpget = new HttpGet("https://plugins.jetbrains.com/api/plugins/12132/updates");
                        CloseableHttpResponse response = HttpClientUtils.executeGet(httpget);
                        String body = EntityUtils.toString(response.getEntity(), "UTF-8");
                        JSONArray jsonArray = JSONObject.parseArray(body);
                        for (int i = 0; i < jsonArray.size(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            if (jsonObject.getBoolean("approve")) {
                                String[] nweVersion = jsonObject.getString("version").split("\\.");
                                if (Integer.valueOf(version[0]) < Integer.valueOf(nweVersion[0])) {
                                    MessageUtils.showWarnMsg("info", PropertiesUtils.getInfo("updata", jsonObject.getString("version")));
                                } else if (Integer.valueOf(version[0]).equals(Integer.valueOf(nweVersion[0]))) {
                                    if (Integer.valueOf(version[1]) < Integer.valueOf(nweVersion[1])) {
                                        MessageUtils.showWarnMsg("info", PropertiesUtils.getInfo("updata", jsonObject.getString("version")));
                                    }
                                }
                            }
                        }
                        UpdataUtils.isCheck = false;
                    }
                } catch (Exception e) {

                } finally {
                    if (httpget != null) {
                        httpget.abort();
                    }
                }


            }
        });

    }

}
