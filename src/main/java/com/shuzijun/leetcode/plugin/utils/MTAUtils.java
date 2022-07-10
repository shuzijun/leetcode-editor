package com.shuzijun.leetcode.plugin.utils;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.io.HttpRequests;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import org.apache.http.client.utils.URIBuilder;

import java.awt.*;
import java.net.URI;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author shuzijun
 */
public class MTAUtils {

    private static String URL = "https://hm.baidu.com/hm.gif";
    private static String SID = "153b08575197a6f136f1fe02dd507c1e";
    private static String SI = String.valueOf(System.currentTimeMillis() / 1000);
    private static String version = null;
    private static String userAgent = null;

    private static ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    public static String getI(String prefix) {
        int[] b = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        for (int e = 10; 1 < e; e--) {
            int c = (int) Math.floor(10 * Math.random());
            int f = b[c];
            b[c] = b[e - 1];
            b[e - 1] = f;
        }
        int c = 0;
        for (int e = 0; 5 > e; e++) {
            c = 10 * c + b[e];
        }
        return prefix + c + System.currentTimeMillis();
    }

    public static void click(String actionsId, Config config) {
        cachedThreadPool.execute(new ClickTask(config, actionsId));
    }

    private static class ClickTask implements Runnable {

        private Config config;
        private String actionsId;

        public ClickTask(Config config, String actionsId) {
            this.config = config;
            this.actionsId = actionsId;
        }

        @Override
        public void run() {
            try {
                if (version == null) {
                    version = PluginManagerCore.getPlugin(PluginId.getId(PluginConstant.PLUGIN_ID)).getVersion();
                }
                if (userAgent == null) {
                    if (SystemInfo.OS_NAME.toUpperCase().contains("MAC")) {
                        userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36";
                    } else if (SystemInfo.OS_NAME.toUpperCase().contains("LINUX")) {
                        userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36";
                    } else {
                        userAgent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36";
                    }
                }
                Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
                Calendar calendar = Calendar.getInstance();
                URI uri = new URIBuilder(URL)
                        .setParameter("hca", config.getId())
                        .setParameter("cc", "1")
                        .setParameter("cf", version)
                        .setParameter("ck", "0")
                        .setParameter("cl", Toolkit.getDefaultToolkit().getScreenResolution() + "-bit")
                        .setParameter("ds", (int)screensize.getWidth() + "x" + (int)screensize.getHeight())
                        .setParameter("vl", "")
                        .setParameter("ep", "3392,2371")
                        .setParameter("ep", "3")
                        .setParameter("ja", "0")
                        .setParameter("ln", Locale.getDefault().toString().replace("_", "-").toLowerCase())
                        .setParameter("lo", "0")
                        .setParameter("lt", SI)
                        .setParameter("rnd", String.valueOf(System.currentTimeMillis() / 1000))
                        .setParameter("si", SID)
                        .setParameter("v", "1.2.92")
                        .setParameter("lv", "2")
                        .setParameter("sn", "44949")
                        .setParameter("r", "0")
                        .setParameter("ww", String.valueOf((int)screensize.getWidth()))
                        .setParameter("u", "http://leetcode-editor.shuzijun.cn/" + actionsId )
                        .build();
                HttpRequests.request(uri.toURL().toString()).userAgent(userAgent).tuner(connection -> {
                    connection.addRequestProperty("Cookie", "HMACCOUNT=" + config.getId() + ";" + "HMACCOUNT_BFESS" + config.getId());
                }).tryConnect();

            } catch (Exception e) {
            }
        }
    }
}
