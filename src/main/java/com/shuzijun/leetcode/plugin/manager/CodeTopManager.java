package com.shuzijun.leetcode.plugin.manager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.shuzijun.lc.http.HttpResponse;
import com.shuzijun.lc.model.CodeMetaData;
import com.shuzijun.lc.model.CodeSnippet;
import com.shuzijun.lc.model.QuestionView;
import com.shuzijun.lc.model.Session;
import com.shuzijun.lc.model.Solution;
import com.shuzijun.lc.model.Submission;
import com.shuzijun.lc.model.User;
import com.shuzijun.leetcode.plugin.model.*;
import com.shuzijun.leetcode.plugin.utils.*;
import org.apache.commons.lang3.StringUtils;

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
        long requestVersion = NavigatorRequestTracker.begin(navigatorAction);
        PageInfo<CodeTopQuestionView> pageInfo = copyPageInfo(navigatorAction.getPageInfo());
        boolean needsFilters = navigatorAction.getFind().getFilter().isEmpty();
        if (ApplicationManager.getApplication().isDispatchThread()) {
            ApplicationManager.getApplication().executeOnPooledThread(
                    () -> loadServiceData(navigatorAction, project, selectTitleSlug, requestVersion, pageInfo, needsFilters));
            return;
        }
        loadServiceData(navigatorAction, project, selectTitleSlug, requestVersion, pageInfo, needsFilters);
    }

    private static void loadServiceData(NavigatorAction navigatorAction, Project project, String selectTitleSlug,
                                        long requestVersion, PageInfo<CodeTopQuestionView> pageInfo, boolean needsFilters) {
        if (project.isDisposed() || !NavigatorRequestTracker.isLatest(navigatorAction, requestVersion)) {
            return;
        }
        QuestionManager.getQuestionAllService(project, false);
        if (project.isDisposed() || !NavigatorRequestTracker.isLatest(navigatorAction, requestVersion)) {
            return;
        }
        PageInfo<CodeTopQuestionView> loadedPageInfo = CodeTopManager.getQuestionService(project, pageInfo);
        if (project.isDisposed() || !NavigatorRequestTracker.isLatest(navigatorAction, requestVersion)) {
            return;
        }
        if ((loadedPageInfo.getRows() == null || loadedPageInfo.getRows().isEmpty()) && loadedPageInfo.getRowTotal() != 0) {
            MessageUtils.getInstance(project).showErrorMsg("error", PropertiesUtils.getInfo("response.question"));
            return;
        }

        List<Tag> difficulties = null;
        List<Tag> tags = null;
        List<Tag> companies = null;
        if (needsFilters) {
            difficulties = CodeTopManager.getDifficulty();
            tags = CodeTopManager.getTags();
            companies = CodeTopManager.getCompany();
        }
        List<Tag> finalDifficulties = difficulties;
        List<Tag> finalTags = tags;
        List<Tag> finalCompanies = companies;
        ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed() || !NavigatorRequestTracker.isLatest(navigatorAction, requestVersion)) {
                return;
            }
            if (needsFilters && navigatorAction.getFind().getFilter().isEmpty()) {
                navigatorAction.getFind().addFilter(Constant.CODETOP_FIND_TYPE_DIFFICULTY, finalDifficulties);
                navigatorAction.getFind().addFilter(Constant.CODETOP_FIND_TYPE_TAGS, finalTags);
                navigatorAction.getFind().addFilter(Constant.CODETOP_FIND_TYPE_COMPANY, finalCompanies);
            }
            applyPageInfo(navigatorAction.getPageInfo(), loadedPageInfo);
            navigatorAction.loadData(selectTitleSlug);
        }, ignored -> project.isDisposed());
    }

    private static PageInfo<CodeTopQuestionView> copyPageInfo(PageInfo<CodeTopQuestionView> pageInfo) {
        PageInfo<CodeTopQuestionView> copy = new PageInfo<>(pageInfo.getPageIndex(), pageInfo.getPageSize());
        PageInfo.Filters sourceFilters = pageInfo.getFilters();
        PageInfo.Filters targetFilters = copy.getFilters();
        targetFilters.setOrderBy(sourceFilters.getOrderBy());
        targetFilters.setSortOrder(sourceFilters.getSortOrder());
        targetFilters.setDifficulty(sourceFilters.getDifficulty());
        targetFilters.setListId(sourceFilters.getListId());
        targetFilters.setTags(sourceFilters.getTags() == null ? null : new ArrayList<>(sourceFilters.getTags()));
        return copy;
    }

    private static void applyPageInfo(PageInfo<CodeTopQuestionView> target, PageInfo<CodeTopQuestionView> source) {
        target.setRowTotal(source.getRowTotal());
        target.setRows(source.getRows());
    }

    private static List<Tag> getCompany() {
        List<Tag> tags = new ArrayList<>();

        HttpResponse response = HttpRequestUtils.get(CodeTopURLUtils.getCompanies());
        if (isSuccessful(response)) {
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
            LogUtils.LOG.error("Request companies failed, status:"
                    + statusCode(response)
                    + " body:" + (response == null ? "" : response.getBody()));
        }
        return tags;
    }

    private static List<Tag> getTags() {

        List<Tag> tags = new ArrayList<>();

        HttpResponse response = HttpRequestUtils.get(CodeTopURLUtils.getTags());
        if (isSuccessful(response)) {
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
            LogUtils.LOG.error("Request tags failed, status:"
                    + statusCode(response)
                    + " body:" + (response == null ? "" : response.getBody()));
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


        HttpResponse response = HttpRequestUtils.get(url);
        if (isSuccessful(response)) {
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
            LogUtils.LOG.error("Request question list failed, status:" + statusCode(response));
            throw new RuntimeException("Request question list failed");
        }

        return pageInfo;
    }

    static boolean isSuccessful(HttpResponse response) {
        return response != null && response.isCodeSuccess();
    }

    private static int statusCode(HttpResponse response) {
        return response == null ? -1 : response.getStatusCode();
    }
}
