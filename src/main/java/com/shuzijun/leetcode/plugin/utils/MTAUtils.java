package com.shuzijun.leetcode.plugin.utils;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.SystemInfo;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.utils.io.HttpRequests;
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

    private static String URL = "http://pingtcss.qq.com/pingd";
    private static String SID = "500676642";
    private static String SI = getI("s");
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
                    version = PluginManager.getPlugin(PluginId.getId(Constant.PLUGIN_ID)).getVersion()
                            .replace("v", "").replaceAll("-|_", ".");
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
                        .setParameter("dm", "127.0.0.1")
                        .setParameter("pvi", config.getId())
                        .setParameter("si", SI)
                        .setParameter("url", "/" + actionsId)
                        .setParameter("arg", "")
                        .setParameter("ty", "0")
                        .setParameter("rdm", "")
                        .setParameter("rurl", "")
                        .setParameter("rarg", "")
                        .setParameter("adt", version)
                        .setParameter("r2", SID)
                        .setParameter("scr", (int)screensize.getWidth() + "x" + (int)screensize.getHeight())
                        .setParameter("scl", Toolkit.getDefaultToolkit().getScreenResolution() + "-bit")
                        .setParameter("lg", Locale.getDefault().toString().replace("_", "-").toLowerCase())
                        .setParameter("tz", -(calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / 60000 / 60 + "")
                        .setParameter("ext", "version=2.0.14")
                        .setParameter("random", System.currentTimeMillis() + "")
                        .build();

                HttpRequests.request(uri.toURL().toString()).userAgent(userAgent).tryConnect();

            } catch (Exception e) {
            }
        }
    }
}
