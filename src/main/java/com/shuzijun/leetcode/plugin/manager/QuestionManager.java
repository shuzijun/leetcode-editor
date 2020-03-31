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


    public static List<Question> getQuestionService(Project project) {
        List<Question> questionList = null;

        HttpRequest httpRequest = HttpRequest.get(URLUtils.getLeetcodeAll());
        HttpResponse response = HttpRequestUtils.executeGet(httpRequest);
        if (response != null && response.getStatusCode() == 200) {
            questionList = parseQuestion(response.getBody());
            JSONObject jsonObject = JSONObject.parseObject(response.getBody());
            ApplicationManager.getApplication().invokeAndWait(() -> {
                WindowFactory.updateTitle(project, jsonObject.getString("user_name"));
            });
        } else {
            LogUtils.LOG.error("Request question list failed, status:" + response == null ? "" : response.getStatusCode());
        }

        if (questionList != null && !questionList.isEmpty()) {
            String filePath = PersistentConfig.getInstance().getTempFilePath() + ALLNAME;
            FileUtils.saveFile(filePath, JSON.toJSONString(questionList));
            QUESTIONLIST = questionList;
        }
        return questionList;

    }

    public static List<Question> getQuestionCache() {
        if (QUESTIONLIST != null) {
            return QUESTIONLIST;
        }

        String filePath = PersistentConfig.getInstance().getTempFilePath() + ALLNAME;
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


    private static List<Question> parseQuestion(String str) {

        List<Question> questionList = new ArrayList<Question>();

        if (StringUtils.isNotBlank(str)) {
            JSONObject jsonObject = JSONObject.parseObject(str);
            Boolean isPremium = new Integer("0").equals(jsonObject.getInteger("frequency_high")); //Premium users display frequency
            JSONArray jsonArray = jsonObject.getJSONArray("stat_status_pairs");
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                Question question = new Question(object.getJSONObject("stat").getString("question__title"));
                question.setLeaf(Boolean.TRUE);
                question.setQuestionId(object.getJSONObject("stat").getString("question_id"));
                question.setFrontendQuestionId(object.getJSONObject("stat").getString("frontend_question_id"));
                try {
                    if (object.getBoolean("paid_only") && isPremium) {
                        question.setStatus(object.getBoolean("paid_only") ? "lock" : null);
                    } else {
                        question.setStatus(object.get("status") == null ? "" : object.getString("status"));
                    }
                } catch (Exception ee) {
                    question.setStatus("");
                }
                question.setTitleSlug(object.getJSONObject("stat").getString("question__title_slug"));
                question.setLevel(object.getJSONObject("difficulty").getInteger("level"));
                questionList.add(question);
            }

            translation(questionList);

            Collections.sort(questionList, new Comparator<Question>() {
                @Override
                public int compare(Question arg0, Question arg1) {
                    String frontendId0 = arg0.getFrontendQuestionId();
                    String frontendId1 = arg1.getFrontendQuestionId();
                    if (StringUtils.isNumeric(frontendId0) && StringUtils.isNumeric(frontendId1)) {
                        return Integer.valueOf(frontendId0).compareTo(Integer.valueOf(frontendId1));
                    } else if (StringUtils.isNumeric(frontendId0)) {
                        return -1;
                    } else if (StringUtils.isNumeric(frontendId1)) {
                        return 1;
                    } else {
                        return frontendId0.compareTo(frontendId1);
                    }

                }
            });
        }
        return questionList;

    }

    private static void translation(List<Question> questions) {

        if (URLUtils.getQuestionTranslation()) {

            String filePathTranslation = PersistentConfig.getInstance().getTempFilePath() + TRANSLATIONNAME;

            try {
                HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeGraphql(),"application/json");
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

}
