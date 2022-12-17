package com.shuzijun.leetcode.plugin.manager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.model.*;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shuzijun
 */
public class SubmissionManager {

    public static List<Submission> getSubmissionService(String titleSlug, Project project) {

        if (!HttpRequestUtils.isLogin(project)) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
            return null;
        }

        List<Submission> submissionList = new ArrayList<Submission>();

        try {
            HttpResponse response = Graphql.builder().operationName("submissions").variables("offset", 0).variables("limit", 100).variables("questionSlug", titleSlug).request();
            if (response != null && response.getStatusCode() == 200) {
                String body = response.getBody();
                if (StringUtils.isNotBlank(body)) {

                    JSONArray jsonArray = JSONObject.parseObject(body).getJSONObject("data").getJSONObject("submissionList").getJSONArray("submissions");
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        Submission submission = new Submission();
                        submission.setId(object.getString("id"));
                        submission.setStatus(object.getString("statusDisplay"));
                        submission.setLang(object.getString("lang"));
                        submission.setRuntime(object.getString("runtime"));
                        submission.setTime(object.getString("timestamp"));
                        submission.setMemory(object.getString("memory"));
                        submissionList.add(submission);
                    }
                   /* if (submissionList.size() == 0) {
                        MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("submission.empty"));
                    }*/
                }
            } else {
                MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("request.failed"));
            }

        } catch (Exception io) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("request.failed"));
        }
        return submissionList;
    }

    public static File openSubmission(Submission submission, String titleSlug, Project project, Boolean isOpenEditor) {

        if (!HttpRequestUtils.isLogin(project)) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
            return null;
        }
        Config config = PersistentConfig.getInstance().getInitConfig();
        Question question = QuestionManager.getQuestionByTitleSlug(titleSlug, project);
        CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnumByLangSlug(submission.getLang());
        String filePath = PersistentConfig.getInstance().getTempFilePath() + Constant.DOC_SUBMISSION + VelocityUtils.convert(config.getCustomFileName(), question) + submission.getId() + ".txt";

        File file = new File(filePath);
        if (file.exists()) {
            if (isOpenEditor) {
                FileUtils.openFileEditor(file, project);
            }
            return file;
        } else {
            try {

                JSONObject jsonObject;
                if (URLUtils.isCn()) {
                    jsonObject = loadSubmissionCn(submission, project);
                } else {
                    jsonObject = loadSubmissionEn(submission, project);
                }
                if (jsonObject == null) {
                    return file;
                }

                StringBuffer sb = new StringBuffer();

                sb.append(jsonObject.getString("submissionCode").replaceAll("\\u000A", "\n")).append("\n");

                JSONObject submissionData = jsonObject.getJSONObject("submissionData");
                if ("Accepted".equals(submission.getStatus())) {
                    sb.append(codeTypeEnum.getComment()).append("runtime:").append(submissionData.getString("runtime")).append("\n");
                    sb.append(codeTypeEnum.getComment()).append("memory:").append(submissionData.getString("memory")).append("\n");
                } else if ("Wrong Answer".equals(submission.getStatus())) {
                    sb.append(codeTypeEnum.getComment()).append("total_testcases:").append(submissionData.getString("total_testcases")).append("\n");
                    sb.append(codeTypeEnum.getComment()).append("total_correct:").append(submissionData.getString("total_correct")).append("\n");
                    sb.append(codeTypeEnum.getComment()).append("input_formatted:").append(submissionData.getString("input_formatted")).append("\n");
                    sb.append(codeTypeEnum.getComment()).append("expected_output:").append(submissionData.getString("expected_output")).append("\n");
                    sb.append(codeTypeEnum.getComment()).append("code_output:").append(submissionData.getString("code_output")).append("\n");
                    sb.append(codeTypeEnum.getComment()).append("last_testcase:").append(submissionData.getString("last_testcase")).append("\n");
                } else if ("Runtime Error".equals(submission.getStatus())) {
                    sb.append(codeTypeEnum.getComment()).append("runtime_error:").append(submissionData.getString("runtime_error")).append("\n");
                    sb.append(codeTypeEnum.getComment()).append("last_testcase:").append(submissionData.getString("last_testcase").replaceAll("(\\r|\\r\\n|\\n\\r|\\n)", " ")).append("\n");
                } else if ("Compile Error".equals(submission.getStatus())) {
                    sb.append(codeTypeEnum.getComment()).append("total_correct:").append(submissionData.getString("total_correct")).append("\n");
                    sb.append(codeTypeEnum.getComment()).append("compile_error:").append(submissionData.getString("compile_error")).append("\n");
                } else {
                    sb.append(codeTypeEnum.getComment()).append("runtime:").append(submissionData.getString("runtime")).append("\n");
                    sb.append(codeTypeEnum.getComment()).append("memory:").append(submissionData.getString("memory")).append("\n");
                    sb.append(codeTypeEnum.getComment()).append("total_testcases:").append(submissionData.getString("total_testcases")).append("\n");
                    sb.append(codeTypeEnum.getComment()).append("total_correct:").append(submissionData.getString("total_correct")).append("\n");
                    sb.append(codeTypeEnum.getComment()).append("input_formatted:").append(submissionData.getString("input_formatted")).append("\n");
                    sb.append(codeTypeEnum.getComment()).append("expected_output:").append(submissionData.getString("expected_output")).append("\n");
                    sb.append(codeTypeEnum.getComment()).append("code_output:").append(submissionData.getString("code_output")).append("\n");
                    sb.append(codeTypeEnum.getComment()).append("runtime_error:").append(submissionData.getString("runtime_error")).append("\n");
                    if (submissionData.containsKey("last_testcase")) {
                        sb.append(codeTypeEnum.getComment()).append("last_testcase:").append(submissionData.getString("last_testcase").replaceAll("(\\r|\\r\\n|\\n\\r|\\n)", " ")).append("\n");
                    }
                }
                FileUtils.saveFile(file, sb.toString());
                if (isOpenEditor) {
                    FileUtils.openFileEditor(file, project);
                }
                return file;
            } catch (Exception e) {
                LogUtils.LOG.error("获取提交详情失败", e);
                MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.failed"));
                return file;
            }

        }

    }

    private static JSONObject loadSubmissionEn(Submission submission, Project project) {
        HttpResponse response = Graphql.builder().operationName("submissionDetail","submissionDetails").variables("id", submission.getId()).request();
        if (response.getStatusCode() == 200) {
            String body = response.getBody();
            if (StringUtils.isNotBlank(body)) {
                JSONObject jsonObject = new JSONObject();
                JSONObject enObject = JSONObject.parseObject(body).getJSONObject("data").getJSONObject("submissionDetails");

                jsonObject.put("submissionCode", enObject.getString("code"));

                JSONObject submissionData = new JSONObject();
                submissionData.put("runtime", enObject.getString("runtimeDisplay"));
                submissionData.put("memory", enObject.getString("memoryDisplay"));
                submissionData.put("total_testcases", "");
                submissionData.put("total_correct", "");
                submissionData.put("input_formatted", "");
                submissionData.put("expected_output", "");
                submissionData.put("code_output", "");
                submissionData.put("runtime_error", enObject.getString("runtimeError"));
                submissionData.put("last_testcase", enObject.getString("lastTestcase"));
                submissionData.put("compile_error", enObject.getString("compileError"));
                jsonObject.put("submissionData", submissionData);

                return jsonObject;

            }
        } else {
            MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.failed"));
        }
        return null;
    }

    private static JSONObject loadSubmissionCn(Submission submission, Project project) {
        HttpResponse response = Graphql.builder().cn(URLUtils.isCn()).operationName("submissionDetail").variables("id", submission.getId()).request();
        if (response.getStatusCode() == 200) {
            String body = response.getBody();
            if (StringUtils.isNotBlank(body)) {
                JSONObject jsonObject = new JSONObject();
                JSONObject cnObject = JSONObject.parseObject(body).getJSONObject("data").getJSONObject("submissionDetail");

                jsonObject.put("submissionCode", cnObject.getString("code"));

                JSONObject submissionData = new JSONObject();
                submissionData.put("runtime", cnObject.getString("runtime"));
                submissionData.put("memory", cnObject.getString("memory"));
                submissionData.put("total_testcases", cnObject.getString("totalTestCaseCnt"));
                submissionData.put("total_correct", cnObject.getString("passedTestCaseCnt"));
                submissionData.put("input_formatted", cnObject.getJSONObject("outputDetail").getString("input"));
                submissionData.put("expected_output", cnObject.getJSONObject("outputDetail").getString("expectedOutput"));
                submissionData.put("code_output", cnObject.getJSONObject("outputDetail").getString("codeOutput"));
                submissionData.put("runtime_error", cnObject.getJSONObject("outputDetail").getString("runtimeError"));
                submissionData.put("last_testcase", cnObject.getJSONObject("outputDetail").getString("lastTestcase"));
                submissionData.put("compile_error", cnObject.getJSONObject("outputDetail").getString("compileError"));
                jsonObject.put("submissionData", submissionData);

                return jsonObject;

            }
        } else {
            MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.failed"));
        }
        return null;
    }
}
