package com.shuzijun.leetcode.plugin.manager;

import com.intellij.openapi.project.Project;
import com.shuzijun.lc.model.FavoriteResult;
import com.shuzijun.leetcode.plugin.application.LeetCodeFavoriteService;
import com.shuzijun.leetcode.plugin.application.LeetCodeServices;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.model.Tag;
import com.shuzijun.leetcode.plugin.utils.*;

/**
 * @author shuzijun
 */
public class FavoriteManager {

    public static boolean addQuestionToFavorite(Tag tag, String titleSlug, Project project) {
        if (!LeetCodeServices.login().isLoggedIn()) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
            return false;
        }
        Question question = QuestionManager.getQuestionByTitleSlug(titleSlug, project);
        if (question == null) {
            return false;
        }

        try {
            String error = applyFavoriteResult(
                    tag,
                    question,
                    favoriteService().add(tag.getSlug(), question.getQuestionId()),
                    true
            );
            if (error != null) {
                MessageUtils.getInstance(project).showWarnMsg("info", error);
                return false;
            }
            return true;
        } catch (Exception exception) {
            LogUtils.LOG.error("添加收藏失败", exception);
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("request.failed"));
            return false;
        }
    }

    public static boolean removeQuestionFromFavorite(Tag tag, String titleSlug, Project project) {
        if (!LeetCodeServices.login().isLoggedIn()) {
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
            return false;
        }
        Question question = QuestionManager.getQuestionByTitleSlug(titleSlug, project);
        if (question == null) {
            return false;
        }

        try {
            String error = applyFavoriteResult(
                    tag,
                    question,
                    favoriteService().remove(tag.getSlug(), question.getQuestionId()),
                    false
            );
            if (error != null) {
                MessageUtils.getInstance(project).showWarnMsg("info", error);
                return false;
            }
            return true;
        } catch (Exception exception) {
            LogUtils.LOG.error("移除收藏失败", exception);
            MessageUtils.getInstance(project).showWarnMsg("info", PropertiesUtils.getInfo("request.failed"));
            return false;
        }
    }

    static String applyFavoriteResult(
            Tag tag,
            Question question,
            FavoriteResult result,
            boolean add
    ) {
        if (!result.isOk()) {
            return result.getError();
        }
        if (add) {
            tag.getQuestions().add(question.getFrontendQuestionId());
        } else {
            tag.getQuestions().remove(question.getFrontendQuestionId());
        }
        return null;
    }

    private static LeetCodeFavoriteService favoriteService() {
        return LeetCodeServices.favorite();
    }
}
