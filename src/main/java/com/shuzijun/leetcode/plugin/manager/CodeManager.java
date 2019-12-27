package com.shuzijun.leetcode.plugin.manager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author shuzijun
 */
public class CodeManager {

    private static ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    public static void openCode(Question question, Project project) {
        Config config = PersistentConfig.getInstance().getInitConfig();
        String codeType = config.getCodeType();
        CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnum(codeType);
        if (codeTypeEnum == null) {
            MessageUtils.showWarnMsg("info", PropertiesUtils.getInfo("config.code"));
            return;
        }

        if (!fillQuestion(question)) {
            return;
        }

        String filePath = PersistentConfig.getInstance().getTempFilePath() + VelocityUtils.convert(config.getCustomFileName(), question) + codeTypeEnum.getSuffix();

        File file = new File(filePath);
        if (file.exists()) {

            VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
            OpenFileDescriptor descriptor = new OpenFileDescriptor(project, vf);
            FileEditorManager.getInstance(project).openTextEditor(descriptor, false);
        } else {

            if (getQuestion(question, codeTypeEnum)) {
                question.setContent(CommentUtils.createComment(question.getContent(), codeTypeEnum));
                FileUtils.saveFile(file, VelocityUtils.convert(config.getCustomTemplate(), question));

                VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
                OpenFileDescriptor descriptor = new OpenFileDescriptor(project, vf);
                FileEditorManager.getInstance(project).openTextEditor(descriptor, false);
            }
        }
    }


    public static void openContent(Question question, Project project) {
        Config config = PersistentConfig.getInstance().getInitConfig();
        String codeType = config.getCodeType();
        CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnum(codeType);
        if (codeTypeEnum == null) {
            MessageUtils.showWarnMsg("info", PropertiesUtils.getInfo("config.code"));
            return;
        }

        if (!fillQuestion(question)) {
            return;
        }

        String filePath = PersistentConfig.getInstance().getTempFilePath() + VelocityUtils.convert(config.getCustomFileName(), question) + ".md";

        File file = new File(filePath);
        if (file.exists()) {

            VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
            OpenFileDescriptor descriptor = new OpenFileDescriptor(project, vf);
            FileEditorManager.getInstance(project).openTextEditor(descriptor, false);
        } else {
            if (getQuestion(question, codeTypeEnum)) {
                FileUtils.saveFile(file, question.getContent());

                VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
                OpenFileDescriptor descriptor = new OpenFileDescriptor(project, vf);
                FileEditorManager.getInstance(project).openTextEditor(descriptor, false);
            }

        }
    }

    private static boolean getQuestion(Question question, CodeTypeEnum codeTypeEnum) {
        HttpPost post = null;
        try {
            post = new HttpPost(URLUtils.getLeetcodeGraphql());
            StringEntity entity = new StringEntity("{\"operationName\":\"questionData\",\"variables\":{\"titleSlug\":\"" + question.getTitleSlug() + "\"},\"query\":\"query questionData($titleSlug: String!) {\\n  question(titleSlug: $titleSlug) {\\n    questionId\\n    questionFrontendId\\n    boundTopicId\\n    title\\n    titleSlug\\n    content\\n    translatedTitle\\n    translatedContent\\n    isPaidOnly\\n    difficulty\\n    likes\\n    dislikes\\n    isLiked\\n    similarQuestions\\n    contributors {\\n      username\\n      profileUrl\\n      avatarUrl\\n      __typename\\n    }\\n    langToValidPlayground\\n    topicTags {\\n      name\\n      slug\\n      translatedName\\n      __typename\\n    }\\n    companyTagStats\\n    codeSnippets {\\n      lang\\n      langSlug\\n      code\\n      __typename\\n    }\\n    stats\\n    hints\\n    solution {\\n      id\\n      canSeeDetail\\n      __typename\\n    }\\n    status\\n    sampleTestCase\\n    metaData\\n    judgerAvailable\\n    judgeType\\n    mysqlSchemas\\n    enableRunCode\\n    enableTestMode\\n    envInfo\\n    __typename\\n  }\\n}\\n\"}");
            post.setEntity(entity);
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-type", "application/json");
            CloseableHttpResponse response = HttpClientUtils.executePost(post);
            if (response != null && response.getStatusLine().getStatusCode() == 200) {

                String body = EntityUtils.toString(response.getEntity(), "UTF-8");

                JSONObject jsonObject = JSONObject.parseObject(body).getJSONObject("data").getJSONObject("question");

                question.setContent(getContent(jsonObject));

                question.setTestCase(jsonObject.getString("sampleTestCase"));

                JSONArray jsonArray = jsonObject.getJSONArray("codeSnippets");
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    if (codeTypeEnum.getType().equals(object.getString("lang"))) {
                        question.setLangSlug(object.getString("langSlug"));
                        StringBuffer sb = new StringBuffer();
                        sb.append("\n\n");
                        sb.append(codeTypeEnum.getComment()).append(Constant.SUBMIT_REGION_BEGIN).append("\n");
                        sb.append(object.getString("code").replaceAll("\\n", "\n")).append("\n");
                        sb.append(codeTypeEnum.getComment()).append(Constant.SUBMIT_REGION_END).append("\n");
                        question.setCode(sb.toString());
                        break;
                    }
                }
                return Boolean.TRUE;
            } else {
                MessageUtils.showWarnMsg("error", PropertiesUtils.getInfo("response.code"));
            }

        } catch (Exception e) {
            LogUtils.LOG.error("获取代码失败", e);
            MessageUtils.showWarnMsg("error", PropertiesUtils.getInfo("response.code"));
        } finally {
            if (post != null) {
                post.abort();
            }
        }
        return Boolean.FALSE;
    }

    public static void SubmitCode(Question question) {
        Config config = PersistentConfig.getInstance().getInitConfig();
        String codeType = config.getCodeType();
        CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnum(codeType);
        String code = getCodeText(question, config, codeTypeEnum);
        if (StringUtils.isBlank(code)) {
            return;
        }

        if (!fillQuestion(question)) {
            return;
        }

        HttpPost post = new HttpPost(URLUtils.getLeetcodeProblems() + question.getTitleSlug() + "/submit/");
        try {
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
                cachedThreadPool.execute(new SubmitCheckTask(returnObj, codeTypeEnum, question));
                MessageUtils.showInfoMsg("info", PropertiesUtils.getInfo("request.pending"));
            }else if(response != null && response.getStatusLine().getStatusCode() == 429){
                MessageUtils.showInfoMsg("info", PropertiesUtils.getInfo("request.pending"));
            } else {
                LogUtils.LOG.error("提交失败：url：" + post.getURI().getPath() + ";param:" + arg.toJSONString() + ";body:" + EntityUtils.toString(response.getEntity(), "UTF-8"));
                MessageUtils.showWarnMsg("error", PropertiesUtils.getInfo("request.failed"));
            }
        } catch (IOException i) {
            LogUtils.LOG.error("读取代码错误", i);
            MessageUtils.showWarnMsg("error", PropertiesUtils.getInfo("response.code"));

        } finally {
            post.abort();
        }

    }

    public static void RuncodeCode(Question question) {
        Config config = PersistentConfig.getInstance().getInitConfig();
        String codeType = config.getCodeType();
        CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnum(codeType);

        String code = getCodeText(question, config, codeTypeEnum);
        if (StringUtils.isBlank(code)) {
            return;
        }

        if (!fillQuestion(question)) {
            return;
        }
        HttpPost post = new HttpPost(URLUtils.getLeetcodeProblems() + question.getTitleSlug() + "/interpret_solution/");
        try {
            JSONObject arg = new JSONObject();
            arg.put("question_id", question.getQuestionId());
            arg.put("data_input", question.getTestCase());
            arg.put("lang", question.getLangSlug());
            arg.put("judge_type", "large");
            arg.put("typed_code", code);
            StringEntity entity = new StringEntity(arg.toJSONString());
            post.setEntity(entity);
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-type", "application/json");
            CloseableHttpResponse response = HttpClientUtils.executePost(post);
            if (response != null && response.getStatusLine().getStatusCode() == 200) {

                String body = EntityUtils.toString(response.getEntity(), "UTF-8");
                JSONObject returnObj = JSONObject.parseObject(body);
                cachedThreadPool.execute(new RunCodeCheckTask(returnObj));
                MessageUtils.showInfoMsg("info", PropertiesUtils.getInfo("request.pending"));
            } else {
                LogUtils.LOG.error("提交测试失败" + EntityUtils.toString(response.getEntity(), "UTF-8"));
                MessageUtils.showWarnMsg("error", PropertiesUtils.getInfo("request.failed"));
            }
        } catch (IOException i) {
            MessageUtils.showWarnMsg("error", PropertiesUtils.getInfo("response.code"));
        } finally {
            post.abort();
        }
    }

    private static String getCodeText(Question question, Config config, CodeTypeEnum codeTypeEnum) {
        if (codeTypeEnum == null) {
            MessageUtils.showWarnMsg("info", PropertiesUtils.getInfo("config.code"));
            return null;
        }
        if (!HttpClientUtils.isLogin()) {
            MessageUtils.showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
            return null;
        }
        String filePath = PersistentConfig.getInstance().getTempFilePath() + VelocityUtils.convert(config.getCustomFileName(), question) + codeTypeEnum.getSuffix();
        File file = new File(filePath);
        if (!file.exists()) {
            MessageUtils.showWarnMsg("info", PropertiesUtils.getInfo("request.code"));
            return null;
        } else {
            setTestCaeAndLang(question, codeTypeEnum);
            if (StringUtils.isBlank(question.getTestCase())) {
                return null;
            }

            String code = FileUtils.getClearCommentFileBody(file, codeTypeEnum);
            if (StringUtils.isBlank(code)) {
                MessageUtils.showWarnMsg("info", PropertiesUtils.getInfo("request.empty"));
                return null;
            }


            return code;
        }
    }

    public static void setTestCaeAndLang(Question question, CodeTypeEnum codeTypeEnum) {
        if (codeTypeEnum == null) {
            MessageUtils.showWarnMsg("info", PropertiesUtils.getInfo("config.code"));
            return;
        }
        if (!fillQuestion(question)) {
            return;
        }
        HttpPost questionCodePost = new HttpPost(URLUtils.getLeetcodeGraphql());
        try {
            StringEntity entityCode = new StringEntity("{\"operationName\":\"questionData\",\"variables\":{\"titleSlug\":\"" + question.getTitleSlug() + "\"},\"query\":\"query questionData($titleSlug: String!) {\\n  question(titleSlug: $titleSlug) {\\n    questionId\\n    questionFrontendId\\n    boundTopicId\\n    title\\n    titleSlug\\n    content\\n    translatedTitle\\n    translatedContent\\n    isPaidOnly\\n    difficulty\\n    likes\\n    dislikes\\n    isLiked\\n    similarQuestions\\n    contributors {\\n      username\\n      profileUrl\\n      avatarUrl\\n      __typename\\n    }\\n    langToValidPlayground\\n    topicTags {\\n      name\\n      slug\\n      translatedName\\n      __typename\\n    }\\n    companyTagStats\\n    codeSnippets {\\n      lang\\n      langSlug\\n      code\\n      __typename\\n    }\\n    stats\\n    hints\\n    solution {\\n      id\\n      canSeeDetail\\n      __typename\\n    }\\n    status\\n    sampleTestCase\\n    metaData\\n    judgerAvailable\\n    judgeType\\n    mysqlSchemas\\n    enableRunCode\\n    enableTestMode\\n    envInfo\\n    __typename\\n  }\\n}\\n\"}");
            questionCodePost.setEntity(entityCode);
            questionCodePost.setHeader("Accept", "application/json");
            questionCodePost.setHeader("Content-type", "application/json");
            CloseableHttpResponse responseCode = HttpClientUtils.executePost(questionCodePost);
            if (responseCode != null && responseCode.getStatusLine().getStatusCode() == 200) {
                String body = EntityUtils.toString(responseCode.getEntity(), "UTF-8");

                JSONObject jsonObject = JSONObject.parseObject(body).getJSONObject("data").getJSONObject("question");
                if (StringUtils.isBlank(question.getTestCase())) {
                    question.setTestCase(jsonObject.getString("sampleTestCase"));
                }

                JSONArray jsonArray = jsonObject.getJSONArray("codeSnippets");

                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    if (codeTypeEnum.getType().equals(object.getString("lang"))) {
                        question.setLangSlug(object.getString("langSlug"));
                        break;
                    }
                }
            }
        } catch (IOException i) {
            LogUtils.LOG.error("get test case error", i);
            MessageUtils.showWarnMsg("error", PropertiesUtils.getInfo("response.code"));
            return;
        } finally {
            questionCodePost.abort();
        }

    }

    private static boolean fillQuestion(Question question) {

        if (Constant.NODETYPE_ITEM.equals(question.getNodeType())) {
            ExploreManager.getItem(question);
            if (StringUtils.isBlank(question.getTitleSlug())) {
                MessageUtils.showWarnMsg("info", PropertiesUtils.getInfo("response.restrict"));
                return false;
            } else {
                question.setNodeType(Constant.NODETYPE_DEF);
                return true;
            }
        }
        return true;
    }

    private static String getContent(JSONObject jsonObject) {
        StringBuffer sb = new StringBuffer();
        sb.append(jsonObject.getString(URLUtils.getDescContent()));
        JSONArray topicTagsArray = jsonObject.getJSONArray("topicTags");
        if (topicTagsArray != null && !topicTagsArray.isEmpty()) {
            sb.append("<div><div>Related Topics</div><div>");
            for (int i = 0; i < topicTagsArray.size(); i++) {
                JSONObject tag = topicTagsArray.getJSONObject(i);
                sb.append("<li>");
                if (StringUtils.isBlank(tag.getString("translatedName"))) {
                    sb.append(tag.getString("name"));
                } else {
                    sb.append(tag.getString("translatedName"));
                }
                sb.append("</li>");
            }
            sb.append("</div></div>");
        }
        return sb.toString();
    }

    private static class SubmitCheckTask implements Runnable {

        private Question question;
        private JSONObject returnObj;
        private CodeTypeEnum codeTypeEnum;

        public SubmitCheckTask(JSONObject returnObj, CodeTypeEnum codeTypeEnum, Question question) {
            this.returnObj = returnObj;
            this.codeTypeEnum = codeTypeEnum;
            this.question = question;
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

                                    MessageUtils.showInfoMsg("info", PropertiesUtils.getInfo("submit.success", runtime, runtimePercentile, codeTypeEnum.getType(), memory, memoryPercentile, codeTypeEnum.getType()));
                                    question.setStatus("ac");
                                    ViewManager.updateStatus();
                                } else {

                                    String input = jsonObject.getString("input");
                                    String output = jsonObject.getString("code_output");
                                    String expected = jsonObject.getString("expected_output");
                                    String outputs = StringUtils.join(jsonObject.getJSONArray("code_output"),"\n\t\t");
                                    MessageUtils.showInfoMsg("info", PropertiesUtils.getInfo("submit.failed", input, output, expected,outputs));

                                    MessageUtils.showInfoMsg("info", PropertiesUtils.getInfo("submit.failed", input, output, expected));
                                    if (!"ac".equals(question.getStatus())) {
                                        question.setStatus("notac");
                                        ViewManager.updateStatus();
                                    }
                                }
                            } else {
                                String outputs = StringUtils.join(jsonObject.getJSONArray("code_output"),"\n\t\t");
                                MessageUtils.showInfoMsg("info", PropertiesUtils.getInfo("submit.run.failed", buildErrorMsg(jsonObject),outputs));
                                if (!"ac".equals(question.getStatus())) {
                                    question.setStatus("notac");
                                    ViewManager.updateStatus();
                                }
                            }
                            return;
                        }

                    }
                    httpget.abort();
                    Thread.sleep(300L);
                } catch (Exception e) {
                    LogUtils.LOG.error("提交出错", e);
                    MessageUtils.showWarnMsg("error", PropertiesUtils.getInfo("request.failed"));
                    return;
                }

            }

            MessageUtils.showInfoMsg("info", PropertiesUtils.getInfo("response.timeout"));
        }
    }

    private static String buildErrorMsg(JSONObject errorBody) {
        String statusMsg = errorBody.getString("status_msg");
        if (StringUtils.isNotBlank(statusMsg)) {
            if (statusMsg.equals("Compile Error")) {
                return errorBody.getString("full_compile_error");
            } else if (statusMsg.equals("Runtime Error")) {
                return errorBody.getString("full_runtime_error");
            } else {
                return statusMsg;
            }
        }
        return "Unknown error";
    }


    private static class RunCodeCheckTask implements Runnable {
        private JSONObject returnObj;

        public RunCodeCheckTask(JSONObject returnObj) {
            this.returnObj = returnObj;
        }

        @Override
        public void run() {
            String key = returnObj.getString("interpret_expected_id");
            if(StringUtils.isBlank(key)){
                key = returnObj.getString("interpret_id");
            }
            for (int i = 0; i < 50; i++) {
                String body = null;
                try {
                    HttpGet httpget = new HttpGet(URLUtils.getLeetcodeSubmissions() + key + "/check/");
                    CloseableHttpResponse response = HttpClientUtils.executeGet(httpget);
                    if (response != null && response.getStatusLine().getStatusCode() == 200) {
                        body = EntityUtils.toString(response.getEntity(), "UTF-8");
                        JSONObject jsonObject = JSONObject.parseObject(body);
                        if ("SUCCESS".equals(jsonObject.getString("state"))) {
                            if (!key.equals(returnObj.getString("interpret_id"))) {
                                key = returnObj.getString("interpret_id");
                                returnObj.put("expected_code_answer", jsonObject.getJSONArray("code_answer"));
                            } else {
                                if (jsonObject.getBoolean("run_success")) {
                                    String input = returnObj.getString("test_case");
                                    String output = jsonObject.getJSONArray("code_answer").getString(0);
                                    String expected = "";
                                    if (returnObj.getJSONArray("expected_code_answer") != null && !returnObj.getJSONArray("expected_code_answer").isEmpty()) {
                                        expected = returnObj.getJSONArray("expected_code_answer").getString(0);
                                    } else if (jsonObject.getJSONArray("expected_code_answer") != null && !jsonObject.getJSONArray("expected_code_answer").isEmpty()) {
                                        expected = jsonObject.getJSONArray("expected_code_answer").getString(0);
                                    }
                                    String outputs = StringUtils.join(jsonObject.getJSONArray("code_output"),"\n\t\t");
                                    MessageUtils.showInfoMsg("info", PropertiesUtils.getInfo("test.success", input, output, expected,outputs));
                                } else {
                                    String outputs = StringUtils.join(jsonObject.getJSONArray("code_output"),"\n\t\t");
                                    MessageUtils.showInfoMsg("info", PropertiesUtils.getInfo("submit.run.failed", buildErrorMsg(jsonObject),outputs));
                                }
                                return;
                            }
                        }

                    }
                    httpget.abort();
                    Thread.sleep(300L);
                } catch (Exception e) {
                    LogUtils.LOG.error("提交出错，body:" + body + ",returnObj:" + returnObj.toJSONString(), e);
                    MessageUtils.showWarnMsg("error", PropertiesUtils.getInfo("request.failed"));
                    return;
                }

            }
            MessageUtils.showWarnMsg("info", PropertiesUtils.getInfo("response.timeout"));
        }
    }
}
