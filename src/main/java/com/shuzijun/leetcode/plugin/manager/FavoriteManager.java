package com.shuzijun.leetcode.plugin.manager;

import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.model.Tag;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;

/**
 * @author shuzijun
 */
public class FavoriteManager {

    public static void addQuestionToFavorite(Tag tag, Question question, Project project) {
        if (!HttpRequestUtils.isLogin()) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
            return ;
        }
        CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnum(PersistentConfig.getInstance().getInitConfig().getCodeType());
        if (codeTypeEnum == null) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("config.code"));
            return;
        }

        if(!CodeManager.fillQuestion(question,codeTypeEnum,project)){
            return;
        }

        try {
            HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeGraphql(),"application/json");
            httpRequest.setBody("{\"operationName\":\"addQuestionToFavorite\",\"variables\":{\"favoriteIdHash\":\""+tag.getSlug()+"\",\"questionId\":\""+question.getQuestionId()+"\"},\"query\":\"mutation addQuestionToFavorite($favoriteIdHash: String!, $questionId: String!) {\\n  addQuestionToFavorite(favoriteIdHash: $favoriteIdHash, questionId: $questionId) {\\n    ok\\n    error\\n    favoriteIdHash\\n    questionId\\n    __typename\\n  }\\n}\\n\"}");
            httpRequest.addHeader("Accept", "application/json");
            HttpResponse response = HttpRequestUtils.executePost(httpRequest);

            if (response != null && response.getStatusCode() == 200) {
                String body = response.getBody();
                JSONObject object = JSONObject.parseObject(body).getJSONObject("data").getJSONObject("addQuestionToFavorite");
                if (object.getBoolean("ok")) {
                    tag.getFrontendQuestionId().add(question.getFrontendQuestionId());
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

    public static void removeQuestionFromFavorite(Tag tag, Question question,Project project) {
        if (!HttpRequestUtils.isLogin()) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
            return ;
        }
        CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnum(PersistentConfig.getInstance().getInitConfig().getCodeType());
        if (codeTypeEnum == null) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("config.code"));
            return;
        }

        if(!CodeManager.fillQuestion(question,codeTypeEnum,project)){
            return;
        }

        try {
            HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeGraphql(),"application/json");
            httpRequest.setBody("{\"operationName\":\"removeQuestionFromFavorite\",\"variables\":{\"favoriteIdHash\":\"" + tag.getSlug() + "\",\"questionId\":\"" + question.getQuestionId() + "\"},\"query\":\"mutation removeQuestionFromFavorite($favoriteIdHash: String!, $questionId: String!) {\\n  removeQuestionFromFavorite(favoriteIdHash: $favoriteIdHash, questionId: $questionId) {\\n    ok\\n    error\\n    favoriteIdHash\\n    questionId\\n    __typename\\n  }\\n}\\n\"}");
            httpRequest.addHeader("Accept", "application/json");
            HttpResponse response = HttpRequestUtils.executePost(httpRequest);

            if (response != null && response.getStatusCode() == 200) {
                String body = response.getBody();
                JSONObject object = JSONObject.parseObject(body).getJSONObject("data").getJSONObject("removeQuestionFromFavorite");
                if (object.getBoolean("ok")) {
                    tag.getFrontendQuestionId().remove(question.getFrontendQuestionId());
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
