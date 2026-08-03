package com.shuzijun.leetcode.plugin.manager;

import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.progress.ProgressIndicator;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Question;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CodeManagerRequestTest {

    @Test
    public void createsSubmitPayloadWithQuestionLanguageAndCode() {
        JSONObject request = CodeManager.createSubmitRequest(question(), CodeTypeEnum.JAVA,
                "class Solution {}");

        assertEquals("1", request.getString("question_id"));
        assertEquals("java", request.getString("lang"));
        assertEquals("class Solution {}", request.getString("typed_code"));
        assertFalse(request.containsKey("data_input"));
    }

    @Test
    public void createsRunPayloadWithTestCaseAndLargeJudge() {
        JSONObject request = CodeManager.createRunRequest(question(), CodeTypeEnum.JAVA,
                "class Solution {}");

        assertEquals("[2,7,11,15]\n9", request.getString("data_input"));
        assertEquals("large", request.getString("judge_type"));
        assertEquals("java", request.getString("lang"));
        assertEquals("class Solution {}", request.getString("typed_code"));
    }

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

    private static Question question() {
        Question question = new Question();
        question.setQuestionId("1");
        question.setTestCase("[2,7,11,15]\n9");
        return question;
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
