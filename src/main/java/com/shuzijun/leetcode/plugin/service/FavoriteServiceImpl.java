package com.shuzijun.leetcode.plugin.service;

import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.platform.RepositoryService;
import com.shuzijun.leetcode.platform.model.Graphql;
import com.shuzijun.leetcode.platform.model.HttpResponse;
import com.shuzijun.leetcode.platform.model.Question;
import com.shuzijun.leetcode.platform.model.Tag;
import com.shuzijun.leetcode.platform.repository.FavoriteService;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;

/**
 * @author shuzijun
 */
public class FavoriteServiceImpl implements FavoriteService {

    private final Project project;
    private RepositoryService repositoryService;

    public FavoriteServiceImpl(Project project) {
        this.project = project;
    }

    @Override
    public void registerRepository(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @Override
    public void addQuestionToFavorite(Tag tag, String titleSlug) {
        if (!repositoryService.getHttpRequestService().isLogin(project)) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
            return;
        }
        Question question = repositoryService.getQuestionService().getQuestionByTitleSlug(titleSlug);
        if (question == null) {
            return;
        }

        try {
            HttpResponse response = Graphql.builder(repositoryService).operationName("addQuestionToFavorite").variables("favoriteIdHash", tag.getSlug()).variables("questionId", question.getQuestionId()).request();
            if (response.getStatusCode() == 200) {
                String body = response.getBody();
                JSONObject object = JSONObject.parseObject(body).getJSONObject("data").getJSONObject("addQuestionToFavorite");
                if (object.getBoolean("ok")) {
                    tag.getQuestions().add(question.getFrontendQuestionId());
                } else {
                    MessageUtils.getInstance(project).showWarnMsg("info", object.getString("error"));
                }
            } else {
                MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("request.failed"));
            }
        } catch (Exception io) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("request.failed"));
        }
    }

    @Override
    public void removeQuestionFromFavorite(Tag tag, String titleSlug) {
        if (!repositoryService.getHttpRequestService().isLogin(project)) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
            return;
        }
        Question question = repositoryService.getQuestionService().getQuestionByTitleSlug(titleSlug);
        if (question == null) {
            return;
        }

        try {
            HttpResponse response = Graphql.builder(repositoryService).operationName("removeQuestionFromFavorite").variables("favoriteIdHash", tag.getSlug()).variables("questionId", question.getQuestionId()).request();
            if (response.getStatusCode() == 200) {
                String body = response.getBody();
                JSONObject object = JSONObject.parseObject(body).getJSONObject("data").getJSONObject("removeQuestionFromFavorite");
                if (object.getBoolean("ok")) {
                    tag.getQuestions().remove(question.getFrontendQuestionId());
                } else {
                    MessageUtils.getInstance(project).showWarnMsg("info", object.getString("error"));
                }
            } else {
                MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("request.failed"));
            }
        } catch (Exception io) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("request.failed"));
        }
    }
}
