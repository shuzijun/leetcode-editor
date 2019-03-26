package com.shuzijun.leetcode.plugin.listener;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.ToolWindow;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author shuzijun
 */
public class SubmitMenuRunnable implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(SubmitMenuRunnable.class);

    private Question question;

    private ToolWindow toolWindow;

    private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    public SubmitMenuRunnable(Question question, ToolWindow toolWindow) {
        this.question = question;
        this.toolWindow = toolWindow;
    }

    @Override
    public void run() {

        String codeType = PersistentConfig.getInstance().getInitConfig().getCodeType();
        CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnum(codeType);
        if (codeTypeEnum == null) {
            MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.INFO, "info", PropertiesUtils.getInfo("config.code"));
            return;
        }
        if (!HttpClientUtils.isLogin()) {
            MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.INFO, "info", PropertiesUtils.getInfo("login.not"));
            return;
        }
        String filePath = PersistentConfig.getInstance().getTempFilePath() + question.getTitle() + codeTypeEnum.getSuffix();
        File file = new File(filePath);
        if (!file.exists()) {
            MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.INFO, "info", PropertiesUtils.getInfo("request.code"));
            return;
        } else {

            try {
                String code = FileUtils.getClearCommentFileBody(file, codeTypeEnum);
                if (StringUtils.isBlank(code)) {
                    MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.INFO, "info", PropertiesUtils.getInfo("request.empty"));
                    return;
                }

                HttpPost questionCodePost = new HttpPost(URLUtils.getLeetcodeGraphql());
                StringEntity entityCode = new StringEntity("{\"operationName\":\"questionData\",\"variables\":{\"titleSlug\":\"" + question.getTitleSlug() + "\"},\"query\":\"query questionData($titleSlug: String!) {\\n  question(titleSlug: $titleSlug) {\\n    questionId\\n    questionFrontendId\\n    boundTopicId\\n    title\\n    titleSlug\\n    content\\n    translatedTitle\\n    translatedContent\\n    isPaidOnly\\n    difficulty\\n    likes\\n    dislikes\\n    isLiked\\n    similarQuestions\\n    contributors {\\n      username\\n      profileUrl\\n      avatarUrl\\n      __typename\\n    }\\n    langToValidPlayground\\n    topicTags {\\n      name\\n      slug\\n      translatedName\\n      __typename\\n    }\\n    companyTagStats\\n    codeSnippets {\\n      lang\\n      langSlug\\n      code\\n      __typename\\n    }\\n    stats\\n    hints\\n    solution {\\n      id\\n      canSeeDetail\\n      __typename\\n    }\\n    status\\n    sampleTestCase\\n    metaData\\n    judgerAvailable\\n    judgeType\\n    mysqlSchemas\\n    enableRunCode\\n    enableTestMode\\n    envInfo\\n    __typename\\n  }\\n}\\n\"}");
                questionCodePost.setEntity(entityCode);
                questionCodePost.setHeader("Accept", "application/json");
                questionCodePost.setHeader("Content-type", "application/json");
                CloseableHttpResponse responseCode = HttpClientUtils.executePost(questionCodePost);
                if (responseCode != null && responseCode.getStatusLine().getStatusCode() == 200) {
                    String body = EntityUtils.toString(responseCode.getEntity(), "UTF-8");

                    JSONArray jsonArray = JSONObject.parseObject(body).getJSONObject("data").getJSONObject("question").getJSONArray("codeSnippets");
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        if (codeTypeEnum.getType().equals(object.getString("lang"))) {
                            question.setLangSlug(object.getString("langSlug"));
                            break;
                        }
                    }
                }
                questionCodePost.abort();

                HttpPost post = new HttpPost(URLUtils.getLeetcodeProblems() + question.getTitleSlug() + "/submit/");
                JSONObject arg = new JSONObject();
                arg.put("question_id", question.getQuestionId());
                arg.put("lang", question.getLangSlug());
                arg.put("typed_code", code);
                StringEntity entity = new StringEntity(arg.toJSONString());
                post.setEntity(entity);
                post.setHeader("Accept", "application/json");
                post.setHeader("Content-type", "application/json");
                CloseableHttpResponse response = HttpClientUtils.executePost(post);
                if (response != null && response.getStatusLine().getStatusCode() == 200) {
                    String body = EntityUtils.toString(response.getEntity(), "UTF-8");
                    JSONObject returnObj = JSONObject.parseObject(body);
                    cachedThreadPool.execute(new CheckTask(returnObj, codeTypeEnum));
                    MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.INFO, "info", PropertiesUtils.getInfo("request.pending"));
                } else {
                    logger.error("提交失败" + EntityUtils.toString(response.getEntity(), "UTF-8"));
                    MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.ERROR, "error", PropertiesUtils.getInfo("request.failed"));
                }
                post.abort();
                return;
            } catch (IOException i) {
                logger.error("读取代码错误", i);
                MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.ERROR, "error", PropertiesUtils.getInfo("response.code"));
                return;
            }
        }

    }

    private class CheckTask implements Runnable {
        private JSONObject returnObj;
        private CodeTypeEnum codeTypeEnum;

        public CheckTask(JSONObject returnObj, CodeTypeEnum codeTypeEnum) {
            this.returnObj = returnObj;
            this.codeTypeEnum = codeTypeEnum;
        }

        @Override
        public void run() {
            String key = returnObj.getString("submission_id");
            for (int i = 0; i < 50; i++) {
                try {
                    HttpGet httpget = new HttpGet(URLUtils.getLeetcodeSubmissions() + key + "/check/");
                    CloseableHttpResponse response = HttpClientUtils.executeGet(httpget);
                    if (response != null && response.getStatusLine().getStatusCode() == 200) {
                        String body = EntityUtils.toString(response.getEntity(), "UTF-8");
                        JSONObject jsonObject = JSONObject.parseObject(body);
                        if ("SUCCESS".equals(jsonObject.getString("state"))) {
                            if (jsonObject.getBoolean("run_success")) {
                                if (Integer.valueOf(10).equals(jsonObject.getInteger("status_code"))) {
                                    String runtime = jsonObject.getString("status_runtime");
                                    String runtimePercentile = jsonObject.getBigDecimal("runtime_percentile").setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                                    String memory = jsonObject.getString("status_memory");
                                    String memoryPercentile = jsonObject.getBigDecimal("memory_percentile").setScale(2, BigDecimal.ROUND_HALF_UP).toString();

                                    MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.INFO, "info", PropertiesUtils.getInfo("submit.success", runtime, runtimePercentile, codeTypeEnum.getType(), memory, memoryPercentile, codeTypeEnum.getType()));
                                } else {

                                    String input = jsonObject.getString("input");
                                    String output = jsonObject.getString("code_output");
                                    String expected = jsonObject.getString("expected_output");

                                    MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.INFO, "info", PropertiesUtils.getInfo("submit.failed", input, output, expected));
                                }
                            } else {
                                MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.INFO, "info", PropertiesUtils.getInfo("submit.run.failed", jsonObject.getString("compile_error")));
                            }
                            return;
                        }

                    }
                    httpget.abort();
                    Thread.sleep(300L);
                } catch (Exception e) {
                    logger.error("提交出错", e);
                    MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.ERROR, "error", PropertiesUtils.getInfo("request.failed"));
                    return;
                }

            }
            logger.error("等待结果超时");
            MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.INFO, "info", PropertiesUtils.getInfo("response.timeout"));
        }
    }
}
