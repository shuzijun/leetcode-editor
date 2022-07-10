package com.shuzijun.leetcode.plugin.manager;

import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.model.Graphql;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.model.Tag;
import com.shuzijun.leetcode.plugin.utils.*;

/**
 * @author shuzijun
 */
public class FavoriteManager {

    public static void addQuestionToFavorite(Tag tag, String titleSlug, Project project) {
        if (!HttpRequestUtils.isLogin(project)) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
            return;
        }
        Question question = QuestionManager.getQuestionByTitleSlug(titleSlug, project);
        if (question == null) {
            return;
        }

        try {
            HttpResponse response = Graphql.builder().operationName("addQuestionToFavorite")
                    .variables("favoriteIdHash", tag.getSlug()).variables("questionId", question.getQuestionId()).request();
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

    public static void removeQuestionFromFavorite(Tag tag, String titleSlug, Project project) {
        if (!HttpRequestUtils.isLogin(project)) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
            return;
        }
        Question question = QuestionManager.getQuestionByTitleSlug(titleSlug, project);
        if (question == null) {
            return;
        }

        try {
            HttpResponse response = Graphql.builder().operationName("removeQuestionFromFavorite")
                    .variables("favoriteIdHash", tag.getSlug()).variables("questionId", question.getQuestionId()).request();
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
