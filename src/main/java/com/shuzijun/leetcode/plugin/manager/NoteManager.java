package com.shuzijun.leetcode.plugin.manager;

import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.Graphql;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;

import java.io.File;

/**
 * @author shuzijun
 */
public class NoteManager {


    public static File show(String titleSlug, Project project, Boolean isOpenEditor) {
        Config config = PersistentConfig.getInstance().getConfig();
        Question question = QuestionManager.getQuestionByTitleSlug(titleSlug, project);
        String filePath = PersistentConfig.getInstance().getTempFilePath() + Constant.DOC_NOTE + VelocityUtils.convert(config.getCustomFileName(), question) + ".md";
        File file = new File(filePath);
        if (file.exists()) {
            if (isOpenEditor) {
                FileUtils.openFileEditor(file, project);
            }
        } else {
            if (pull(titleSlug, project)) {
                if (isOpenEditor) {
                    FileUtils.openFileEditor(file, project);
                }
            }
        }
        return file;
    }

    public static boolean pull(String titleSlug, Project project) {
        try {
            if (!HttpRequestUtils.isLogin(project)) {
                MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
                return false;
            }
            Question question = QuestionManager.getQuestionByTitleSlug(titleSlug, project);
            Config config = PersistentConfig.getInstance().getConfig();
            String filePath = PersistentConfig.getInstance().getTempFilePath() + Constant.DOC_NOTE + VelocityUtils.convert(config.getCustomFileName(), question) + ".md";

            HttpResponse response = Graphql.builder().operationName("getNote").variables("titleSlug",question.getTitleSlug()).request();
            if (response.getStatusCode() == 200) {

                String body = response.getBody();

                JSONObject jsonObject = JSONObject.parseObject(body).getJSONObject("data").getJSONObject("question");
                FileUtils.saveFile(filePath, jsonObject.getString("note"));
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

    public static void push(String titleSlug, Project project) {
        try {
            if (!HttpRequestUtils.isLogin(project)) {
                MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
                return;
            }

            Config config = PersistentConfig.getInstance().getConfig();
            Question question = QuestionManager.getQuestionByTitleSlug(titleSlug, project);
            String filePath = PersistentConfig.getInstance().getTempFilePath() + Constant.DOC_NOTE + VelocityUtils.convert(config.getCustomFileName(), question) + ".md";
            File file = new File(filePath);
            if (!file.exists()) {
                MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.code"));
                return;
            }
            VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
            FileUtils.saveEditDocument(vf);
            String note = ApplicationManager.getApplication().runReadAction((Computable<String>) () -> FileDocumentManager.getInstance().getDocument(vf).getText());
            HttpResponse response = Graphql.builder().operationName("updateNote").variables("titleSlug",question.getTitleSlug()).variables("content",note).request();
            if (response.getStatusCode() == 200) {
                String body = response.getBody();
                JSONObject jsonObject = JSONObject.parseObject(body).getJSONObject("data").getJSONObject("updateNote");
                if (!jsonObject.getBoolean("ok")) {
                    MessageUtils.getInstance(project).showWarnMsg("error", jsonObject.getString("error"));
                } else {
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
