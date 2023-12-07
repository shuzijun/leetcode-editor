package com.shuzijun.leetcode.plugin.manager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.model.*;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;
import com.shuzijun.leetcode.plugin.utils.doc.CleanMarkdown;
import com.shuzijun.leetcode.plugin.window.WindowFactory;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author shuzijun
 */
public class QuestionManager {

    private static final Cache<String, QuestionView> dayMap = CacheBuilder.newBuilder().maximumSize(5).expireAfterWrite(2, TimeUnit.DAYS).build();
    private static final Cache<String, Question> questionCache = CacheBuilder.newBuilder().maximumSize(30).build();
    private static final Cache<String, List<QuestionView>> questionAllCache = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.DAYS).build();
    private static final Map<String, Map<String, Integer>> questionIndexCache = Maps.newLinkedHashMap();


    public static PageInfo<QuestionView> getQuestionViewList(Project project, PageInfo<QuestionView> pageInfo) {
        boolean isPremium = false;
        User user = WindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_PROJECTS_TABS).getUser();
        if (user != null) {
            isPremium = user.isPremium();
        }

        HttpResponse response = Graphql.builder().cn(URLUtils.isCn()).operationName("problemsetQuestionList")
                .variables("categorySlug", pageInfo.getCategorySlug()).variables("skip", pageInfo.getSkip())
                .variables("limit", pageInfo.getPageSize()).variables("filters", pageInfo.getFilters())
                .cacheParam(WindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_PROJECTS_TABS).getUser().getUsername()).request();
        if (response.getStatusCode() == 200) {
            List<QuestionView> questionList = parseQuestion(response.getBody(), isPremium);

            QuestionView dayQuestion = questionOfToday();
            if (dayQuestion != null) {
                questionList.add(0, dayQuestion);
            }

            Integer total = JSONObject.parseObject(response.getBody()).getJSONObject("data").getJSONObject("problemsetQuestionList").getInteger("total");
            pageInfo.setRowTotal(total);
            pageInfo.setRows(questionList);
        } else {
            LogUtils.LOG.error("Request question list failed, status:" + response.getStatusCode());
            throw new RuntimeException("Request question list failed");
        }

        return pageInfo;

    }

    public static List<QuestionView> getQuestionAllService(Project project, boolean reset) {
        Boolean isPremium = false;
        User user = WindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_PROJECTS_TABS).getUser();
        if (user != null) {
            isPremium = user.isPremium();
        }
        if (questionAllCache.getIfPresent(URLUtils.getLeetcodeHost()) == null || reset) {
            String key = URLUtils.getLeetcodeHost() + "getQuestionAll";
            synchronized (key.intern()) {
                if (questionAllCache.getIfPresent(URLUtils.getLeetcodeHost()) == null || reset) {
                    HttpResponse response = Graphql.builder().cn(URLUtils.isCn()).operationName("allQuestions")
                            .cacheParam(WindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_PROJECTS_TABS).getUser().getUsername()).request();
                    if (response.getStatusCode() == 200) {
                        List<QuestionView> questionViews = new ArrayList<>();

                        JSONArray allQuestions = JSONObject.parseObject(response.getBody()).getJSONObject("data").getJSONArray("allQuestions");
                        for (int i = 0; i < allQuestions.size(); i++) {
                            JSONObject jsonObject = allQuestions.getJSONObject(i);
                            QuestionView questionView = jsonObject.toJavaObject(QuestionView.class);
                            if (jsonObject.getBoolean("isPaidOnly") && !isPremium) {
                                questionView.setStatus("lock");
                            }
                            if (URLUtils.isCn() && !PersistentConfig.getInstance().getConfig().getEnglishContent()) {
                                if (StringUtils.isNotBlank(jsonObject.getString("translatedTitle"))) {
                                    questionView.setTitle(jsonObject.getString("translatedTitle"));
                                }
                            }
                            questionViews.add(questionView);

                        }

                        Collections.sort(questionViews, (o1, o2) -> o1.frontendQuestionIdCompareTo(o2));

                        Map<String, Integer> questionIndex = Maps.newHashMap();
                        for (int i = 0; i < questionViews.size(); i++) {
                            questionIndex.put(questionViews.get(i).getTitleSlug(), i);
                        }

                        questionAllCache.put(URLUtils.getLeetcodeHost(), questionViews);
                        questionIndexCache.put(URLUtils.getLeetcodeHost(), questionIndex);
                    } else {
                        questionAllCache.invalidate(URLUtils.getLeetcodeHost());
                        questionIndexCache.remove(URLUtils.getLeetcodeHost());
                    }
                }
            }
        }
        return questionAllCache.getIfPresent(URLUtils.getLeetcodeHost());
    }

    public static QuestionIndex getQuestionIndex(String titleSlug) {
        if (questionAllCache.getIfPresent(URLUtils.getLeetcodeHost()) == null) {
            return null;
        } else if (!questionIndexCache.get(URLUtils.getLeetcodeHost()).containsKey(titleSlug)) {
            return null;
        } else {
            QuestionIndex questionIndex = new QuestionIndex();
            questionIndex.setIndex(questionIndexCache.get(URLUtils.getLeetcodeHost()).get(titleSlug));
            questionIndex.setQuestionView(questionAllCache.getIfPresent(URLUtils.getLeetcodeHost()).get(questionIndex.getIndex()));
            return questionIndex;
        }
    }

    private static List<QuestionView> parseQuestion(String str, Boolean isPremium) {

        List<QuestionView> questionList = new ArrayList<>();

        if (StringUtils.isNotBlank(str)) {
            JSONObject jsonObject = JSONObject.parseObject(str).getJSONObject("data").getJSONObject("problemsetQuestionList");
            JSONArray jsonArray = jsonObject.getJSONArray("questions");
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                QuestionView question = parseQuestionView(object, isPremium);
                questionList.add(question);
            }
        }
        return questionList;
    }

    private static QuestionView parseQuestionView(JSONObject object, Boolean isPremium) {
        QuestionView question = new QuestionView(object.getString("title"));
        if (URLUtils.isCn() && !PersistentConfig.getInstance().getConfig().getEnglishContent()) {
            if (StringUtils.isNotBlank(object.getString("titleCn"))) {
                question.setTitle(object.getString("titleCn"));
            }
        }
        question.setFrontendQuestionId(object.getString("frontendQuestionId"));
        question.setAcceptance(object.getDouble("acRate"));
        try {
            if (object.getBoolean("paidOnly") && !isPremium) {
                question.setStatus("lock");
            } else {
                question.setStatus(object.get("status") == null ? "" : object.getString("status").toLowerCase());
            }
            if (object.containsKey("freqBar") && object.get("freqBar") != null) {
                question.setFrequency(object.getDouble("freqBar"));
            }
        } catch (Exception ee) {
            question.setStatus("");
        }
        question.setTitleSlug(object.getString("titleSlug"));
        question.setLevel(object.getString("difficulty"));
        return question;
    }

    public static QuestionView questionOfToday() {
        QuestionView dayQuestion = dayMap.getIfPresent(URLUtils.getLeetcodeHost() + new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        if (dayQuestion == null) {
            try {
                HttpResponse response = Graphql.builder().cn(URLUtils.isCn()).operationName("questionOfToday").request();
                if (response.getStatusCode() != 200) {
                    return null;
                } else {
                    JSONObject dateObject = JSONObject.parseObject(response.getBody()).getJSONObject("data");
                    JSONObject todayRecordObject;
                    if (URLUtils.isCn()) {
                        todayRecordObject = dateObject.getJSONArray("activeDailyCodingChallengeQuestion").getJSONObject(0);
                    } else {
                        todayRecordObject = dateObject.getJSONObject("activeDailyCodingChallengeQuestion");
                    }
                    dayQuestion = parseQuestionView(todayRecordObject.getJSONObject("question"), true);
                    dayQuestion.setStatus("day");
                    dayMap.put(URLUtils.getLeetcodeHost() + todayRecordObject.getString("date"), dayQuestion);
                }
            } catch (Exception ignore) {

            }
        }

        return dayQuestion;
    }


    private static boolean getQuestion(Question question, Project project) {
        try {
            HttpResponse response = Graphql.builder().operationName("questionData").variables("titleSlug", question.getTitleSlug()).request();
            if (response.getStatusCode() == 200) {

                String body = response.getBody();

                JSONObject jsonObject = JSONObject.parseObject(body).getJSONObject("data").getJSONObject("question");

                question.setQuestionId(jsonObject.getString("questionId"));
                question.setContent(getContent(jsonObject));
                question.setTestCase(jsonObject.getString("sampleTestCase"));
                question.setExampleTestcases(jsonObject.getString("exampleTestcases"));
                question.setStatus(jsonObject.get("status") == null ? "" : jsonObject.getString("status"));
                question.setTitle(jsonObject.getString("title"));
                if (URLUtils.isCn() && !PersistentConfig.getInstance().getConfig().getEnglishContent()) {
                    if (StringUtils.isNotBlank(jsonObject.getString("translatedTitle"))) {
                        question.setTitle(jsonObject.getString("translatedTitle"));
                    }
                }
                question.setFrontendQuestionId(jsonObject.getString("questionFrontendId"));
                question.setLevel(jsonObject.getString("difficulty"));
                if (URLUtils.isCn()) {
                    question.setArticleLive(Constant.ARTICLE_LIVE_LIST);
                } else if (jsonObject.get("solution") != null) {
                    question.setArticleLive(Constant.ARTICLE_LIVE_ONE);
                    question.setArticleSlug(question.getTitleSlug());
                } else {
                    question.setArticleLive(Constant.ARTICLE_LIVE_NONE);
                }

                JSONArray jsonArray = jsonObject.getJSONArray("codeSnippets");
                if (jsonArray != null) {
                    List<CodeSnippet> codeSnippets = new ArrayList<>();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        CodeSnippet codeSnippet = new CodeSnippet();
                        codeSnippet.setCode(object.getString("code").replaceAll("\\n", "\n"));
                        codeSnippet.setLang(object.getString("lang"));
                        codeSnippet.setLangSlug(object.getString("langSlug"));
                        codeSnippets.add(codeSnippet);
                    }
                    question.setCodeSnippets(codeSnippets);
                }
                return Boolean.TRUE;
            } else {
                MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("response.code"));
            }

        } catch (Exception e) {
            LogUtils.LOG.error("获取代码失败", e);
            MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("response.code"));
        }
        return Boolean.FALSE;
    }

    private static String getContent(JSONObject jsonObject) {
        StringBuilder sb = new StringBuilder();
        sb.append(CleanMarkdown.cleanMarkdown(jsonObject.getString(URLUtils.getDescContent()), ""));
        Config config = PersistentConfig.getInstance().getConfig();
        if (config.getShowTopics()) {
            JSONArray topicTagsArray = jsonObject.getJSONArray("topicTags");
            if (topicTagsArray != null && !topicTagsArray.isEmpty()) {
                sb.append("<div><div>Related Topics</div><div>");
                for (int i = 0; i < topicTagsArray.size(); i++) {
                    JSONObject tag = topicTagsArray.getJSONObject(i);
                    sb.append("<li>");
                    if (StringUtils.isBlank(tag.getString("translatedName"))) {
                        sb.append(tag.getString("name"));
                    } else {
                        sb.append(tag.getString("translatedName"));
                    }
                    sb.append("</li>");
                }
                sb.append("</div></div>");
                sb.append("<br>");
            }
        }
        sb.append("<div><li>\uD83D\uDC4D ").append(jsonObject.getInteger("likes")).append("</li><li>\uD83D\uDC4E ").append(jsonObject.getInteger("dislikes")).append("</li></div>");
        return sb.toString();
    }

    public static Question pick(Project project, PageInfo<?> pageInfo) {
        String titleSlug = null;
        HttpResponse response = Graphql.builder().cn(URLUtils.isCn()).operationName("randomQuestion").variables("categorySlug", pageInfo.getCategorySlug()).variables("filters", pageInfo.getFilters()).request();
        if (response.getStatusCode() == 200) {
            String body = response.getBody();
            if (URLUtils.isCn()) {
                titleSlug = JSONObject.parseObject(body).getJSONObject("data").getString("randomQuestion");
            } else {
                titleSlug = JSONObject.parseObject(body).getJSONObject("data").getJSONObject("randomQuestion").getString("titleSlug");
            }
        }
        if (StringUtils.isNotBlank(titleSlug)) {
            return getQuestionByTitleSlug(titleSlug, project);
        } else {
            return null;
        }

    }

    public static User getUser() {
        HttpResponse response = Graphql.builder().cn(URLUtils.isCn()).operationName("userStatus","globalData").request();
        if (response.getStatusCode() == 200) {
            JSONObject userObject = JSONObject.parseObject(response.getBody()).getJSONObject("data").getJSONObject("userStatus");
            User user = new User();
            user.setPremium(userObject.getBoolean("isPremium"));
            user.setUsername(userObject.getString("username"));
            if (userObject.containsValue("userSlug")){
                user.setUserSlug(userObject.getString("userSlug"));
            }else {
                user.setUserSlug(user.getUsername());
            }
            user.setSignedIn(userObject.getBoolean("isSignedIn"));
            user.setVerified(userObject.getBoolean("isVerified"));
            user.setPhoneVerified(userObject.getBoolean("isPhoneVerified"));
            return user;
        } else {
            LogUtils.LOG.error("Request userStatus  failed, status:" + response.getStatusCode());
            return new User();
        }
    }

    public static Question getQuestionByTitleSlug(String titleSlug, Project project) {
        return getQuestionByTitleSlug(titleSlug,project, false);
    }

    public static Question getQuestionByTitleSlug(String titleSlug, Project project, boolean readOnlyCache) {

        if (StringUtils.isBlank(titleSlug)) {
            return null;
        }
        String key = URLUtils.getLeetcodeHost() + titleSlug;
        if (questionCache.getIfPresent(key) == null) {
            synchronized (key.intern()) {
                if (questionCache.getIfPresent(key) == null) {
                    try {
                        Question question = new Question();
                        question.setTitleSlug(titleSlug);
                        Future<Boolean> questionFuture = ApplicationManager.getApplication().executeOnPooledThread(() -> {
                            return getQuestion(question, project);
                        });
                        if (readOnlyCache) {
                            return null;
                        }
                        if (questionFuture.get()) {
                            questionCache.put(key, question);
                        } else {
                            return null;
                        }
                    } catch (Exception e) {
                        return null;
                    }
                }
            }
        }
        return questionCache.getIfPresent(key);
    }
}
