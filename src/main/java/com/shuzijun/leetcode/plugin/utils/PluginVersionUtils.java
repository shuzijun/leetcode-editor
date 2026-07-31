package com.shuzijun.leetcode.plugin.utils;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

/** Reads the version from this plugin's packaged descriptor without platform-internal APIs. */
public final class PluginVersionUtils {
    private static final String UNKNOWN_VERSION = "unknown";
    private static final String VERSION = readVersion();

    private PluginVersionUtils() {
    }

    public static String getVersion() {
        return VERSION;
    }

    private static String readVersion() {
        try (InputStream input = PluginVersionUtils.class.getResourceAsStream("/META-INF/plugin.xml")) {
            if (input == null) {
                return UNKNOWN_VERSION;
            }
            String version = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(input)
                    .getDocumentElement()
                    .getElementsByTagName("version")
                    .item(0)
                    .getTextContent();
            return version == null || version.trim().isEmpty() ? UNKNOWN_VERSION : version.trim();
        } catch (Exception ignored) {
            return UNKNOWN_VERSION;
        }
    }
}
