package com.shuzijun.leetcode.plugin.manager;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.model.*;
import com.shuzijun.leetcode.plugin.utils.LogUtils;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
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
        long requestVersion = NavigatorRequestTracker.begin(navigatorAction);
        if (ApplicationManager.getApplication().isDispatchThread()) {
            ApplicationManager.getApplication().executeOnPooledThread(
                    () -> loadServiceData(navigatorAction, project, selectTitleSlug, requestVersion));
            return;
        }
        loadServiceData(navigatorAction, project, selectTitleSlug, requestVersion);
    }

    private static void loadServiceData(NavigatorAction navigatorAction, Project project, String selectTitleSlug,
                                        long requestVersion) {
        if (project.isDisposed() || !NavigatorRequestTracker.isLatest(navigatorAction, requestVersion)) {
            return;
        }
        logPageState("loadServiceData:start", navigatorAction.getPageInfo(), selectTitleSlug);
        PageInfo pageInfo = QuestionManager.getQuestionViewList(project, copyPageInfo(navigatorAction.getPageInfo()));
        if (project.isDisposed() || !NavigatorRequestTracker.isLatest(navigatorAction, requestVersion)) {
            return;
        }
        logPageState("loadServiceData:question-list-loaded", pageInfo, selectTitleSlug);
        if ((pageInfo.getRows() == null || pageInfo.getRows().isEmpty()) && pageInfo.getRowTotal() != 0) {
            LogUtils.navigatorTrace("loadServiceData:unexpected-empty-page rowTotal=" + pageInfo.getRowTotal());
            MessageUtils.getInstance(project).showErrorMsg("error", PropertiesUtils.getInfo("response.question"));
            return;
        }

        if (navigatorAction.getFind().getFilter().isEmpty()) {
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                List<Tag> categories = FindManager.getCategory();
                List<Tag> difficulties = FindManager.getDifficulty();
                List<Tag> statuses = FindManager.getStatus();
                List<Tag> tags = FindManager.getTags();
                List<Tag> lists = FindManager.getLists(project);
                if (project.isDisposed() || !NavigatorRequestTracker.isLatest(navigatorAction, requestVersion)
                        || !navigatorAction.getFind().getFilter().isEmpty()) {
                    return;
                }
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (project.isDisposed() || !NavigatorRequestTracker.isLatest(navigatorAction, requestVersion)
                            || !navigatorAction.getFind().getFilter().isEmpty()) {
                        return;
                    }
                    navigatorAction.getFind().addFilter(Constant.FIND_TYPE_CATEGORY, categories);
                    navigatorAction.getFind().addFilter(Constant.FIND_TYPE_DIFFICULTY, difficulties);
                    navigatorAction.getFind().addFilter(Constant.FIND_TYPE_STATUS, statuses);
                    navigatorAction.getFind().addFilter(Constant.FIND_TYPE_TAGS, tags);
                    navigatorAction.getFind().addFilter(Constant.FIND_TYPE_LISTS, lists);
                }, ignored -> project.isDisposed());
            });
        }

        publishPageInfo(navigatorAction, project, selectTitleSlug, requestVersion, pageInfo);
        logPageState("loadServiceData:refresh-queued", pageInfo, selectTitleSlug);
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
        long requestVersion = NavigatorRequestTracker.begin(navigatorAction);
        PageInfo<QuestionView> pageInfo = copyPageInfo(navigatorAction.getPageInfo());
        Map<String, List<Tag>> filterData = copyFilterData(navigatorAction.getFind());
        if (ApplicationManager.getApplication().isDispatchThread()) {
            ApplicationManager.getApplication().executeOnPooledThread(
                    () -> loadAllServiceData(navigatorAction, project, selectTitleSlug, reset, requestVersion,
                            pageInfo, filterData));
            return;
        }
        loadAllServiceData(navigatorAction, project, selectTitleSlug, reset, requestVersion, pageInfo, filterData);
    }

    private static void loadAllServiceData(NavigatorAction navigatorAction, Project project, String selectTitleSlug,
                                           boolean reset, long requestVersion, PageInfo<QuestionView> pageInfo,
                                           Map<String, List<Tag>> filterData) {
        if (project.isDisposed() || !NavigatorRequestTracker.isLatest(navigatorAction, requestVersion)) {
            return;
        }
        List<QuestionView> questionViews = QuestionManager.getQuestionAllService(project, reset);
        if (project.isDisposed() || !NavigatorRequestTracker.isLatest(navigatorAction, requestVersion)) {
            return;
        }
        if (questionViews == null || questionViews.isEmpty()) {
            MessageUtils.getInstance(project).showErrorMsg("error", PropertiesUtils.getInfo("response.question"));
            return;
        }

        if (filterData.isEmpty()) {
            filterData.put(Constant.FIND_TYPE_CATEGORY.toLowerCase(), FindManager.getCategory());
            filterData.put(Constant.FIND_TYPE_DIFFICULTY.toLowerCase(), FindManager.getDifficulty());
            filterData.put(Constant.FIND_TYPE_STATUS.toLowerCase(), FindManager.getStatus());
            filterData.put(Constant.FIND_TYPE_TAGS.toLowerCase(), FindManager.getTags());
            filterData.put(Constant.FIND_TYPE_LISTS.toLowerCase(), FindManager.getLists(project));
        }

        Set<String> conformSet = questionViews.stream().map(QuestionView::getQuestionId).collect(Collectors.toSet());
        PageInfo.Filters filters = pageInfo.getFilters();
        if (StringUtils.isNotBlank(filters.getListId())) {
            List<Tag> tagList = filterData.get(Constant.FIND_TYPE_LISTS.toLowerCase());
            Optional<Tag> optional = tagList == null ? Optional.empty()
                    : tagList.stream().filter(t -> t.getSlug().equalsIgnoreCase(filters.getListId())).findAny();
            if (optional.isPresent()){
                Tag tag = optional.get();
                conformSet.retainAll(tag.getQuestions());
            }
        }
        if (filters.getTags() != null && !filters.getTags().isEmpty()) {
            List<Tag> tagList = filterData.get(Constant.FIND_TYPE_TAGS.toLowerCase());
            Set<String> tagQuestions = new HashSet<>();
            Set<String> tagSlugs = filters.getTags().stream().collect(Collectors.toSet());
            if (tagList != null) {
                for (Tag tag : tagList) {
                    if (tagSlugs.contains(tag.getSlug())) {
                        tagQuestions.addAll(tag.getQuestions());
                    }
                }
            }
            conformSet.retainAll(tagQuestions);
        }

        boolean category = StringUtils.isNotBlank(pageInfo.getCategorySlug());
        boolean searchKeywords = StringUtils.isNotBlank(filters.getSearchKeywords());
        boolean difficulty = StringUtils.isNotBlank(filters.getDifficulty());
        boolean status = StringUtils.isNotBlank(filters.getStatus());
        int difficultyLevel = difficulty ? difficultyLevel(filters.getDifficulty()) : 0;

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
                if (!questionView.getLevel().equals(difficultyLevel)) {
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

        pageInfo.setRows(conformList);
        pageInfo.setRowTotal(conformList.size());
        publishAllPageInfo(navigatorAction, project, selectTitleSlug, requestVersion, pageInfo, filterData);
    }

    private static int difficultyLevel(String difficulty) {
        if (Constant.DIFFICULTY_EASY.equalsIgnoreCase(difficulty)) {
            return 1;
        }
        if (Constant.DIFFICULTY_MEDIUM.equalsIgnoreCase(difficulty)) {
            return 2;
        }
        return Constant.DIFFICULTY_HARD.equalsIgnoreCase(difficulty) ? 3 : 0;
    }

    private static <T> void publishPageInfo(NavigatorAction<T> navigatorAction, Project project,
                                            String selectTitleSlug, long requestVersion, PageInfo<T> pageInfo) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed() || !NavigatorRequestTracker.isLatest(navigatorAction, requestVersion)) {
                return;
            }
            applyPageInfo(navigatorAction.getPageInfo(), pageInfo);
            navigatorAction.loadData(selectTitleSlug);
        }, ignored -> project.isDisposed());
    }

    private static void publishAllPageInfo(NavigatorAction<QuestionView> navigatorAction, Project project,
                                           String selectTitleSlug, long requestVersion, PageInfo<QuestionView> pageInfo,
                                           Map<String, List<Tag>> filterData) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed() || !NavigatorRequestTracker.isLatest(navigatorAction, requestVersion)) {
                return;
            }
            if (navigatorAction.getFind().getFilter().isEmpty()) {
                filterData.forEach(navigatorAction.getFind()::addFilter);
            }
            applyPageInfo(navigatorAction.getPageInfo(), pageInfo);
            navigatorAction.loadData(selectTitleSlug);
        }, ignored -> project.isDisposed());
    }

    private static Map<String, List<Tag>> copyFilterData(Find find) {
        Map<String, List<Tag>> copy = new HashMap<>();
        find.getFilter().forEach((key, tags) -> copy.put(key, tags == null ? Collections.emptyList() : new ArrayList<>(tags)));
        return copy;
    }

    private static <T> PageInfo<T> copyPageInfo(PageInfo<T> pageInfo) {
        PageInfo<T> copy = new PageInfo<>(pageInfo.getPageIndex(), pageInfo.getPageSize());
        copy.setRowTotal(pageInfo.getRowTotal());
        copy.setCategorySlug(pageInfo.getCategorySlug());
        PageInfo.Filters sourceFilters = pageInfo.getFilters();
        PageInfo.Filters targetFilters = copy.getFilters();
        targetFilters.setSearchKeywords(sourceFilters.getSearchKeywords());
        targetFilters.setOrderBy(sourceFilters.getOrderBy());
        targetFilters.setSortOrder(sourceFilters.getSortOrder());
        targetFilters.setDifficulty(sourceFilters.getDifficulty());
        targetFilters.setStatus(sourceFilters.getStatus());
        targetFilters.setListId(sourceFilters.getListId());
        targetFilters.setTags(sourceFilters.getTags() == null ? null : new ArrayList<>(sourceFilters.getTags()));
        return copy;
    }

    private static <T> void applyPageInfo(PageInfo<T> target, PageInfo<T> source) {
        target.setRowTotal(source.getRowTotal());
        target.setRows(source.getRows());
    }

    private static void logPageState(String event, PageInfo<?> pageInfo, String selectTitleSlug) {
        int rowCount = pageInfo.getRows() == null ? 0 : pageInfo.getRows().size();
        LogUtils.navigatorTrace(event
                + " page=" + pageInfo.getPageIndex()
                + " skip=" + pageInfo.getSkip()
                + " pageSize=" + pageInfo.getPageSize()
                + " rowTotal=" + pageInfo.getRowTotal()
                + " rows=" + rowCount
                + " category=" + pageInfo.getCategorySlug()
                + " filters=" + pageInfo.getFilters()
                + " selectSlug=" + selectTitleSlug);
    }
}
