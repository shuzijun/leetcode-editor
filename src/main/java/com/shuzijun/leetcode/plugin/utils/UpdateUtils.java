package com.shuzijun.leetcode.plugin.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author shuzijun
 */
public class UpdateUtils {

    private static final AtomicBoolean UPDATE_CHECKED = new AtomicBoolean();

    public static void examine(Config config, Project project) {
        if (config == null || !config.getUpdate() || !UPDATE_CHECKED.compareAndSet(false, true)) {
            return;
        }
        AppExecutorUtil.getAppExecutorService().execute(() -> {
            try (CloseableHttpClient httpClient = HttpClients.custom().build();
                 CloseableHttpResponse response = httpClient.execute(
                         new HttpGet("https://plugins.jetbrains.com/api/plugins/" + PluginConstant.WEB_ID + "/updates"))) {
                String body = EntityUtils.toString(response.getEntity(), "UTF-8");
                JSONArray jsonArray = JSONObject.parseArray(body);
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getBoolean("approve")
                            && isNewerVersion(PluginVersionUtils.getVersion(), jsonObject.getString("version"))) {
                        MessageUtils.getInstance(project).showInfoMsg("info",
                                PropertiesUtils.getInfo("updata", jsonObject.getString("version")));
                        break;
                    }
                }
            } catch (Exception exception) {
                LogUtils.LOG.debug("Plugin update check failed", exception);
            }
        });
    }

    static boolean isNewerVersion(String currentVersion, String candidateVersion) {
        String[] currentParts = normalizeVersion(currentVersion).split("\\.");
        String[] candidateParts = normalizeVersion(candidateVersion).split("\\.");
        int componentCount = Math.max(currentParts.length, candidateParts.length);
        for (int index = 0; index < componentCount; index++) {
            int current = versionPart(currentParts, index);
            int candidate = versionPart(candidateParts, index);
            if (current != candidate) {
                return candidate > current;
            }
        }
        return false;
    }

    private static String normalizeVersion(String version) {
        return version == null ? "" : version.replaceFirst("^[vV]", "").split("-", 2)[0];
    }

    private static int versionPart(String[] parts, int index) {
        if (index >= parts.length) {
            return 0;
        }
        try {
            return Integer.parseInt(parts[index]);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
