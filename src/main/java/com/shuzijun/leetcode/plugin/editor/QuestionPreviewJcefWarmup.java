package com.shuzijun.leetcode.plugin.editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.jcef.JBCefApp;

import java.util.concurrent.atomic.AtomicBoolean;

public final class QuestionPreviewJcefWarmup {

    private static final Logger LOG = Logger.getInstance(QuestionPreviewJcefWarmup.class);
    private static final AtomicBoolean REQUESTED = new AtomicBoolean();

    private QuestionPreviewJcefWarmup() {
    }

    public static void request() {
        if (!JBCefApp.isSupported() || JBCefApp.isStarted() || !REQUESTED.compareAndSet(false, true)) {
            return;
        }
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                JBCefApp.getInstance();
            } catch (RuntimeException exception) {
                REQUESTED.set(false);
                LOG.warn("Unable to warm up JCEF for question preview", exception);
            }
        });
    }
}
