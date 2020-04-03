package com.shuzijun.leetcode.plugin.manager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.math.BigDecimal;

/**
 * @author shuzijun
 */
public class CodeManager {

    public static void openCode(Question question, Project project) {
        Config config = PersistentConfig.getInstance().getInitConfig();
        String codeType = config.getCodeType();
        CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnum(codeType);
        if (codeTypeEnum == null) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("config.code"));
            return;
        }

        if (!fillQuestion(question, project)) {
            return;
        }

        String filePath = PersistentConfig.getInstance().getTempFilePath() + VelocityUtils.convert(config.getCustomFileName(), question) + codeTypeEnum.getSuffix();

        File file = new File(filePath);
        if (file.exists()) {
            FileUtils.openFileEditorAndSaveState(file,project,question);
        } else {

            if (getQuestion(question, codeTypeEnum, project)) {
                question.setContent(CommentUtils.createComment(question.getContent(), codeTypeEnum));
                FileUtils.saveFile(file, VelocityUtils.convert(config.getCustomTemplate(), question));
                FileUtils.openFileEditorAndSaveState(file,project,question);
            }
        }
    }


    public static void openContent(Question question, Project project) {
        Config config = PersistentConfig.getInstance().getInitConfig();
        String codeType = config.getCodeType();
        CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnum(codeType);
        if (codeTypeEnum == null) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("config.code"));
            return;
        }

        if (!fillQuestion(question, project)) {
            return;
        }

        String filePath = PersistentConfig.getInstance().getTempFilePath() + VelocityUtils.convert(config.getCustomFileName(), question) + ".md";

        File file = new File(filePath);
        if (file.exists()) {
            FileUtils.openFileEditor(file,project);
        } else {
            if (getQuestion(question, codeTypeEnum, project)) {
                FileUtils.saveFile(file, question.getContent());
                FileUtils.openFileEditor(file,project);
            }

        }
    }

    private static boolean getQuestion(Question question, CodeTypeEnum codeTypeEnum, Project project) {
        try {
            HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeGraphql(),"application/json");
            httpRequest.setBody("{\"operationName\":\"questionData\",\"variables\":{\"titleSlug\":\"" + question.getTitleSlug() + "\"},\"query\":\"query questionData($titleSlug: String!) {\\n  question(titleSlug: $titleSlug) {\\n    questionId\\n    questionFrontendId\\n    boundTopicId\\n    title\\n    titleSlug\\n    content\\n    translatedTitle\\n    translatedContent\\n    isPaidOnly\\n    difficulty\\n    likes\\n    dislikes\\n    isLiked\\n    similarQuestions\\n    contributors {\\n      username\\n      profileUrl\\n      avatarUrl\\n      __typename\\n    }\\n    langToValidPlayground\\n    topicTags {\\n      name\\n      slug\\n      translatedName\\n      __typename\\n    }\\n    companyTagStats\\n    codeSnippets {\\n      lang\\n      langSlug\\n      code\\n      __typename\\n    }\\n    stats\\n    hints\\n    solution {\\n      id\\n      canSeeDetail\\n      __typename\\n    }\\n    status\\n    sampleTestCase\\n    metaData\\n    judgerAvailable\\n    judgeType\\n    mysqlSchemas\\n    enableRunCode\\n    enableTestMode\\n    envInfo\\n    __typename\\n  }\\n}\\n\"}");
            httpRequest.addHeader("Accept", "application/json");
            HttpResponse response = HttpRequestUtils.executePost(httpRequest);
            if (response != null && response.getStatusCode() == 200) {

                String body = response.getBody();

                JSONObject jsonObject = JSONObject.parseObject(body).getJSONObject("data").getJSONObject("question");

                question.setContent(getContent(jsonObject));

                question.setTestCase(jsonObject.getString("sampleTestCase"));

                JSONArray jsonArray = jsonObject.getJSONArray("codeSnippets");
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    if (codeTypeEnum.getType().equals(object.getString("lang"))) {
                        question.setLangSlug(object.getString("langSlug"));
                        StringBuffer sb = new StringBuffer();
                        sb.append(codeTypeEnum.getComment()).append(Constant.SUBMIT_REGION_BEGIN).append("\n");
                        sb.append(object.getString("code").replaceAll("\\n", "\n")).append("\n");
                        sb.append(codeTypeEnum.getComment()).append(Constant.SUBMIT_REGION_END).append("\n");
                        question.setCode(sb.toString());
                        break;
                    }
                    if (i == jsonArray.size() - 1) {
                        question.setCode(codeTypeEnum.getComment() + "There is no code of " + codeTypeEnum.getType() + " type for this problem");
                    }
                }
                return Boolean.TRUE;
            } else {
                MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("response.code"));
            }

        } catch (Exception e) {
            LogUtils.LOG.error("获取代码失败", e);
            MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("response.code"));
        }
        return Boolean.FALSE;
    }

    public static void SubmitCode(Question question, Project project) {
        Config config = PersistentConfig.getInstance().getInitConfig();
        String codeType = config.getCodeType();
        CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnum(codeType);
        String code = getCodeText(question, config, codeTypeEnum, project);
        if (StringUtils.isBlank(code)) {
            return;
        }

        if (!fillQuestion(question, project)) {
            return;
        }

        try {
            HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeProblems() + question.getTitleSlug() + "/submit/","application/json");
            JSONObject arg = new JSONObject();
            arg.put("question_id", question.getQuestionId());
            arg.put("lang", question.getLangSlug());
            arg.put("typed_code", code);
            httpRequest.setBody(arg.toJSONString());
            httpRequest.addHeader("Accept", "application/json");
            HttpResponse response = HttpRequestUtils.executePost(httpRequest);
            if (response != null && response.getStatusCode() == 200) {
                String body = response.getBody();
                JSONObject returnObj = JSONObject.parseObject(body);
                ProgressManager.getInstance().run(new SubmitCheckTask(returnObj, codeTypeEnum, question, project));
                MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("request.pending"));
            } else if (response != null && response.getStatusCode() == 429) {
                MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("request.pending"));
            } else {
                LogUtils.LOG.error("提交失败：url：" + httpRequest.getUrl() + ";param:" + arg.toJSONString() + ";body:" + response.getBody());
                MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.failed"));
            }
        } catch (Exception i) {
            LogUtils.LOG.error("SubmitCode error", i);
            MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("response.code"));

        }

    }

    public static void RunCodeCode(Question question, Project project) {
        Config config = PersistentConfig.getInstance().getInitConfig();
        String codeType = config.getCodeType();
        CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnum(codeType);

        String code = getCodeText(question, config, codeTypeEnum, project);
        if (StringUtils.isBlank(code)) {
            return;
        }

        if (!fillQuestion(question, project)) {
            return;
        }

        try {
            HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeProblems() + question.getTitleSlug() + "/interpret_solution/","application/json");
            JSONObject arg = new JSONObject();
            arg.put("question_id", question.getQuestionId());
            arg.put("data_input", question.getTestCase());
            arg.put("lang", question.getLangSlug());
            arg.put("judge_type", "large");
            arg.put("typed_code", code);
            httpRequest.setBody(arg.toJSONString());
            httpRequest.addHeader("Accept", "application/json");
            HttpResponse response = HttpRequestUtils.executePost(httpRequest);
            if (response != null && response.getStatusCode() == 200) {

                String body = response.getBody();
                JSONObject returnObj = JSONObject.parseObject(body);
                ProgressManager.getInstance().run(new RunCodeCheckTask(returnObj, project));
                MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("request.pending"));
            } else {
                LogUtils.LOG.error("RuncodeCode failure " + response.getBody());
                MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.failed"));
            }
        } catch (Exception i) {
            MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("response.code"));
        }
    }

    private static String getCodeText(Question question, Config config, CodeTypeEnum codeTypeEnum, Project project) {
        if (codeTypeEnum == null) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("config.code"));
            return null;
        }
        if (!HttpRequestUtils.isLogin()) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
            return null;
        }
        String filePath = PersistentConfig.getInstance().getTempFilePath() + VelocityUtils.convert(config.getCustomFileName(), question) + codeTypeEnum.getSuffix();
        File file = new File(filePath);
        if (!file.exists()) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("request.code"));
            return null;
        } else {
            setTestCaeAndLang(question, codeTypeEnum, project);
            if (StringUtils.isBlank(question.getTestCase())) {
                return null;
            }

            String code = FileUtils.getClearCommentFileBody(file, codeTypeEnum);
            if (StringUtils.isBlank(code)) {
                MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("request.empty"));
                return null;
            }


            return code;
        }
    }

    public static void setTestCaeAndLang(Question question, CodeTypeEnum codeTypeEnum, Project project) {
        if (codeTypeEnum == null) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("config.code"));
            return;
        }
        if (!fillQuestion(question, project)) {
            return;
        }

        try {
            HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeGraphql(),"application/json");
            httpRequest.setBody("{\"operationName\":\"questionData\",\"variables\":{\"titleSlug\":\"" + question.getTitleSlug() + "\"},\"query\":\"query questionData($titleSlug: String!) {\\n  question(titleSlug: $titleSlug) {\\n    questionId\\n    questionFrontendId\\n    boundTopicId\\n    title\\n    titleSlug\\n    content\\n    translatedTitle\\n    translatedContent\\n    isPaidOnly\\n    difficulty\\n    likes\\n    dislikes\\n    isLiked\\n    similarQuestions\\n    contributors {\\n      username\\n      profileUrl\\n      avatarUrl\\n      __typename\\n    }\\n    langToValidPlayground\\n    topicTags {\\n      name\\n      slug\\n      translatedName\\n      __typename\\n    }\\n    companyTagStats\\n    codeSnippets {\\n      lang\\n      langSlug\\n      code\\n      __typename\\n    }\\n    stats\\n    hints\\n    solution {\\n      id\\n      canSeeDetail\\n      __typename\\n    }\\n    status\\n    sampleTestCase\\n    metaData\\n    judgerAvailable\\n    judgeType\\n    mysqlSchemas\\n    enableRunCode\\n    enableTestMode\\n    envInfo\\n    __typename\\n  }\\n}\\n\"}");
            httpRequest.addHeader("Accept", "application/json");
            HttpResponse response = HttpRequestUtils.executePost(httpRequest);
            if (response != null && response.getStatusCode() == 200) {
                String body = response.getBody();

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
        } catch (Exception i) {
            LogUtils.LOG.error("get test case error", i);
            MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("response.code"));
            return;
        }

    }

    private static boolean fillQuestion(Question question, Project project) {

        if (Constant.NODETYPE_ITEM.equals(question.getNodeType())) {
            ExploreManager.getItem(question);
            if (StringUtils.isBlank(question.getTitleSlug())) {
                MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("response.restrict"));
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

    private static class SubmitCheckTask extends Task.Backgroundable {

        private Question question;
        private JSONObject returnObj;
        private CodeTypeEnum codeTypeEnum;
        private Project project;

        public SubmitCheckTask(JSONObject returnObj, CodeTypeEnum codeTypeEnum, Question question, Project project) {
            super(project,"leetcode.editor.submitCheckTask",true);
            this.returnObj = returnObj;
            this.codeTypeEnum = codeTypeEnum;
            this.question = question;
            this.project = project;
        }

        @Override
        public void run(@NotNull ProgressIndicator progressIndicator) {
            String key = returnObj.getString("submission_id");
            for (int i = 0; i < 50; i++) {
                if(progressIndicator.isCanceled()){
                    MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.cancel"));
                    return;
                }
                try {
                    HttpRequest httpRequest = HttpRequest.get(URLUtils.getLeetcodeSubmissions() + key + "/check/");
                    HttpResponse response = HttpRequestUtils.executeGet(httpRequest);
                    if (response != null && response.getStatusCode() == 200) {
                        String body = response.getBody();
                        JSONObject jsonObject = JSONObject.parseObject(body);
                        if ("SUCCESS".equals(jsonObject.getString("state"))) {
                            if (jsonObject.getBoolean("run_success")) {
                                if (Integer.valueOf(10).equals(jsonObject.getInteger("status_code"))) {
                                    String runtime = jsonObject.getString("status_runtime");
                                    String runtimePercentile = jsonObject.getBigDecimal("runtime_percentile").setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                                    String memory = jsonObject.getString("status_memory");
                                    String memoryPercentile = jsonObject.getBigDecimal("memory_percentile").setScale(2, BigDecimal.ROUND_HALF_UP).toString();

                                    MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("submit.success", runtime, runtimePercentile, codeTypeEnum.getType(), memory, memoryPercentile, codeTypeEnum.getType()));
                                    question.setStatus("ac");
                                    ViewManager.updateStatus();
                                } else {

                                    String input = jsonObject.getString("input");
                                    String output = jsonObject.getString("code_output");
                                    String expected = jsonObject.getString("expected_output");
                                    String outputs = jsonObject.getString("std_output");
                                    MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("submit.failed", input, output, expected, outputs));

                                    if (!"ac".equals(question.getStatus())) {
                                        question.setStatus("notac");
                                        ViewManager.updateStatus();
                                    }
                                }
                            } else {
                                String outputs = StringUtils.join(jsonObject.getJSONArray("code_output"), "\n\t\t");
                                MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("submit.run.failed", buildErrorMsg(jsonObject), outputs));
                                if (!"ac".equals(question.getStatus())) {
                                    question.setStatus("notac");
                                    ViewManager.updateStatus();
                                }
                            }
                            return;
                        }

                    }
                    Thread.sleep(300L);
                } catch (Exception e) {
                    LogUtils.LOG.error("提交出错", e);
                    MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.failed"));
                    return;
                }

            }

            MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("response.timeout"));
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


    private static class RunCodeCheckTask extends Task.Backgroundable  {
        private JSONObject returnObj;
        private Project project;

        public RunCodeCheckTask(JSONObject returnObj, Project project) {
            super(project,"leetcode.editor.runCodeCheckTask",true);
            this.returnObj = returnObj;
            this.project = project;
        }

        @Override
        public void run(@NotNull ProgressIndicator progressIndicator) {
            String key = returnObj.getString("interpret_expected_id");
            if (StringUtils.isBlank(key)) {
                key = returnObj.getString("interpret_id");
            }
            for (int i = 0; i < 50; i++) {
                if(progressIndicator.isCanceled()){
                    MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.cancel"));
                    return;
                }
                String body = null;
                try {
                    HttpRequest httpRequest = HttpRequest.get(URLUtils.getLeetcodeSubmissions() + key + "/check/");
                    HttpResponse response = HttpRequestUtils.executeGet(httpRequest);
                    if (response != null && response.getStatusCode() == 200) {
                        body = response.getBody();
                        JSONObject jsonObject = JSONObject.parseObject(body);
                        if ("SUCCESS".equals(jsonObject.getString("state"))) {
                            if (!key.equals(returnObj.getString("interpret_id"))) {
                                key = returnObj.getString("interpret_id");
                                returnObj.put("expected_code_answer", jsonObject.getJSONArray("code_answer"));
                            } else {
                                if (jsonObject.getBoolean("run_success")) {
                                    String input = returnObj.getString("test_case");
                                    String output = "";
                                    if (jsonObject.getJSONArray("code_answer") != null) {
                                        output = Joiner.on("\n").join(jsonObject.getJSONArray("code_answer"));
                                    }
                                    String expected = "";
                                    if (returnObj.getJSONArray("expected_code_answer") != null && !returnObj.getJSONArray("expected_code_answer").isEmpty()) {
                                        expected = Joiner.on("\n").join(returnObj.getJSONArray("expected_code_answer"));
                                    } else if (jsonObject.getJSONArray("expected_code_answer") != null && !jsonObject.getJSONArray("expected_code_answer").isEmpty()) {
                                        expected = Joiner.on("\n").join(jsonObject.getJSONArray("expected_code_answer"));
                                    }
                                    String outputs = StringUtils.join(jsonObject.getJSONArray("code_output"), "\n\t\t");
                                    MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("test.success", input, output, expected, outputs));
                                } else {
                                    String outputs = StringUtils.join(jsonObject.getJSONArray("code_output"), "\n\t\t");
                                    MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("submit.run.failed", buildErrorMsg(jsonObject), outputs));
                                }
                                return;
                            }
                        }

                    }
                    Thread.sleep(300L);
                } catch (Exception e) {
                    LogUtils.LOG.error("提交出错，body:" + body + ",returnObj:" + returnObj.toJSONString(), e);
                    MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.failed"));
                    return;
                }

            }
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("response.timeout"));
        }
    }
}
