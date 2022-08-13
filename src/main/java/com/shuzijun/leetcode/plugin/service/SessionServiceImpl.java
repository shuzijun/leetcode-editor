package com.shuzijun.leetcode.plugin.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.platform.RepositoryService;
import com.shuzijun.leetcode.platform.repository.SessionService;
import com.shuzijun.leetcode.plugin.model.HttpResponse;
import com.shuzijun.leetcode.plugin.model.Session;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.utils.URLUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shuzijun
 */
public class SessionServiceImpl implements SessionService {


    private final Project project;
    private RepositoryService repositoryService;

    public SessionServiceImpl(Project project) {
        this.project = project;
    }
    @Override
    public void registerRepository(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @Override
    public List<Session> getSession() {
        return getSession(false);
    }

    @Override
    public List<Session> getSession(boolean cache) {
        List<Session> sessionList = new ArrayList<>();
        HttpResponse httpResponse = repositoryService.HttpRequest().get(URLUtils.getLeetcodeProgress()).cache(cache).request();
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

    @Override
    public boolean switchSession(Integer id) {

        HttpResponse httpResponse = repositoryService.HttpRequest().put(URLUtils.getLeetcodeSession(), "application/json")
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
