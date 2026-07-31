package com.shuzijun.leetcode.plugin.utils;

import com.intellij.ide.BrowserUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * @author shuzijun
 */
public class BrowserUtils {

    private static final String TEST_BROWSER_CAPTURE_FILE = "leetcode.test.browser.capture.file";

    private static volatile Consumer<String> testBrowser;

    public static void browse(String url) {
        Consumer<String> browser = testBrowser;
        if (browser != null) {
            browser.accept(url);
            return;
        }
        String captureFile = System.getProperty(TEST_BROWSER_CAPTURE_FILE);
        if (captureFile != null) {
            try {
                Files.writeString(Path.of(captureFile), url);
                return;
            } catch (IOException e) {
                throw new IllegalStateException("Unable to capture test browser URL", e);
            }
        }
        BrowserUtil.browse(url);
    }

    @TestOnly
    public static void setTestBrowser(@Nullable Consumer<String> browser) {
        testBrowser = browser;
    }
}
