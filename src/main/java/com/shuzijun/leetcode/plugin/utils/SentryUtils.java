package com.shuzijun.leetcode.plugin.utils;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import io.sentry.Sentry;
import io.sentry.protocol.User;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shuzijun
 */
public class SentryUtils {

    private static final String SENTRY_DSN = "https://ac9e2d69c3294870848cee5b1b23ad51@sentry.io/1534194";
    private static volatile boolean initialized;

    public static void submitErrorReport(Throwable error, String description) {
        initialize();
        if (error == null) {
            Sentry.captureMessage(description == null ? "" : description, scope -> configureScope(scope, description));
        } else {
            Sentry.captureException(error, scope -> configureScope(scope, description));
        }
    }

    private static void initialize() {
        if (initialized) {
            return;
        }
        synchronized (SentryUtils.class) {
            if (!initialized) {
                Sentry.init(options -> options.setDsn(SENTRY_DSN));
                initialized = true;
            }
        }
    }

    private static void configureScope(io.sentry.IScope scope, String description) {
        final ApplicationInfo applicationInfo = ApplicationInfo.getInstance();
        final Map<String, Object> os = new HashMap<>();
        os.put("name", SystemInfo.OS_NAME);
        os.put("version", SystemInfo.OS_VERSION);
        os.put("kernel_version", SystemInfo.OS_ARCH);
        scope.setContexts("os", os);

        final Map<String, Object> runtime = new HashMap<>();
        runtime.put("name", applicationInfo.getBuild().getProductCode());
        runtime.put("version", applicationInfo.getFullVersion());
        scope.setContexts("runtime", runtime);

        if (!StringUtil.isEmptyOrSpaces(description)) {
            scope.setTag("with-description", "true");
        }

        final Config config = PersistentConfig.getInstance().getInitConfig();
        if (config != null) {
            final User user = new User();
            user.setId(config.getId());

            final Map<String, String> userConfig = new HashMap<>();
            userConfig.put("version", String.valueOf(config.getVersion()));
            userConfig.put("codeType", config.getCodeType());
            userConfig.put("url", config.getUrl());
            userConfig.put("proxy", String.valueOf(config.getProxy()));
            userConfig.put("customCode", String.valueOf(config.getCustomCode()));
            userConfig.put("customFileName", config.getCustomFileName());
            userConfig.put("customTemplate", config.getCustomTemplate());
            user.setData(userConfig);
            scope.setUser(user);
        }
        scope.setTag("javaVersion", SystemInfo.JAVA_RUNTIME_VERSION);
        scope.setTag("pluginVersion", PluginVersionUtils.getVersion());
    }
}
