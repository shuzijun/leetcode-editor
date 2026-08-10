package com.shuzijun.leetcode.plugin.manager;

import com.intellij.openapi.progress.ProgressIndicator;
import com.shuzijun.lc.model.CodeExecutionResult;
import com.shuzijun.lc.model.RunCodeCheckResult;
import com.shuzijun.lc.model.SubmitCheckResult;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CodeManagerRequestTest {

    @Test
    public void stopsPollingPromptlyWhenCanceled() {
        AtomicBoolean canceled = new AtomicBoolean(false);
        ProgressIndicator indicator = (ProgressIndicator) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{ProgressIndicator.class},
                (proxy, method, args) -> {
                    if ("isCanceled".equals(method.getName())) {
                        return canceled.get();
                    }
                    return defaultValue(method.getReturnType());
                }
        );

        Thread cancelThread = new Thread(() -> {
            try {
                Thread.sleep(25L);
                canceled.set(true);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        });
        cancelThread.start();

        long startedAt = System.nanoTime();
        assertFalse(CodeManager.waitForNextPoll(indicator));
        long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000L;

        assertTrue("cancellation should not wait for the full poll interval", elapsedMillis < 200L);
    }

    @Test
    public void submitCompileErrorFallsBackToStatusWhenDetailsAreMissing() {
        SubmitCheckResult submitResult = new SubmitCheckResult();
        submitResult.setStatusMsg("Compile Error");

        assertEquals(
                "Compile Error",
                CodeManager.buildErrorMsg(CodeExecutionResult.fromSubmit(submitResult))
        );
    }

    @Test
    public void runRuntimeErrorFallsBackToStatusWhenDetailsAreMissing() {
        RunCodeCheckResult runResult = new RunCodeCheckResult();
        runResult.setStatusMsg("Runtime Error");

        assertEquals(
                "Runtime Error",
                CodeManager.buildErrorMsg(CodeExecutionResult.fromRun(runResult))
        );
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0F;
        }
        if (type == double.class) {
            return 0D;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == char.class) {
            return '\0';
        }
        return null;
    }
}
