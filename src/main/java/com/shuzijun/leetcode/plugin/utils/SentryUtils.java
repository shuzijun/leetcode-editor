package com.shuzijun.leetcode.plugin.utils;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.impl.ApplicationInfoImpl;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import io.sentry.SentryClient;
import io.sentry.SentryClientFactory;
import io.sentry.context.Context;
import io.sentry.event.EventBuilder;
import io.sentry.event.UserBuilder;
import io.sentry.event.helper.EventBuilderHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shuzijun
 */
public class SentryUtils {

    public static void submitErrorReport(Throwable error, String description) {

        final SentryClient sentry = SentryClientFactory.sentryClient("https://ac9e2d69c3294870848cee5b1b23ad51@sentry.io/1534194");

        final ApplicationInfoImpl applicationInfo = (ApplicationInfoImpl) ApplicationInfo.getInstance();

        EventBuilderHelper eventBuilder = new EventBuilderHelper() {
            @Override
            public void helpBuildingEvent(EventBuilder eventBuilder) {
                final Map<String, Object> os = new HashMap<>();
                os.put("name", SystemInfo.OS_NAME);
                os.put("version", SystemInfo.OS_VERSION);
                os.put("kernel_version", SystemInfo.OS_ARCH);

                final Map<String, Object> runtime = new HashMap<>();
                final String ideName = applicationInfo.getBuild().getProductCode();
                runtime.put("name", ideName);
                runtime.put("version", applicationInfo.getFullVersion());

                final Map<String, Map<String, Object>> contexts = new HashMap<>();
                contexts.put("os", os);
                contexts.put("runtime", runtime);

                eventBuilder.withContexts(contexts);

                if (!StringUtil.isEmptyOrSpaces(description)) {
                    eventBuilder.withMessage(description);
                    eventBuilder.withTag("with-description", "true");
                }
            }
        };

        sentry.addBuilderHelper(eventBuilder);

        final Context context = sentry.getContext();

        final Config config = PersistentConfig.getInstance().getInitConfig();
        if (config != null) {

            UserBuilder userBuilder = new UserBuilder();
            userBuilder.setId(config.getId());

            Map<String, Object> userConfig = new HashMap<>();
            userConfig.put("version", config.getVersion());
            userConfig.put("codeType", config.getCodeType());
            userConfig.put("url", config.getUrl());
            userConfig.put("proxy", config.getProxy());
            userConfig.put("customCode", config.getCustomCode());
            userConfig.put("customFileName", config.getCustomFileName());
            userConfig.put("customTemplate", config.getCustomTemplate());
            userBuilder.setData(userConfig);
            context.setUser(userBuilder.build());

        }
        context.addTag("javaVersion", SystemInfo.JAVA_RUNTIME_VERSION);
        context.addTag("pluginVersion", PluginManagerCore.getPlugin(PluginId.getId(PluginConstant.PLUGIN_ID)).getVersion());
        if(error == null){
            sentry.sendMessage(description);
        }else {
            sentry.sendException(error);
        }

    }

}
