package com.shuzijun.leetcode.plugin.manager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.model.Submission;
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

    public static List<Submission> getSubmissionService(Question question, Project project) {

        if (!HttpRequestUtils.isLogin()) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
            return null;
        }

        List<Submission> submissionList = new ArrayList<Submission>();

        try {
            HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeGraphql(), "application/json");
            httpRequest.setBody("{\"operationName\":\"Submissions\",\"variables\":{\"offset\":0,\"limit\":20,\"lastKey\":null,\"questionSlug\":\"" + question.getTitleSlug() + "\"},\"query\":\"query Submissions($offset: Int!, $limit: Int!, $lastKey: String, $questionSlug: String!) {\\n  submissionList(offset: $offset, limit: $limit, lastKey: $lastKey, questionSlug: $questionSlug) {\\n    lastKey\\n    hasNext\\n    submissions {\\n      id\\n      statusDisplay\\n      lang\\n      runtime\\n      timestamp\\n      url\\n      isPending\\n      memory\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}");
            httpRequest.addHeader("Accept", "application/json");
            HttpResponse response = HttpRequestUtils.executePost(httpRequest);
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
                    if (submissionList.size() == 0) {
                        MessageUtils.getInstance(project).showInfoMsg("info", PropertiesUtils.getInfo("submission.empty"));
                    }
                }
            } else {
                MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("request.failed"));
            }

        } catch (Exception io) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("request.failed"));
        }
        return submissionList;
    }

    public static void openSubmission(Submission submission, Question question, Project project) {

        if (!HttpRequestUtils.isLogin()) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
            return;
        }
        Config config = PersistentConfig.getInstance().getInitConfig();
        CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnumByLangSlug(submission.getLang());
        String filePath = PersistentConfig.getInstance().getTempFilePath() + VelocityUtils.convert(config.getCustomFileName(), question) + submission.getId() + ".txt";

        File file = new File(filePath);
        if (file.exists()) {
            FileUtils.openFileEditor(file, project);
        } else {
            try {

                JSONObject jsonObject;
                if (URLUtils.isCn()) {
                    jsonObject = loadSubmissionCn(submission,project);
                } else {
                    jsonObject = loadSubmissionEn(submission,project);
                }
                if (jsonObject == null) {
                    return;
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
                FileUtils.openFileEditor(file, project);


            } catch (Exception e) {
                LogUtils.LOG.error("获取提交详情失败", e);
                MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.failed"));
                return;
            }

        }

    }

    private static JSONObject loadSubmissionEn(Submission submission,Project project) {
        HttpRequest httpRequest = HttpRequest.get(URLUtils.getLeetcodeSubmissions() + submission.getId() + "/");
        HttpResponse response = HttpRequestUtils.executeGet(httpRequest);
        if (response != null && response.getStatusCode() == 200) {
            String html = response.getBody();
            String body = CommentUtils.createSubmissions(html);
            if (StringUtils.isBlank(body)) {
                LogUtils.LOG.error(html);
                MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("submission.parse"));
            } else {
                try {
                    JSONObject jsonObject = JSONObject.parseObject(body);
                    return jsonObject;
                } catch (Exception e) {
                    LogUtils.LOG.error(body, e);
                    MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("submission.parse"));
                }
            }
        } else {
            MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.failed"));
        }
        return null;
    }

    private static JSONObject loadSubmissionCn(Submission submission,Project project) {
        HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeGraphql(), "application/json");
        httpRequest.setBody("{\"operationName\":\"mySubmissionDetail\",\"variables\":{\"id\":\"" + submission.getId() + "\"},\"query\":\"query mySubmissionDetail($id: ID!) {\\n  submissionDetail(submissionId: $id) {\\n    id\\n    code\\n    runtime\\n    memory\\n    statusDisplay\\n    timestamp\\n    lang\\n    passedTestCaseCnt\\n    totalTestCaseCnt\\n    sourceUrl\\n    question {\\n      titleSlug\\n      title\\n      translatedTitle\\n      questionId\\n      __typename\\n    }\\n    ... on GeneralSubmissionNode {\\n      outputDetail {\\n        codeOutput\\n        expectedOutput\\n        input\\n        compileError\\n        runtimeError\\n        lastTestcase\\n        __typename\\n      }\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}");
        httpRequest.addHeader("Accept", "application/json");
        HttpResponse response = HttpRequestUtils.executePost(httpRequest);
        if (response != null && response.getStatusCode() == 200) {
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
