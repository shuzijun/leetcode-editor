package com.shuzijun.leetcode.plugin.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.model.HttpRequest;
import com.shuzijun.leetcode.plugin.model.Session;
import com.shuzijun.leetcode.plugin.utils.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shuzijun
 */
public class SessionManager {

    public static List<Session> getSession(Project project) {
        return getSession(project, false);
    }

    public static List<Session> getSession(Project project, boolean cache) {
        List<Session> sessionList = new ArrayList<>();
        HttpResponse httpResponse = HttpRequest.builderGet(URLUtils.getLeetcodeProgress()).cache(cache).request();
        if (httpResponse.getStatusCode() == 200) {

            Session defSession = new Session();
            JSONObject jsonObject = JSON.parseObject(httpResponse.getBody());
            defSession.setName(jsonObject.getString("sessionName"));
            defSession.setXP(jsonObject.getInteger("XP"));
            defSession.setSolvedTotal(jsonObject.getInteger("solvedTotal"));
            defSession.setQuestionTotal(jsonObject.getInteger("questionTotal"));
            defSession.setAttempted(jsonObject.getInteger("attempted"));
            defSession.setPoint(jsonObject.getInteger("leetCoins"));
            defSession.setUnsolved(jsonObject.getInteger("unsolved"));
            defSession.setEasy(jsonObject.getJSONObject("solvedPerDifficulty").getInteger("Easy"));
            defSession.setMedium(jsonObject.getJSONObject("solvedPerDifficulty").getInteger("Medium"));
            defSession.setHard(jsonObject.getJSONObject("solvedPerDifficulty").getInteger("Hard"));
            sessionList.add(defSession);

            JSONArray jsonArray = jsonObject.getJSONArray("sessionList");
            for (int i = 0; i < jsonArray.size(); i++) {
                Session session = new Session();
                session.setId(jsonArray.getJSONObject(i).getInteger("id"));
                session.setName(jsonArray.getJSONObject(i).getString("name"));
                sessionList.add(session);
            }

        } else {
            MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.failed"));
        }
        return sessionList;
    }

    public static boolean switchSession(Project project, Integer id) {

        HttpResponse httpResponse = HttpRequest.builderPut(URLUtils.getLeetcodeSession(), "application/json")
                .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                .addHeader("x-requested-with", "XMLHttpRequest")
                .body("{\"func\":\"activate\",\"target\":" + id + "}").request();
        if (httpResponse.getStatusCode() == 200) {
            return true;
        } else {
            MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.failed"));
            return false;
        }
    }
}
