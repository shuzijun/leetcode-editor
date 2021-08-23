package com.shuzijun.leetcode.plugin.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.model.Sort;
import com.shuzijun.leetcode.plugin.model.Tag;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;
import com.shuzijun.leetcode.plugin.window.WindowFactory;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * @author shuzijun
 */
public class QuestionManager {


    private static List<Question> QUESTIONLIST = null;

    private final static String ALLNAME = "all.json";

    private final static String TRANSLATIONNAME = "translation.json";

    private static String dayQuestion = null;

    public static List<Question> getQuestionService(Project project, String categorySlug) {
        Boolean isPremium = false;
        if (HttpRequestUtils.isLogin()) {
            HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeGraphql(), "application/json");
            httpRequest.setBody("{\"query\":\"\\n    query globalData {\\n  userStatus {\\n    isSignedIn\\n    isPremium\\n    username\\n    realName\\n    avatar\\n    userSlug\\n    isAdmin\\n    useTranslation\\n    premiumExpiredAt\\n    isTranslator\\n    isSuperuser\\n    isPhoneVerified\\n  }\\n  jobsMyCompany {\\n    nameSlug\\n  }\\n  commonNojPermissionTypes\\n}\\n    \",\"variables\":{},\"operationName\":\"globalData\"}");
            httpRequest.addHeader("Accept", "application/json");
            HttpResponse response = HttpRequestUtils.executePost(httpRequest);
            if (response != null && response.getStatusCode() == 200) {
                JSONObject user = JSONObject.parseObject(response.getBody()).getJSONObject("data").getJSONObject("userStatus");
                isPremium =  user.getBoolean("isPremium");
                ApplicationManager.getApplication().invokeAndWait(() -> {
                    WindowFactory.updateTitle(project, user.getString("username"));
                });
            } else {
                LogUtils.LOG.error("Request userStatus  failed, status:" + response == null ? "" : response.getStatusCode());
            }
        }
        List<Question> questionList = new ArrayList<>();
        int skip = 0;
        int limit = 100;
        int total = -1;
        while(true) {
            try {
                Map<String, Object> page = getQuestionPage(categorySlug, skip, limit, isPremium);
                if (total < 0) {
                    total = (int) page.get("total");
                }
                questionList.addAll((List) page.get("questionList"));
                skip = skip + limit;
                if(total <= skip){
                    break;
                }
            } catch (Exception e) {
                return null;
            }

        }
        questionOfToday();
        sortQuestionList(questionList, new Sort(Constant.SORT_TYPE_ID, 1));
        if (questionList != null && !questionList.isEmpty()) {
            String filePath = PersistentConfig.getInstance().getTempFilePath() + Constant.DOC_PATH + ALLNAME;
            FileUtils.saveFile(filePath, JSON.toJSONString(questionList));
            QUESTIONLIST = questionList;
        }
        return questionList;

    }

    private static Map<String, Object> getQuestionPage(String categorySlug, int skip, int limit, Boolean isPremium) throws Exception {
        HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeGraphql(), "application/json");
        if (URLUtils.isCn()) {
            httpRequest.setBody("{\"query\":\"\\n    query problemsetQuestionList($categorySlug: String, $limit: Int, $skip: Int, $filters: QuestionListFilterInput) {\\n  problemsetQuestionList(\\n    categorySlug: $categorySlug\\n    limit: $limit\\n    skip: $skip\\n    filters: $filters\\n  ) {\\n    hasMore\\n    total\\n    questions {\\n      acRate\\n      difficulty\\n      freqBar\\n      frontendQuestionId\\n      isFavor\\n      paidOnly\\n      solutionNum\\n      status\\n      title\\n      titleCn\\n      titleSlug\\n      topicTags {\\n        name\\n        nameTranslated\\n        id\\n        slug\\n      }\\n      extra {\\n        hasVideoSolution\\n        topCompanyTags {\\n          imgUrl\\n          slug\\n          numSubscribed\\n        }\\n      }\\n    }\\n  }\\n}\\n    \",\"variables\":{\"categorySlug\":\""+categorySlug+"\",\"skip\":" + skip + ",\"limit\":" + limit + ",\"filters\":{}},\"operationName\":\"problemsetQuestionList\"}");
        } else {
            httpRequest.setBody("{\"query\":\"\\n    query problemsetQuestionList($categorySlug: String, $limit: Int, $skip: Int, $filters: QuestionListFilterInput) {\\n  problemsetQuestionList: questionList(\\n    categorySlug: $categorySlug\\n    limit: $limit\\n    skip: $skip\\n    filters: $filters\\n  ) {\\n    total: totalNum\\n    questions: data {\\n      acRate\\n      difficulty\\n      freqBar\\n      frontendQuestionId: questionFrontendId\\n      isFavor\\n      paidOnly: isPaidOnly\\n      status\\n      title\\n      titleSlug\\n      topicTags {\\n        name\\n        id\\n        slug\\n      }\\n      hasSolution\\n      hasVideoSolution\\n    }\\n  }\\n}\\n    \",\"variables\":{\"categorySlug\":\"" + categorySlug + "\",\"skip\":" + skip + ",\"limit\":" + limit + ",\"filters\":{}},\"operationName\":\"problemsetQuestionList\"}");
        }
        httpRequest.addHeader("Accept", "application/json");
        HttpResponse response = HttpRequestUtils.executePost(httpRequest);
        if (response != null && response.getStatusCode() == 200) {
            List questionList = parseQuestion(response.getBody(), isPremium);
            Integer total = JSONObject.parseObject(response.getBody()).getJSONObject("data").getJSONObject("problemsetQuestionList").getInteger("total");
            Map<String, Object> page = new HashMap<>();
            page.put("total", total);
            page.put("questionList", questionList);
            return page;
        } else {
            LogUtils.LOG.error("Request question list failed, status:" + response == null ? "" : response.getStatusCode());
            throw new RuntimeException("Request question list failed");
        }
    }

    public static List<Question> getQuestionCache() {
        if (QUESTIONLIST != null) {
            return QUESTIONLIST;
        }

        String filePath = PersistentConfig.getInstance().getTempFilePath() + Constant.DOC_PATH + ALLNAME;
        String body = FileUtils.getFileBody(filePath);

        if (StringUtils.isBlank(body)) {
            return Lists.newArrayList();
        } else {
            List<Question> questionList = JSON.parseArray(body, Question.class);
            QUESTIONLIST = questionList;
            return questionList;
        }
    }

    public static List<Tag> getDifficulty() {

        List<String> keyList = Lists.newArrayList(Constant.DIFFICULTY_EASY, Constant.DIFFICULTY_MEDIUM, Constant.DIFFICULTY_HARD);

        List<Question> questionList = getQuestionCache();
        ImmutableListMultimap<String, Question> questionImmutableMap = Multimaps.index(questionList.iterator(), new Function<Question, String>() {
            @Override
            public String apply(Question question) {
                String difficulty;
                if (question.getLevel() == 1) {
                    difficulty = Constant.DIFFICULTY_EASY;
                } else if (question.getLevel() == 2) {
                    difficulty = Constant.DIFFICULTY_MEDIUM;
                } else if (question.getLevel() == 3) {
                    difficulty = Constant.DIFFICULTY_HARD;
                } else {
                    difficulty = Constant.DIFFICULTY_UNKNOWN;
                }
                return difficulty;
            }
        });

        List<Tag> difficultyList = Lists.newArrayList();
        for (String key : keyList) {
            Tag tag = new Tag();
            tag.setName(key);
            for (Question question : questionImmutableMap.get(key)) {
                tag.addQuestion(question.getQuestionId());
            }
            difficultyList.add(tag);
        }
        return difficultyList;
    }

    public static List<Tag> getStatus() {
        List<String> keyList = Lists.newArrayList(Constant.STATUS_TODO, Constant.STATUS_SOLVED, Constant.STATUS_ATTEMPTED);

        List<Question> questionList = getQuestionCache();
        ImmutableListMultimap<String, Question> questionImmutableMap = Multimaps.index(questionList.iterator(), new Function<Question, String>() {
            @Override
            public String apply(Question question) {
                String status;
                if ("ac".equals(question.getStatus())) {
                    status = Constant.STATUS_SOLVED;
                } else if ("notac".equals(question.getStatus())) {
                    status = Constant.STATUS_ATTEMPTED;
                } else {
                    status = Constant.STATUS_TODO;
                }
                return status;
            }
        });

        List<Tag> statusList = Lists.newArrayList();
        for (String key : keyList) {
            Tag tag = new Tag();
            tag.setName(key);
            for (Question question : questionImmutableMap.get(key)) {
                tag.addQuestion(question.getQuestionId());
            }
            statusList.add(tag);
        }
        return statusList;
    }

    public static List<Tag> getTags() {
        List<Tag> tags = new ArrayList<>();

        HttpRequest httpRequest = HttpRequest.get(URLUtils.getLeetcodeTags());
        HttpResponse response = HttpRequestUtils.executeGet(httpRequest);
        if (response != null && response.getStatusCode() == 200) {
            try {
                String body = response.getBody();
                tags = parseTag(body);
            } catch (Exception e1) {
                LogUtils.LOG.error("Request tags exception", e1);
            }
        } else {
            LogUtils.LOG.error("Request tags failed, status:" + response.getStatusCode() + "body:" + response.getBody());
        }

        return tags;
    }

    public static List<Tag> getLists() {
        List<Tag> tags = new ArrayList<>();

        HttpRequest httpRequest = HttpRequest.get(URLUtils.getLeetcodeFavorites());
        HttpResponse response = HttpRequestUtils.executeGet(httpRequest);
        if (response != null && response.getStatusCode() == 200) {
            try {
                String body = response.getBody();
                tags = parseList(body);
            } catch (Exception e1) {
                LogUtils.LOG.error("Request Lists exception", e1);
            }
        } else {
            LogUtils.LOG.error("Request Lists failed, status:" + response.getStatusCode() + "body:" + response.getBody());
        }
        return tags;
    }

    public static List<Tag> getCategory(String categorySlug) {
        List<Tag> tags = new ArrayList<>();

        HttpRequest httpRequest = HttpRequest.get(URLUtils.getLeetcodeCardInfo());
        HttpResponse response = HttpRequestUtils.executeGet(httpRequest);
        if (response != null && response.getStatusCode() == 200) {
            try {
                String body = response.getBody();
                tags = parseCategory(body, categorySlug);
            } catch (Exception e1) {
                LogUtils.LOG.error("Request CardInfo exception", e1);
            }
        } else {
            LogUtils.LOG.error("Request CardInfo failed, status:" + response.getStatusCode() + "body:" + response.getBody());
        }
        return tags;
    }


    private static List<Question> parseQuestion(String str, Boolean isPremium) {

        List<Question> questionList = new ArrayList<Question>();

        if (StringUtils.isNotBlank(str)) {
            JSONObject jsonObject = JSONObject.parseObject(str).getJSONObject("data").getJSONObject("problemsetQuestionList");
            JSONArray jsonArray = jsonObject.getJSONArray("questions");
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                Question question = new Question(object.getString("title"));
                if (URLUtils.isCn() && !PersistentConfig.getInstance().getConfig().getEnglishContent()) {
                    question.setTitle(object.getString("titleCn"));
                }
                question.setLeaf(Boolean.TRUE);
                question.setQuestionId(object.getString("frontendQuestionId"));
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
                try {
                    if (object.containsKey("hasSolution")) {
                        if (object.getBoolean("hasSolution")) {
                            question.setArticleLive(Constant.ARTICLE_LIVE_ONE);
                            question.setArticleSlug(object.getString("titleSlug"));
                            question.setColumnArticles(1);
                        } else {
                            question.setArticleLive(Constant.ARTICLE_LIVE_NONE);
                        }
                    } else if (object.containsKey("solutionNum")) {
                        question.setArticleLive(Constant.ARTICLE_LIVE_LIST);
                        question.setColumnArticles(object.getInteger("solutionNum"));
                    } else {
                        question.setArticleLive(Constant.ARTICLE_LIVE_NONE);
                    }
                } catch (Exception e) {
                    LogUtils.LOG.error("Identify abnormal article", e);
                    question.setArticleLive(Constant.ARTICLE_LIVE_NONE);
                }
                questionList.add(question);
            }
        }
        return questionList;
    }

    private static void translation(List<Question> questions) {

        if (URLUtils.isCn() && !PersistentConfig.getInstance().getConfig().getEnglishContent()) {

            String filePathTranslation = PersistentConfig.getInstance().getTempFilePath() + Constant.DOC_PATH + TRANSLATIONNAME;

            try {
                HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeGraphql(), "application/json");
                httpRequest.setBody("{\"operationName\":\"getQuestionTranslation\",\"variables\":{},\"query\":\"query getQuestionTranslation($lang: String) {\\n  translations: allAppliedQuestionTranslations(lang: $lang) {\\n    title\\n    questionId\\n    __typename\\n  }\\n}\\n\"}");
                httpRequest.addHeader("Accept", "application/json");
                HttpResponse response = HttpRequestUtils.executePost(httpRequest);
                String body;
                if (response != null && response.getStatusCode() == 200) {
                    body = response.getBody();
                    FileUtils.saveFile(filePathTranslation, body);
                } else {
                    body = FileUtils.getFileBody(filePathTranslation);
                }

                if (StringUtils.isNotBlank(body)) {
                    Map<String, String> translationMap = new HashMap<String, String>();
                    JSONArray jsonArray = JSONObject.parseObject(body).getJSONObject("data").getJSONArray("translations");
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        translationMap.put(object.getString("questionId"), object.getString("title"));
                    }
                    for (Question question : questions) {
                        if (translationMap.containsKey(question.getQuestionId())) {
                            question.setTitle(translationMap.get(question.getQuestionId()));
                        }
                    }
                } else {
                    LogUtils.LOG.error("读取翻译内容为空");
                }

            } catch (Exception e1) {
                LogUtils.LOG.error("获取题目翻译错误", e1);
            }

        }
    }

    private static void questionOfToday() {
        if (URLUtils.isCn()) {
            try {
                HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeGraphql(), "application/json");
                httpRequest.setBody("{\"operationName\":\"questionOfToday\",\"variables\":{},\"query\":\"query questionOfToday {\\n  todayRecord {\\n    question {\\n      questionFrontendId\\n      questionTitleSlug\\n      __typename\\n    }\\n    lastSubmission {\\n      id\\n      __typename\\n    }\\n    date\\n    userStatus\\n    __typename\\n  }\\n}\\n\"}");
                httpRequest.addHeader("Accept", "application/json");
                HttpResponse response = HttpRequestUtils.executePost(httpRequest);
                if (response == null || response.getStatusCode() != 200) {
                    QuestionManager.dayQuestion = null;
                } else {
                    QuestionManager.dayQuestion = JSONObject.parseObject(response.getBody()).getJSONObject("data").getJSONArray("todayRecord").getJSONObject(0).getJSONObject("question").getString("questionFrontendId");
                    return;
                }
            } catch (Exception e1) {
            }
        }
        QuestionManager.dayQuestion = null;
    }


    private static List<Tag> parseTag(String str) {
        List<Tag> tags = new ArrayList<Tag>();

        if (StringUtils.isNotBlank(str)) {

            JSONArray jsonArray = JSONObject.parseObject(str).getJSONArray("topics");
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                Tag tag = new Tag();
                tag.setSlug(object.getString("slug"));
                String name = object.getString(URLUtils.getTagName());
                if (StringUtils.isBlank(name)) {
                    name = object.getString("name");
                }
                tag.setName(name);
                JSONArray questionArray = object.getJSONArray("questions");
                for (int j = 0; j < questionArray.size(); j++) {
                    tag.addQuestion(questionArray.getInteger(j).toString());
                }
                tags.add(tag);
            }
        }
        return tags;
    }

    private static List<Tag> parseCategory(String str, String categorySlug) {
        List<Tag> tags = new ArrayList<Tag>();

        if (StringUtils.isNotBlank(str)) {

            JSONArray jsonArray = JSONArray.parseObject(str).getJSONObject("categories").getJSONArray("0");
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                Tag tag = new Tag();
                tag.setSlug(object.getString("slug"));
                tag.setType(URLUtils.getLeetcodeUrl() + "/api" + object.getString("url").replace("problemset", "problems"));
                tag.setName(object.getString("title"));
                if (categorySlug.contains(tag.getSlug())) {
                    tag.setSelect(true);
                }
                tags.add(tag);
            }
        }
        return tags;
    }

    private static List<Tag> parseList(String str) {
        List<Tag> tags = new ArrayList<Tag>();

        if (StringUtils.isNotBlank(str)) {

            JSONArray jsonArray = JSONArray.parseArray(str);
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                Tag tag = new Tag();
                tag.setSlug(object.getString("id"));
                tag.setType(object.getString("type"));
                String name = object.getString("name");
                if (StringUtils.isBlank(name)) {
                    name = object.getString("name");
                }
                tag.setName(name);
                JSONArray questionArray = object.getJSONArray("questions");
                for (int j = 0; j < questionArray.size(); j++) {
                    tag.addQuestion(questionArray.getInteger(j).toString());
                }
                tags.add(tag);
            }
        }
        return tags;
    }

    public static void sortQuestionList(List<Question> list, Sort sort) {
        if (list == null || list.isEmpty() || sort.getType() == Constant.SORT_NONE) {
            return;
        }
        Collections.sort(list, new QuestionComparator(sort));

    }

    private static class QuestionComparator implements Comparator<Question> {

        private Sort sort;

        public QuestionComparator(Sort sort) {
            this.sort = sort;
        }

        @Override
        public int compare(Question o1, Question o2) {
            if (o1.getFrontendQuestionId().equals(dayQuestion)) {
                return -1;
            } else if (o2.getFrontendQuestionId().equals(dayQuestion)) {
                return 1;
            }
            int order = 0;
            if (Constant.SORT_TYPE_ID.equals(sort.getSlug())) {
                String frontendId0 = o1.getFrontendQuestionId();
                String frontendId1 = o2.getFrontendQuestionId();
                if (StringUtils.isNumeric(frontendId0) && StringUtils.isNumeric(frontendId1)) {
                    order = Integer.valueOf(frontendId0).compareTo(Integer.valueOf(frontendId1));
                } else if (StringUtils.isNumeric(frontendId0)) {
                    order = -1;
                } else if (StringUtils.isNumeric(frontendId1)) {
                    order = 1;
                } else {
                    order = frontendId0.compareTo(frontendId1);
                }
            } else if (Constant.SORT_TYPE_TITLE.equals(sort.getSlug())) {
                order = o1.getTitle().compareTo(o2.getTitle());
            } else if (Constant.SORT_TYPE_SOLUTION.equals(sort.getSlug())) {
                order = o1.getColumnArticles().compareTo(o2.getColumnArticles());
            } else if (Constant.SORT_TYPE_ACCEPTANCE.equals(sort.getSlug())) {
                order = o1.getAcceptance().compareTo(o2.getAcceptance());
            } else if (Constant.SORT_TYPE_DIFFICULTY.equals(sort.getSlug())) {
                order = o1.getLevel().compareTo(o2.getLevel());
            } else if (Constant.SORT_TYPE_FREQUENCY.equals(sort.getSlug())) {
                order = o1.getFrequency().compareTo(o2.getFrequency());
            }

            if (sort.getType() == Constant.SORT_DESC) {
                return 0 - order;
            }
            return order;
        }
    }

}
