package com.shuzijun.leetcode.plugin.manager;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.listener.QuestionStatusNotifier;
import com.shuzijun.leetcode.plugin.listener.QuestionSubmitNotifier;
import com.shuzijun.leetcode.plugin.model.*;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.math.BigDecimal;
import java.util.function.BiConsumer;

/**
 * @author shuzijun
 */
public class CodeManager {

    public static void openCode(String titleSlug, Project project) {
        Config config = PersistentConfig.getInstance().getInitConfig();
        CodeTypeEnum codeTypeEnum = config.getCodeTypeEnum(project);
        if (codeTypeEnum == null) {
            return;
        }

        Question question = QuestionManager.getQuestionByTitleSlug(titleSlug, project);
        if (question == null) {
            return;
        }

        if (config.isShowQuestionEditor()) {
            openContent(titleSlug, project, false);
        }

        String filePath = PersistentConfig.getInstance().getTempFilePath() + VelocityUtils.convert(config.getCustomFileName(), question) + codeTypeEnum.getSuffix();

        File file = new File(filePath);
        BiConsumer<LeetcodeEditor, String> fillPath = (e, s) -> e.setPath(s);
        if (file.exists()) {
            FileUtils.openFileEditorAndSaveState(file, project, question, fillPath, true);
        } else {
            String content = question.getContent();
            try{
                question.setLangSlug(codeTypeEnum.getLangSlug());
                question.setContent(CommentUtils.createComment(content, codeTypeEnum, config));
                FileUtils.saveFile(file, VelocityUtils.convert(config.getCustomTemplate(), question));
                FileUtils.openFileEditorAndSaveState(file, project, question, fillPath, true);
            }finally {
                question.setContent(content);
            }

        }
    }


    public static void openContent(String titleSlug, Project project, boolean isOpen) {
        Config config = PersistentConfig.getInstance().getInitConfig();
        Question question = QuestionManager.getQuestionByTitleSlug(titleSlug, project);
        if (question == null) {
            return;
        }

        String filePath = PersistentConfig.getInstance().getTempFilePath() + Constant.DOC_CONTENT + VelocityUtils.convert(config.getCustomFileName(), question) + ".md";

        File file = new File(filePath);
        BiConsumer<LeetcodeEditor, String> fillPath = (e, s) -> e.setContentPath(s);
        if (file.exists()) {
            FileUtils.openFileEditorAndSaveState(file, project, question, fillPath, isOpen);
        } else {
            FileUtils.saveFile(file, question.getContent());
            FileUtils.openFileEditorAndSaveState(file, project, question, fillPath, isOpen);
        }
    }


    public static void SubmitCode(String titleSlug, Project project) {
        Config config = PersistentConfig.getInstance().getInitConfig();
        Question question = QuestionManager.getQuestionByTitleSlug(titleSlug, project);
        if (question == null) {
            return;
        }
        CodeTypeEnum codeTypeEnum = config.getCodeTypeEnum(project);
        String code = getCodeText(question, config, codeTypeEnum, project);
        if (StringUtils.isBlank(code)) {
            return;
        }

        try {
            JSONObject arg = new JSONObject();
            arg.put("question_id", question.getQuestionId());
            arg.put("lang", codeTypeEnum.getLangSlug());
            arg.put("typed_code", code);
            HttpResponse response = HttpRequest.builderPost(URLUtils.getLeetcodeProblems() + question.getTitleSlug() + "/submit/", "application/json")
                    .addHeader("Accept", "application/json").body(arg.toJSONString()).request();
            if (response.getStatusCode() == 200) {
                String body = response.getBody();
                JSONObject returnObj = JSONObject.parseObject(body);
                ProgressManager.getInstance().run(new SubmitCheckTask(returnObj, codeTypeEnum, question, project));
                MessageUtils.getInstance(project).showInfoMsg("", PropertiesUtils.getInfo("request.pending"));
            } else if (response.getStatusCode() == 429) {
                MessageUtils.getInstance(project).showInfoMsg("", PropertiesUtils.getInfo("request.pending"));
            } else {
                LogUtils.LOG.error("提交失败：url：" + URLUtils.getLeetcodeProblems() + question.getTitleSlug() + "/submit/" + ";param:" + arg.toJSONString() + ";body:" + response.getBody());
                MessageUtils.getInstance(project).showWarnMsg("", PropertiesUtils.getInfo("request.failed"));
            }
        } catch (Exception i) {
            LogUtils.LOG.error("SubmitCode error", i);
            MessageUtils.getInstance(project).showWarnMsg("", PropertiesUtils.getInfo("response.code"));

        }

    }

    public static void RunCodeCode(String titleSlug, Project project) {
        Config config = PersistentConfig.getInstance().getInitConfig();
        Question question = QuestionManager.getQuestionByTitleSlug(titleSlug, project);
        if (question == null) {
            return;
        }
        CodeTypeEnum codeTypeEnum = config.getCodeTypeEnum(project);
        String code = getCodeText(question, config, codeTypeEnum, project);
        if (StringUtils.isBlank(code)) {
            return;
        }
        try {
            JSONObject arg = new JSONObject();
            arg.put("question_id", question.getQuestionId());
            arg.put("data_input", question.getTestCase());
            arg.put("lang", codeTypeEnum.getLangSlug());
            arg.put("judge_type", "large");
            arg.put("typed_code", code);
            HttpResponse response = HttpRequest.builderPost(URLUtils.getLeetcodeProblems() + question.getTitleSlug() + "/interpret_solution/", "application/json")
                    .addHeader("Accept", "application/json").body(arg.toJSONString()).request();
            if (response.getStatusCode() == 200) {

                String body = response.getBody();
                JSONObject returnObj = JSONObject.parseObject(body);
                ProgressManager.getInstance().run(new RunCodeCheckTask(returnObj, project, question.getTestCase()));
                MessageUtils.getInstance(project).showInfoMsg("", PropertiesUtils.getInfo("request.pending"));
            } else if (response.getStatusCode() == 429) {
                MessageUtils.getInstance(project).showWarnMsg("", "Please wait for the result.");
            } else {
                LogUtils.LOG.error("RuncodeCode failure " +  response.getBody());
                MessageUtils.getInstance(project).showWarnMsg("", PropertiesUtils.getInfo("request.failed"));
            }
        } catch (Exception i) {
            MessageUtils.getInstance(project).showWarnMsg("", PropertiesUtils.getInfo("response.code"));
        }
    }

    private static String getCodeText(Question question, Config config, CodeTypeEnum codeTypeEnum, Project project) {
        if (codeTypeEnum == null) {
            return null;
        }
        if (!HttpRequestUtils.isLogin(project)) {
            MessageUtils.getInstance(project).showWarnMsg("", PropertiesUtils.getInfo("login.not"));
            return null;
        }
        String filePath = PersistentConfig.getInstance().getTempFilePath() + VelocityUtils.convert(config.getCustomFileName(), question) + codeTypeEnum.getSuffix();
        File file = new File(filePath);
        if (!file.exists()) {
            MessageUtils.getInstance(project).showWarnMsg("", PropertiesUtils.getInfo("request.code"));
            return null;
        } else {
            if (StringUtils.isBlank(question.getTestCase())) {
                return null;
            }

            String code = FileUtils.getClearCommentFileBody(file, codeTypeEnum);
            if (StringUtils.isBlank(code)) {
                MessageUtils.getInstance(project).showWarnMsg("", PropertiesUtils.getInfo("request.empty"));
                return null;
            }


            return code;
        }
    }

    private static class SubmitCheckTask extends Task.Backgroundable {

        private Question question;
        private JSONObject returnObj;
        private CodeTypeEnum codeTypeEnum;
        private Project project;

        public SubmitCheckTask(JSONObject returnObj, CodeTypeEnum codeTypeEnum, Question question, Project project) {
            super(project, PluginConstant.PLUGIN_NAME + ".submitCheckTask", true);
            this.returnObj = returnObj;
            this.codeTypeEnum = codeTypeEnum;
            this.question = question;
            this.project = project;
        }

        @Override
        public void run(@NotNull ProgressIndicator progressIndicator) {
            String key = returnObj.getString("submission_id");
            for (int i = 0; i < 100; i++) {
                if (progressIndicator.isCanceled()) {
                    MessageUtils.getInstance(project).showWarnMsg("", PropertiesUtils.getInfo("request.cancel"));
                    return;
                }
                try {
                    HttpResponse response = HttpRequest.builderGet(URLUtils.getLeetcodeSubmissions() + key + "/check/").request();
                    if (response.getStatusCode() == 200) {
                        String body = response.getBody();
                        JSONObject jsonObject = JSONObject.parseObject(body);
                        if ("SUCCESS".equals(jsonObject.getString("state"))) {
                            if (jsonObject.getBoolean("run_success")) {
                                if (Integer.valueOf(10).equals(jsonObject.getInteger("status_code"))) {
                                    String runtime = jsonObject.getString("status_runtime");
                                    String runtimePercentile = jsonObject.getBigDecimal("runtime_percentile").setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                                    String memory = jsonObject.getString("status_memory");
                                    String memoryPercentile = jsonObject.getBigDecimal("memory_percentile").setScale(2, BigDecimal.ROUND_HALF_UP).toString();

                                    MessageUtils.getInstance(project).showInfoMsg("", PropertiesUtils.getInfo("submit.success", runtime, runtimePercentile, codeTypeEnum.getType(), memory, memoryPercentile, codeTypeEnum.getType()));
                                    question.setStatus("ac");
                                    ApplicationManager.getApplication().getMessageBus().syncPublisher(QuestionStatusNotifier.QUESTION_STATUS_TOPIC).updateTable(question);
                                } else {

                                    String input = jsonObject.getString("input");
                                    if (StringUtils.isNotBlank(input)) {
                                        input = input.replace("\n", "\n\t\t\t");
                                    }
                                    String output = jsonObject.getString("code_output");
                                    String expected = jsonObject.getString("expected_output");
                                    output = MessageUtils.formatDiff(expected, output);
                                    String outputs = jsonObject.getString("std_output");
                                    MessageUtils.getInstance(project).showInfoMsg("", PropertiesUtils.getInfo("submit.failed", input, output, expected, outputs));

                                    if (!"ac".equals(question.getStatus())) {
                                        question.setStatus("notac");
                                        ApplicationManager.getApplication().getMessageBus().syncPublisher(QuestionStatusNotifier.QUESTION_STATUS_TOPIC).updateTable(question);
                                    }
                                }
                            } else {
                                String outputs = jsonObject.getString("std_output");
                                String testcase = jsonObject.getString("last_testcase");
                                if (StringUtils.isNotBlank(testcase)) {
                                    testcase = testcase.replace("\n", "\n\t\t\t");
                                }
                                MessageUtils.getInstance(project).showInfoMsg("", PropertiesUtils.getInfo("submit.run.failed", MessageUtils.format(buildErrorMsg(jsonObject), "E"), testcase, outputs));
                                if (!"ac".equals(question.getStatus())) {
                                    question.setStatus("notac");
                                    ApplicationManager.getApplication().getMessageBus().syncPublisher(QuestionStatusNotifier.QUESTION_STATUS_TOPIC).updateTable(question);
                                }
                            }
                            ApplicationManager.getApplication().getMessageBus().syncPublisher(QuestionSubmitNotifier.TOPIC).submit(URLUtils.getLeetcodeHost(), question.getTitleSlug());
                            return;
                        }

                    }
                    Thread.sleep(300L);
                } catch (Exception e) {
                    LogUtils.LOG.error("提交出错", e);
                    MessageUtils.getInstance(project).showWarnMsg("", PropertiesUtils.getInfo("request.failed"));
                    return;
                }

            }
            ApplicationManager.getApplication().getMessageBus().syncPublisher(QuestionSubmitNotifier.TOPIC).submit(URLUtils.getLeetcodeHost(), question.getTitleSlug());
            MessageUtils.getInstance(project).showInfoMsg("", PropertiesUtils.getInfo("response.timeout"));
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


    private static class RunCodeCheckTask extends Task.Backgroundable {
        private JSONObject returnObj;
        private Project project;
        private String input;

        public RunCodeCheckTask(JSONObject returnObj, Project project, String input) {
            super(project, PluginConstant.PLUGIN_NAME + ".runCodeCheckTask", true);
            this.returnObj = returnObj;
            this.project = project;
            this.input = input;
        }

        @Override
        public void run(@NotNull ProgressIndicator progressIndicator) {
            String key = returnObj.getString("interpret_expected_id");
            if (StringUtils.isBlank(key)) {
                key = returnObj.getString("interpret_id");
            }
            for (int i = 0; i < 100; i++) {
                if (progressIndicator.isCanceled()) {
                    MessageUtils.getInstance(project).showWarnMsg("", PropertiesUtils.getInfo("request.cancel"));
                    return;
                }
                String body = null;
                try {
                    HttpResponse response = HttpRequest.builderGet(URLUtils.getLeetcodeSubmissions() + key + "/check/").request();
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
                                    if (StringUtils.isNotBlank(input)) {
                                        input = input.replace("\n", "\n\t\t\t");
                                    }
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
                                    output = MessageUtils.formatDiff(expected, output);
                                    String outputs = StringUtils.join(jsonObject.getJSONArray("code_output"), "\n\t\t");
                                    MessageUtils.getInstance(project).showInfoMsg("", PropertiesUtils.getInfo("test.success", input, output, expected, outputs));
                                } else {
                                    String outputs = StringUtils.join(jsonObject.getJSONArray("code_output"), "\n\t\t");
                                    String tempInput = input;
                                    if (StringUtils.isNotBlank(tempInput)) {
                                        tempInput = tempInput.replace("\n", "\n\t\t\t");
                                    }
                                    MessageUtils.getInstance(project).showInfoMsg("", PropertiesUtils.getInfo("submit.run.failed", MessageUtils.format(buildErrorMsg(jsonObject), "E"), tempInput, outputs));
                                }
                                return;
                            }
                        }

                    }
                    Thread.sleep(300L);
                } catch (Exception e) {
                    LogUtils.LOG.error("提交出错，body:" + body + ",returnObj:" + returnObj.toJSONString(), e);
                    MessageUtils.getInstance(project).showWarnMsg("", PropertiesUtils.getInfo("request.failed"));
                    return;
                }

            }
            MessageUtils.getInstance(project).showWarnMsg("", PropertiesUtils.getInfo("response.timeout"));
        }
    }
}
