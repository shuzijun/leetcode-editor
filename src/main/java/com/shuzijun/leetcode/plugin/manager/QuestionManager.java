package com.shuzijun.leetcode.plugin.manager;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.shuzijun.lc.errors.LcException;
import com.shuzijun.leetcode.plugin.application.LeetCodeApiService;
import com.shuzijun.leetcode.plugin.application.LeetCodeServices;
import com.shuzijun.lc.model.CodeMetaData;
import com.shuzijun.lc.model.CodeSnippet;
import com.shuzijun.lc.model.QuestionView;
import com.shuzijun.lc.model.Session;
import com.shuzijun.lc.model.Solution;
import com.shuzijun.lc.model.Submission;
import com.shuzijun.lc.model.User;
import com.shuzijun.leetcode.plugin.model.*;
import com.shuzijun.leetcode.plugin.utils.*;
import com.shuzijun.leetcode.plugin.window.WindowFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author shuzijun
 */
public class QuestionManager {

    public static PageInfo<QuestionView> getQuestionViewList(Project project, PageInfo<QuestionView> pageInfo) {
        LogUtils.navigatorTrace("getQuestionViewList:request"
                + " page=" + pageInfo.getPageIndex()
                + " skip=" + pageInfo.getSkip()
                + " limit=" + pageInfo.getPageSize()
                + " category=" + pageInfo.getCategorySlug()
                + " filters=" + pageInfo.getFilters());
        User user = WindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_PROJECTS_TABS).getUser();
        try {
            apiService().loadQuestionPage(pageInfo, user);
            List<QuestionView> questionList = pageInfo.getRows();
            QuestionView dayQuestion = questionOfToday();
            if (dayQuestion != null) {
                questionList.add(0, dayQuestion);
            }

            LogUtils.navigatorTrace("getQuestionViewList:response"
                    + " page=" + pageInfo.getPageIndex()
                    + " total=" + pageInfo.getRowTotal()
                    + " questionRows=" + (dayQuestion == null ? questionList.size() : questionList.size() - 1)
                    + " dailyQuestion=" + (dayQuestion != null)
                    + " displayedRows=" + questionList.size());
        } catch (LcException exception) {
            LogUtils.navigatorTrace("getQuestionViewList:failed page=" + pageInfo.getPageIndex()
                    + " skip=" + pageInfo.getSkip());
            LogUtils.LOG.error("Request question list failed", exception);
            throw new RuntimeException("Request question list failed", exception);
        }

        return pageInfo;

    }

    public static List<QuestionView> getQuestionAllService(Project project, boolean reset) {
        User user = WindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_PROJECTS_TABS).getUser();
        try {
            return apiService().loadAllQuestions(user, reset);
        } catch (LcException exception) {
            LogUtils.LOG.error("Request all questions failed", exception);
            return null;
        }
    }

    public static QuestionIndex getQuestionIndex(String titleSlug) {
        return apiService().getQuestionIndex(titleSlug);
    }

    public static void invalidateCaches() {
        LeetCodeApiService.invalidateCaches();
    }

    public static void invalidateCaches(String host) {
        LeetCodeApiService.invalidateCaches(host);
    }

    public static QuestionView questionOfToday() {
        try {
            return apiService().loadQuestionOfToday();
        } catch (Exception ignore) {
            return null;
        }
    }


    private static Question loadQuestion(String titleSlug, Project project) {
        try {
            return apiService().loadQuestion(titleSlug);
        } catch (Exception e) {
            LogUtils.LOG.error("获取代码失败", e);
            MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("response.code"));
        }
        return null;
    }

    public static Question pick(Project project, PageInfo<?> pageInfo) {
        String titleSlug;
        try {
            titleSlug = apiService().pickQuestion(pageInfo);
        } catch (LcException exception) {
            LogUtils.LOG.error("Request random question failed", exception);
            return null;
        }
        if (StringUtils.isNotBlank(titleSlug)) {
            return getQuestionByTitleSlug(titleSlug, project);
        } else {
            return null;
        }

    }

    public static User getUser() {
        return apiService().loadUser();
    }

    public static Question getQuestionByTitleSlug(String titleSlug, Project project) {
        return getQuestionByTitleSlug(titleSlug,project, false);
    }

    public static Question getCachedQuestionByTitleSlug(String titleSlug, String host) {
        if (StringUtils.isBlank(titleSlug) || StringUtils.isBlank(host)) {
            return null;
        }
        return apiService().getCachedQuestion(titleSlug, host);
    }

    public static Question getQuestionByTitleSlug(String titleSlug, Project project, boolean readOnlyCache) {

        if (StringUtils.isBlank(titleSlug)) {
            return null;
        }
        Question cachedQuestion = apiService().getCachedQuestion(titleSlug, URLUtils.getLeetcodeHost());
        if (cachedQuestion != null || readOnlyCache) {
            return cachedQuestion;
        }
        if (ApplicationManager.getApplication().isDispatchThread()) {
            LogUtils.LOG.warn("Skipped loading question on the IDEA UI thread: " + titleSlug);
            return null;
        }
        return loadQuestion(titleSlug, project);
    }

    private static LeetCodeApiService apiService() {
        return LeetCodeServices.api();
    }
}
