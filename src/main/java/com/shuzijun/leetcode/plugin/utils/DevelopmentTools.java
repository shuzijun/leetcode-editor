package com.shuzijun.leetcode.plugin.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class DevelopmentTools {

    public static final String SYSTEM_PROPERTY = "leetcode.development.tools";
    private static final String BUILD_PROPERTIES_RESOURCE = "/META-INF/leetcode-editor-build.properties";
    private static final String BUILD_PROPERTY = "development.tools.enabled";

    private DevelopmentTools() {
    }

    public static boolean isEnabled() {
        String runtimeValue = System.getProperty(SYSTEM_PROPERTY);
        if (runtimeValue != null) {
            return Boolean.parseBoolean(runtimeValue);
        }
        try (InputStream inputStream = DevelopmentTools.class.getResourceAsStream(BUILD_PROPERTIES_RESOURCE)) {
            if (inputStream == null) {
                return false;
            }
            Properties properties = new Properties();
            properties.load(inputStream);
            return Boolean.parseBoolean(properties.getProperty(BUILD_PROPERTY));
        } catch (IOException exception) {
            LogUtils.LOG.warn("Unable to read LeetCode Editor build properties", exception);
            return false;
        }
    }
}
