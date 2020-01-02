package com.shuzijun.leetcode.plugin.manager;

import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.model.Tag;
import com.shuzijun.leetcode.plugin.utils.HttpClientUtils;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * @author shuzijun
 */
public class FavoriteManager {

    public static void addQuestionToFavorite(Tag tag, Question question, Project project) {
        if (!HttpClientUtils.isLogin()) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
            return ;
        }
        HttpPost post = new HttpPost(URLUtils.getLeetcodeGraphql());
        try {
            StringEntity entityCode = new StringEntity("{\"operationName\":\"addQuestionToFavorite\",\"variables\":{\"favoriteIdHash\":\""+tag.getSlug()+"\",\"questionId\":\""+question.getQuestionId()+"\"},\"query\":\"mutation addQuestionToFavorite($favoriteIdHash: String!, $questionId: String!) {\\n  addQuestionToFavorite(favoriteIdHash: $favoriteIdHash, questionId: $questionId) {\\n    ok\\n    error\\n    favoriteIdHash\\n    questionId\\n    __typename\\n  }\\n}\\n\"}");
            post.setEntity(entityCode);
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-type", "application/json");
            CloseableHttpResponse responseCode = HttpClientUtils.executePost(post);

            if (responseCode != null && responseCode.getStatusLine().getStatusCode() == 200) {
                String body = EntityUtils.toString(responseCode.getEntity(), "UTF-8");
                JSONObject object = JSONObject.parseObject(body).getJSONObject("data").getJSONObject("addQuestionToFavorite");
                if (object.getBoolean("ok")) {
                    tag.getQuestions().add(question.getQuestionId());
                } else {
                    MessageUtils.getInstance(project).showWarnMsg("info", object.getString("error"));
                }
            } else {
                MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("request.failed"));
            }
        } catch (IOException io) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("request.failed"));
        } finally {
            post.abort();
        }
    }

    public static void removeQuestionFromFavorite(Tag tag, Question question,Project project) {
        if (!HttpClientUtils.isLogin()) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
            return ;
        }
        HttpPost post = new HttpPost(URLUtils.getLeetcodeGraphql());
        try {
            StringEntity entityCode = new StringEntity("{\"operationName\":\"removeQuestionFromFavorite\",\"variables\":{\"favoriteIdHash\":\"" + tag.getSlug() + "\",\"questionId\":\"" + question.getQuestionId() + "\"},\"query\":\"mutation removeQuestionFromFavorite($favoriteIdHash: String!, $questionId: String!) {\\n  removeQuestionFromFavorite(favoriteIdHash: $favoriteIdHash, questionId: $questionId) {\\n    ok\\n    error\\n    favoriteIdHash\\n    questionId\\n    __typename\\n  }\\n}\\n\"}");
            post.setEntity(entityCode);
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-type", "application/json");
            CloseableHttpResponse responseCode = HttpClientUtils.executePost(post);

            if (responseCode != null && responseCode.getStatusLine().getStatusCode() == 200) {
                String body = EntityUtils.toString(responseCode.getEntity(), "UTF-8");
                JSONObject object = JSONObject.parseObject(body).getJSONObject("data").getJSONObject("removeQuestionFromFavorite");
                if (object.getBoolean("ok")) {
                    tag.getQuestions().remove(question.getQuestionId());
                } else {
                    MessageUtils.getInstance(project).showWarnMsg("info", object.getString("error"));
                }
            } else {
                MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("request.failed"));
            }
        } catch (IOException io) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("request.failed"));
        } finally {
            post.abort();
        }
    }
}
