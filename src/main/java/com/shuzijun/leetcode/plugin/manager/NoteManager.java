package com.shuzijun.leetcode.plugin.manager;

import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;

import java.io.File;

/**
 * @author shuzijun
 */
public class NoteManager {


    public static void show(Question question, Project project) {
        Config config = PersistentConfig.getInstance().getInitConfig();
        String filePath = PersistentConfig.getInstance().getTempFilePath() + Constant.DOC_NOTE  + VelocityUtils.convert(config.getCustomFileName(), question) + ".md";
        File file = new File(filePath);
        if (file.exists()) {
            FileUtils.openFileEditor(file,project);
        }else {
            if(pull( question,  project)){
                FileUtils.openFileEditor(file,project);
            }
        }
    }
    public static boolean pull(Question question, Project project) {
        try {
            if (!HttpRequestUtils.isLogin()) {
                MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
                return false;
            }

            Config config = PersistentConfig.getInstance().getInitConfig();
            String filePath = PersistentConfig.getInstance().getTempFilePath() + Constant.DOC_NOTE  + VelocityUtils.convert(config.getCustomFileName(), question) + ".md";

            HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeGraphql(),"application/json");
            httpRequest.setBody("{\"operationName\":\"QuestionNote\",\"variables\":{\"titleSlug\":\""+question.getTitleSlug()+"\"},\"query\":\"query QuestionNote($titleSlug: String!) {\\n  question(titleSlug: $titleSlug) {\\n    questionId\\n    note\\n    __typename\\n  }\\n}\\n\"}");
            httpRequest.addHeader("Accept", "application/json");
            HttpResponse response = HttpRequestUtils.executePost(httpRequest);
            if (response != null && response.getStatusCode() == 200) {

                String body = response.getBody();

                JSONObject jsonObject = JSONObject.parseObject(body).getJSONObject("data").getJSONObject("question");
                FileUtils.saveFile(filePath,jsonObject.getString("note"));
                return Boolean.TRUE;
            } else {
                MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.failed"));
            }

        } catch (Exception e) {
            LogUtils.LOG.error("pull node error", e);
            MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.failed"));
        }
        return Boolean.FALSE;
    }

    public static void push(Question question, Project project) {
        try {
            if (!HttpRequestUtils.isLogin()) {
                MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
                return;
            }

            Config config = PersistentConfig.getInstance().getInitConfig();
            String filePath = PersistentConfig.getInstance().getTempFilePath() + Constant.DOC_NOTE  + VelocityUtils.convert(config.getCustomFileName(), question) + ".md";
            File file = new File(filePath);
            if (!file.exists()) {
                MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.code"));
                return;
            }
            VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
            FileUtils.saveEditDocument(vf);
            String note = FileDocumentManager.getInstance().getDocument(vf).getText();
            HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeGraphql(),"application/json");
            JSONObject variables = new JSONObject();
            variables.put("titleSlug",question.getTitleSlug());
            variables.put("content",note);
            httpRequest.setBody("{\"operationName\":\"updateNote\",\"variables\":"+variables.toJSONString()+",\"query\":\"mutation updateNote($titleSlug: String!, $content: String!) {\\n  updateNote(titleSlug: $titleSlug, content: $content) {\\n    ok\\n    error\\n    question {\\n      questionId\\n      note\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}");
            httpRequest.addHeader("Accept", "application/json");
            HttpResponse response = HttpRequestUtils.executePost(httpRequest);
            if (response != null && response.getStatusCode() == 200) {
                String body = response.getBody();
                JSONObject jsonObject = JSONObject.parseObject(body).getJSONObject("data").getJSONObject("updateNote");
                if(!jsonObject.getBoolean("ok")){
                    MessageUtils.getInstance(project).showWarnMsg("error", jsonObject.getString("error"));
                }else {
                    MessageUtils.getInstance(project).showInfoMsg("info", "success");
                }
            } else {
                MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.failed"));
            }
        } catch (Exception e) {
            LogUtils.LOG.error("pull node error", e);
            MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.failed"));
        }
    }

}
