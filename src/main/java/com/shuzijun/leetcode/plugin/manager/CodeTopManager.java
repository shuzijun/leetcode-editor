package com.shuzijun.leetcode.plugin.manager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.model.*;
import com.shuzijun.leetcode.plugin.utils.*;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shuzijun
 */
public class CodeTopManager {

    public static void loadServiceData(NavigatorAction navigatorAction, Project project) {
        loadServiceData(navigatorAction, project, null);
    }

    public static void loadServiceData(NavigatorAction navigatorAction, Project project, String selectTitleSlug) {
        QuestionManager.getQuestionAllService(project, false);
        PageInfo pageInfo = CodeTopManager.getQuestionService(project, navigatorAction.getPageInfo());
        if ((pageInfo.getRows() == null || pageInfo.getRows().isEmpty()) && pageInfo.getRowTotal() != 0) {
            MessageUtils.getInstance(project).showErrorMsg("error", PropertiesUtils.getInfo("response.question"));
            return;
        }

        if (navigatorAction.getFind().getFilter().isEmpty()) {
            navigatorAction.getFind().addFilter(Constant.CODETOP_FIND_TYPE_DIFFICULTY, CodeTopManager.getDifficulty());
            navigatorAction.getFind().addFilter(Constant.CODETOP_FIND_TYPE_TAGS, CodeTopManager.getTags());
            navigatorAction.getFind().addFilter(Constant.CODETOP_FIND_TYPE_COMPANY, CodeTopManager.getCompany());
        }
        navigatorAction.loadData(selectTitleSlug);
    }

    private static List<Tag> getCompany() {
        List<Tag> tags = new ArrayList<>();

        HttpResponse response = HttpRequest.builderGet(CodeTopURLUtils.getCompanies()).request();
        if (response != null && response.getStatusCode() == 200) {
            try {
                String body = response.getBody();
                if (StringUtils.isNotBlank(body)) {
                    JSONArray jsonArray = JSONObject.parseArray(body);
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        Tag tag = new Tag();
                        tag.setName(object.getString("name"));
                        tag.setSlug(object.getString("id"));
                        tags.add(tag);
                    }
                }
            } catch (Exception e1) {
                LogUtils.LOG.error("Request companies exception", e1);
            }
        } else {
            LogUtils.LOG.error("Request companies failed, status:" + response.getStatusCode() + "body:" + response.getBody());
        }
        return tags;
    }

    private static List<Tag> getTags() {

        List<Tag> tags = new ArrayList<>();

        HttpResponse response = HttpRequest.builderGet(CodeTopURLUtils.getTags()).request();
        if (response.getStatusCode() == 200) {
            try {
                String body = response.getBody();
                if (StringUtils.isNotBlank(body)) {
                    JSONArray jsonArray = JSONObject.parseArray(body);
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        Tag tag = new Tag();
                        tag.setName(object.getString("name"));
                        tag.setSlug(object.getString("id"));
                        tags.add(tag);
                    }
                }
            } catch (Exception e1) {
                LogUtils.LOG.error("Request tags exception", e1);
            }
        } else {
            LogUtils.LOG.error("Request tags failed, status:" + response.getStatusCode() + "body:" + response.getBody());
        }
        return tags;
    }

    private static List<Tag> getDifficulty() {
        List<String> keyList = Lists.newArrayList(Constant.DIFFICULTY_EASY, Constant.DIFFICULTY_MEDIUM, Constant.DIFFICULTY_HARD);
        List<Tag> difficultyList = Lists.newArrayList();
        for (int i = 0; i < keyList.size(); i++) {
            Tag tag = new Tag();
            tag.setName(keyList.get(i));
            tag.setSlug("" + (i + 1));
            difficultyList.add(tag);
        }
        return difficultyList;
    }

    private static PageInfo<CodeTopQuestionView> getQuestionService(Project project, PageInfo pageInfo) {
        String url = CodeTopURLUtils.getQuestions() + "?page=" + pageInfo.getPageIndex();
        PageInfo.Filters filters = pageInfo.getFilters();
        if (StringUtils.isNotBlank(filters.getOrderBy())) {
            url = url + "&ordering=" + ("DESCENDING".equals(filters.getSortOrder()) ? "-" : "") + filters.getOrderBy();
        }
        if (StringUtils.isNotBlank(filters.getDifficulty())) {
            url = url + "&leetcode__level=" + filters.getDifficulty();
        }

        if (filters.getTags() != null && !filters.getTags().isEmpty()) {
            url = url + "&leetcode__tags=" + filters.getTags().get(0);
        }

        if (StringUtils.isNotBlank(filters.getListId())) {
            url = url + "&company=" + filters.getListId();
        }


        HttpResponse response = HttpRequest.builderGet(url).request();
        if (response.getStatusCode() == 200) {
            List<CodeTopQuestionView> questionList = new ArrayList();
            JSONObject pageObject = JSONObject.parseObject(response.getBody());
            JSONArray questionJsonArray = pageObject.getJSONArray("list");
            for (int i = 0; i < pageObject.getJSONArray("list").size(); i++) {
                JSONObject codeTopQuestionJsonObject = questionJsonArray.getJSONObject(i);
                JSONObject questionJsonObject = codeTopQuestionJsonObject.getJSONObject("leetcode");
                CodeTopQuestionView question = new CodeTopQuestionView();
                question.setTitle(questionJsonObject.getString("title"));
                question.setFrontendQuestionId(questionJsonObject.getString("frontend_question_id"));
                question.setLevel(questionJsonObject.getString("level"));
                question.setTitleSlug(questionJsonObject.getString("slug_title"));
                question.setInspectFrequency(codeTopQuestionJsonObject.getInteger("value"));
                String time = codeTopQuestionJsonObject.getString("time");
                if (StringUtils.isNotBlank(time) && time.length() > 10) {
                    question.setInspectTime(time.substring(0, 10));
                } else {
                    question.setInspectTime(time);
                }
                QuestionIndex questionIndex = QuestionManager.getQuestionIndex(question.getTitleSlug());
                if (questionIndex != null) {
                    question.setStatus(questionIndex.getQuestionView().getStatus());
                }
                questionList.add(question);
            }

            pageInfo.setRowTotal(pageObject.getInteger("count"));
            pageInfo.setRows(questionList);
        } else {
            LogUtils.LOG.error("Request question list failed, status:" + response == null ? "" : response.getStatusCode());
            throw new RuntimeException("Request question list failed");
        }

        return pageInfo;
    }
}
