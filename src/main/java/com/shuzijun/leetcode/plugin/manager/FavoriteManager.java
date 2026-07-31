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
                String error = applyFavoriteResponse(tag, question, response.getBody(), true);
                if (error != null) {
                    MessageUtils.getInstance(project).showWarnMsg("info", error);
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
                String error = applyFavoriteResponse(tag, question, response.getBody(), false);
                if (error != null) {
                    MessageUtils.getInstance(project).showWarnMsg("info", error);
                }
            } else {
                MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("request.failed"));
            }
        } catch (Exception io) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("request.failed"));
        }
    }

    static String applyFavoriteResponse(Tag tag, Question question, String body, boolean add) {
        String operation = add ? "addQuestionToFavorite" : "removeQuestionFromFavorite";
        JSONObject object = JSONObject.parseObject(body).getJSONObject("data").getJSONObject(operation);
        if (!object.getBooleanValue("ok")) {
            return object.getString("error");
        }
        if (add) {
            tag.getQuestions().add(question.getFrontendQuestionId());
        } else {
            tag.getQuestions().remove(question.getFrontendQuestionId());
        }
        return null;
    }
}
