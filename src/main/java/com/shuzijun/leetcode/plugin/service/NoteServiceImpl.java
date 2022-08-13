package com.shuzijun.leetcode.plugin.service;

import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.shuzijun.leetcode.platform.RepositoryService;
import com.shuzijun.leetcode.platform.repository.NoteService;
import com.shuzijun.leetcode.plugin.model.*;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;

import java.io.File;

/**
 * @author shuzijun
 */
public class NoteServiceImpl implements NoteService {

    private final Project project;
    private RepositoryService repositoryService;

    public NoteServiceImpl(Project project) {
        this.project = project;
    }
    @Override
    public void registerRepository(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @Override
    public File show(String titleSlug, Boolean isOpenEditor) {
        Config config = PersistentConfig.getInstance().getConfig();
        Question question = repositoryService.getQuestionService().getQuestionByTitleSlug(titleSlug);
        String filePath = PersistentConfig.getInstance().getTempFilePath() + Constant.DOC_NOTE + VelocityUtils.convert(config.getCustomFileName(), question) + ".md";
        File file = new File(filePath);
        if (file.exists()) {
            if (isOpenEditor) {
                FileUtils.openFileEditor(file, project);
            }
        } else {
            if (pull(titleSlug)) {
                if (isOpenEditor) {
                    FileUtils.openFileEditor(file, project);
                }
            }
        }
        return file;
    }

    @Override
    public boolean pull(String titleSlug) {
        try {
            if (!repositoryService.getHttpRequestService().isLogin(project)) {
                MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
                return false;
            }
            Question question = repositoryService.getQuestionService().getQuestionByTitleSlug(titleSlug);
            Config config = PersistentConfig.getInstance().getConfig();
            String filePath = PersistentConfig.getInstance().getTempFilePath() + Constant.DOC_NOTE + VelocityUtils.convert(config.getCustomFileName(), question) + ".md";

            HttpResponse response = Graphql.builder(repositoryService).operationName("getNote").variables("titleSlug", question.getTitleSlug()).request();
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

    @Override
    public void push(String titleSlug) {
        try {
            if (!repositoryService.getHttpRequestService().isLogin(project)) {
                MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
                return;
            }

            Config config = PersistentConfig.getInstance().getConfig();
            Question question = repositoryService.getQuestionService().getQuestionByTitleSlug(titleSlug);
            String filePath = PersistentConfig.getInstance().getTempFilePath() + Constant.DOC_NOTE + VelocityUtils.convert(config.getCustomFileName(), question) + ".md";
            File file = new File(filePath);
            if (!file.exists()) {
                MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.code"));
                return;
            }
            VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
            FileUtils.saveEditDocument(vf);
            String note = ApplicationManager.getApplication().runReadAction((Computable<String>) () -> FileDocumentManager.getInstance().getDocument(vf).getText());
            HttpResponse response = Graphql.builder(repositoryService).operationName("updateNote").variables("titleSlug", question.getTitleSlug()).variables("content", note).request();
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
