package com.shuzijun.leetcode.plugin.manager;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.model.*;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author shuzijun
 */
public class ViewManager {

    public static void loadServiceData(NavigatorAction navigatorAction, Project project) {
        loadServiceData(navigatorAction, project, null);
    }

    public static void loadServiceData(NavigatorAction navigatorAction, Project project, String selectTitleSlug) {
        QuestionManager.getQuestionAllService(project, false);
        PageInfo pageInfo = QuestionManager.getQuestionViewList(project, navigatorAction.getPageInfo());
        if ((pageInfo.getRows() == null || pageInfo.getRows().isEmpty()) && pageInfo.getRowTotal() != 0) {
            MessageUtils.getInstance(project).showErrorMsg("error", PropertiesUtils.getInfo("response.question"));
            return;
        }

        if (navigatorAction.getFind().getFilter().isEmpty()) {
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                navigatorAction.getFind().addFilter(Constant.FIND_TYPE_CATEGORY, FindManager.getCategory());
                navigatorAction.getFind().addFilter(Constant.FIND_TYPE_DIFFICULTY, FindManager.getDifficulty());
                navigatorAction.getFind().addFilter(Constant.FIND_TYPE_STATUS, FindManager.getStatus());
                navigatorAction.getFind().addFilter(Constant.FIND_TYPE_TAGS, FindManager.getTags());
                navigatorAction.getFind().addFilter(Constant.FIND_TYPE_LISTS, FindManager.getLists(project));
            });
        }

        navigatorAction.loadData(selectTitleSlug);
    }

    public static void pick(Project project, PageInfo pageInfo) {
        Question question = QuestionManager.pick(project, pageInfo);
        if (question != null) {
            CodeManager.openCode(question.getTitleSlug(), project);
        }
    }

    public static void loadAllServiceData(NavigatorAction navigatorAction, Project project) {
        loadAllServiceData(navigatorAction, project, null, false);
    }

    public static void loadAllServiceData(NavigatorAction navigatorAction, Project project, String selectTitleSlug, boolean reset) {
        List<QuestionView> questionViews = QuestionManager.getQuestionAllService(project, reset);
        if (CollectionUtils.isEmpty(questionViews)) {
            MessageUtils.getInstance(project).showErrorMsg("error", PropertiesUtils.getInfo("response.question"));
            return;
        }

        if (navigatorAction.getFind().getFilter().isEmpty()) {
            navigatorAction.getFind().addFilter(Constant.FIND_TYPE_CATEGORY, FindManager.getCategory());
            navigatorAction.getFind().addFilter(Constant.FIND_TYPE_DIFFICULTY, FindManager.getDifficulty());
            navigatorAction.getFind().addFilter(Constant.FIND_TYPE_STATUS, FindManager.getStatus());
            navigatorAction.getFind().addFilter(Constant.FIND_TYPE_TAGS, FindManager.getTags());
            navigatorAction.getFind().addFilter(Constant.FIND_TYPE_LISTS, FindManager.getLists(project));
        }

        Set<String> conformSet = questionViews.stream().map(QuestionView::getQuestionId).collect(Collectors.toSet());
        PageInfo<QuestionView> pageInfo = navigatorAction.getPageInfo();
        PageInfo.Filters filters = pageInfo.getFilters();
        if (StringUtils.isNotBlank(filters.getListId())) {
            List<Tag> tagList = navigatorAction.getFind().getFilter(Constant.FIND_TYPE_LISTS);
            Tag tag = tagList.stream().filter(t -> t.getSlug().equalsIgnoreCase(filters.getListId())).findAny().get();
            conformSet.retainAll(tag.getQuestions());
        }
        if (CollectionUtils.isNotEmpty(filters.getTags())) {
            List<Tag> tagList = navigatorAction.getFind().getFilter(Constant.FIND_TYPE_TAGS);
            Set<String> tagQuestions = new HashSet<>();
            Set<String> tagSlugs = filters.getTags().stream().collect(Collectors.toSet());
            for (Tag tag : tagList) {
                if (tagSlugs.contains(tag.getSlug())) {
                    tagQuestions.addAll(tag.getQuestions());
                }
            }
            conformSet.retainAll(tagQuestions);
        }

        boolean category = StringUtils.isNotBlank(pageInfo.getCategorySlug());
        boolean searchKeywords = StringUtils.isNotBlank(filters.getSearchKeywords());
        boolean difficulty = StringUtils.isNotBlank(filters.getDifficulty());
        boolean status = StringUtils.isNotBlank(filters.getStatus());

        List<QuestionView> conformList = new ArrayList<>();
        QuestionView dayQuestion = QuestionManager.questionOfToday();
        if (dayQuestion != null) {
            conformList.add(dayQuestion);
        }
        for (QuestionView questionView : questionViews) {
            if (!conformSet.contains(questionView.getQuestionId())) {
                continue;
            }
            if (category && !questionView.getCategory().equalsIgnoreCase(pageInfo.getCategorySlug())) {
                continue;
            }
            if (searchKeywords && !(questionView.getFrontendQuestionId().startsWith(filters.getSearchKeywords())
                    || questionView.getTitle().contains(filters.getSearchKeywords()) || questionView.getTitleSlug().contains(filters.getSearchKeywords()))) {
                continue;
            }
            if (difficulty) {
                List<String> difficultyList = FindManager.getDifficulty().stream().map(t -> t.getSlug()).collect(Collectors.toList());
                Integer level = difficultyList.indexOf(filters.getDifficulty()) + 1;
                if (!questionView.getLevel().equals(level)) {
                    continue;
                }
            }
            if (status) {
                if ("TRIED".equalsIgnoreCase(filters.getStatus()) && !questionView.getStatusSign().equalsIgnoreCase("?")) {
                    continue;
                } else if ("AC".equalsIgnoreCase(filters.getStatus()) && !questionView.getStatusSign().equalsIgnoreCase("✔")) {
                    continue;
                } else if ("NOT_STARTED".equalsIgnoreCase(filters.getStatus()) && !(questionView.getStatusSign().equalsIgnoreCase("$") || StringUtils.isBlank(questionView.getStatusSign()))) {
                    continue;
                }
            }
            conformList.add(questionView);
        }

        if (StringUtils.isNotBlank(filters.getOrderBy())) {
            int order = "DESCENDING".equalsIgnoreCase(filters.getSortOrder()) ? -1 : 1;
            Collections.sort(conformList, new Comparator<QuestionView>() {
                @Override
                public int compare(QuestionView o1, QuestionView o2) {
                    if ("day".equalsIgnoreCase(o1.getStatus())) {
                        return 1;
                    } else if ("day".equalsIgnoreCase(o2.getStatus())) {
                        return -1;
                    }
                    if ("TITLE".equalsIgnoreCase(filters.getOrderBy())) {
                        return order * o1.getTitle().compareTo(o2.getTitle());
                    }
                    if ("DIFFICULTY".equalsIgnoreCase(filters.getOrderBy())) {
                        return order * o1.getLevel().compareTo(o2.getLevel());
                    }
                    if ("STATES".equalsIgnoreCase(filters.getOrderBy())) {
                        return order * o1.getStatusSign().compareTo(o2.getStatusSign());
                    }
                    return order * o1.getFrontendQuestionId().compareTo(o2.getFrontendQuestionId());
                }
            });
        }

        navigatorAction.getPageInfo().setRows(conformList);
        navigatorAction.getPageInfo().setRowTotal(conformList.size());
        navigatorAction.loadData(selectTitleSlug);
    }
}
