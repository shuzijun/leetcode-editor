package com.shuzijun.leetcode.plugin.manager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.model.Submission;
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
import java.util.ArrayList;
import java.util.List;

/**
 * @author shuzijun
 */
public class SubmissionManager {

    public static List<Submission> getSubmissionService(Question question) {

        if (!HttpClientUtils.isLogin()) {
            MessageUtils.showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
            return null;
        }

        List<Submission> submissionList = new ArrayList<Submission>();

        HttpPost post = new HttpPost(URLUtils.getLeetcodeGraphql());
        try {

            StringEntity entityCode = new StringEntity("{\"operationName\":\"Submissions\",\"variables\":{\"offset\":0,\"limit\":20,\"lastKey\":null,\"questionSlug\":\"" + question.getTitleSlug() + "\"},\"query\":\"query Submissions($offset: Int!, $limit: Int!, $lastKey: String, $questionSlug: String!) {\\n  submissionList(offset: $offset, limit: $limit, lastKey: $lastKey, questionSlug: $questionSlug) {\\n    lastKey\\n    hasNext\\n    submissions {\\n      id\\n      statusDisplay\\n      lang\\n      runtime\\n      timestamp\\n      url\\n      isPending\\n      memory\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}");
            post.setEntity(entityCode);
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-type", "application/json");
            CloseableHttpResponse responseCode = HttpClientUtils.executePost(post);
            if (responseCode != null && responseCode.getStatusLine().getStatusCode() == 200) {
                String body = EntityUtils.toString(responseCode.getEntity(), "UTF-8");
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
                        MessageUtils.showInfoMsg("info", PropertiesUtils.getInfo("submission.empty"));
                    }
                }
            } else {
                MessageUtils.showWarnMsg("info", PropertiesUtils.getInfo("request.failed"));
            }

        } catch (IOException io) {
            MessageUtils.showWarnMsg("info", PropertiesUtils.getInfo("request.failed"));
        } finally {
            post.abort();
        }
        return submissionList;
    }

    public static void openSubmission(Submission submission, Question question, Project project) {

        if (!HttpClientUtils.isLogin()) {
            MessageUtils.showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
            return;
        }
        CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnum(submission.getLang());
        String filePath = PersistentConfig.getInstance().getTempFilePath() + question.getTitle() + submission.getId() + codeTypeEnum.getSuffix();

        File file = new File(filePath);
        if (file.exists()) {

            VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
            OpenFileDescriptor descriptor = new OpenFileDescriptor(project, vf);
            FileEditorManager.getInstance(project).openTextEditor(descriptor, false);
        } else {

            HttpGet get = new HttpGet(URLUtils.getLeetcodeSubmissions() + submission.getId() + "/");
            try {
                CloseableHttpResponse response = HttpClientUtils.executeGet(get);
                if (response != null && response.getStatusLine().getStatusCode() == 200) {

                    String html = EntityUtils.toString(response.getEntity(), "UTF-8");
                    String body = CommentUtils.createSubmissions(html);
                    if (StringUtils.isBlank(body)) {
                        LogUtils.LOG.error(html);
                        MessageUtils.showWarnMsg("error", PropertiesUtils.getInfo("submission.parse"));
                    } else {
                        try {
                            JSONObject jsonObject = JSONObject.parseObject(body);
                            StringBuffer sb = new StringBuffer();

                            sb.append(jsonObject.getString("submissionCode").replaceAll("\\u000A","\n")).append("\n");

                            JSONObject submissionData= jsonObject.getJSONObject("submissionData");
                            if("Accepted".equals(submission.getStatus())){
                                sb.append(codeTypeEnum.getComment()).append("runtime:").append(submissionData.getString("runtime")).append("\n");
                                sb.append(codeTypeEnum.getComment()).append("memory:").append(submissionData.getString("memory")).append("\n");
                            }else if("Wrong Answer".equals(submission.getStatus())){
                                sb.append(codeTypeEnum.getComment()).append("total_testcases:").append(submissionData.getString("total_testcases")).append("\n");
                                sb.append(codeTypeEnum.getComment()).append("total_correct:").append(submissionData.getString("total_correct")).append("\n");
                                sb.append(codeTypeEnum.getComment()).append("input_formatted:").append(submissionData.getString("input_formatted")).append("\n");
                                sb.append(codeTypeEnum.getComment()).append("expected_output:").append(submissionData.getString("expected_output")).append("\n");
                                sb.append(codeTypeEnum.getComment()).append("code_output:").append(submissionData.getString("code_output")).append("\n");
                            }else if("Runtime Error".equals(submission.getStatus())){
                                sb.append(codeTypeEnum.getComment()).append("runtime_error:").append(submissionData.getString("runtime_error")).append("\n");
                                sb.append(codeTypeEnum.getComment()).append("last_testcase:").append(submissionData.getString("last_testcase").replaceAll("(\\r|\\r\\n|\\n\\r|\\n)"," ")).append("\n");
                            }else{
                                sb.append(codeTypeEnum.getComment()).append("runtime:").append(submissionData.getString("runtime")).append("\n");
                                sb.append(codeTypeEnum.getComment()).append("memory:").append(submissionData.getString("memory")).append("\n");
                                sb.append(codeTypeEnum.getComment()).append("total_testcases:").append(submissionData.getString("total_testcases")).append("\n");
                                sb.append(codeTypeEnum.getComment()).append("total_correct:").append(submissionData.getString("total_correct")).append("\n");
                                sb.append(codeTypeEnum.getComment()).append("input_formatted:").append(submissionData.getString("input_formatted")).append("\n");
                                sb.append(codeTypeEnum.getComment()).append("expected_output:").append(submissionData.getString("expected_output")).append("\n");
                                sb.append(codeTypeEnum.getComment()).append("code_output:").append(submissionData.getString("code_output")).append("\n");
                                sb.append(codeTypeEnum.getComment()).append("runtime_error:").append(submissionData.getString("runtime_error")).append("\n");
                                sb.append(codeTypeEnum.getComment()).append("last_testcase:").append(submissionData.getString("last_testcase").replaceAll("(\\r|\\r\\n|\\n\\r|\\n)"," ")).append("\n");

                            }

                            FileUtils.saveFile(file, sb.toString());

                            VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
                            OpenFileDescriptor descriptor = new OpenFileDescriptor(project, vf);
                            FileEditorManager.getInstance(project).openTextEditor(descriptor, false);

                        } catch (Exception e) {
                            LogUtils.LOG.error(body,e);
                            MessageUtils.showWarnMsg("error", PropertiesUtils.getInfo("submission.parse"));
                        }
                    }

                } else {
                    MessageUtils.showWarnMsg("error", PropertiesUtils.getInfo("request.failed"));
                }

            } catch (Exception e) {
                LogUtils.LOG.error("获取提交详情失败", e);
                MessageUtils.showWarnMsg("error", PropertiesUtils.getInfo("request.failed"));
                return;
            } finally {
                get.abort();
            }

        }
    }
}
