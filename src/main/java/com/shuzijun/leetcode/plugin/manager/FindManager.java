package com.shuzijun.leetcode.plugin.manager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.HttpRequest;
import com.shuzijun.leetcode.plugin.model.Tag;
import com.shuzijun.leetcode.plugin.utils.*;
import com.shuzijun.leetcode.plugin.window.WindowFactory;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shuzijun
 */
public class FindManager {

    public static List<Tag> getDifficulty() {

        List<String> keyList = Lists.newArrayList(Constant.DIFFICULTY_EASY, Constant.DIFFICULTY_MEDIUM, Constant.DIFFICULTY_HARD);
        List<Tag> difficultyList = Lists.newArrayList();
        for (String key : keyList) {
            Tag tag = new Tag();
            tag.setName(key);
            tag.setSlug(key.toUpperCase());
            difficultyList.add(tag);
        }
        return difficultyList;
    }

    public static List<Tag> getStatus() {
        List<String> keyList = Lists.newArrayList(Constant.STATUS_TODO, Constant.STATUS_SOLVED, Constant.STATUS_ATTEMPTED);

        List<Tag> statusList = Lists.newArrayList();
        for (String key : keyList) {
            Tag tag = new Tag();
            tag.setName(key);
            if (Constant.STATUS_TODO.equals(key)) {
                tag.setSlug("NOT_STARTED");
            } else if (Constant.STATUS_SOLVED.equals(key)) {
                tag.setSlug("AC");
            } else if (Constant.STATUS_ATTEMPTED.equals(key)) {
                tag.setSlug("TRIED");
            }
            statusList.add(tag);
        }
        return statusList;
    }

    public static List<Tag> getTags() {
        List<Tag> tags = new ArrayList<>();

        HttpResponse response = HttpRequest.builderGet(URLUtils.getLeetcodeTags()).cache(true).request();
        if (response.getStatusCode() == 200) {
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

    public static List<Tag> getLists(Project project) {
        List<Tag> tags = new ArrayList<>();

        HttpResponse response =  HttpRequest.builderGet(URLUtils.getLeetcodeFavorites())
                .cacheParam(WindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_PROJECTS_TABS).getUser().getUsername()).request();
        if (response.getStatusCode() == 200) {
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

    public static List<Tag> getCategory() {
        List<Tag> tags = new ArrayList<>();

        HttpResponse response = HttpRequest.builderGet(URLUtils.getLeetcodeCardInfo()).cache(true).request();
        if (response.getStatusCode() == 200) {
            try {
                String body = response.getBody();
                tags = parseCategory(body);
            } catch (Exception e1) {
                LogUtils.LOG.error("Request CardInfo exception", e1);
            }
        } else {
            LogUtils.LOG.error("Request CardInfo failed, status:" + response.getStatusCode() + "body:" + response.getBody());
        }
        return tags;
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

    private static List<Tag> parseCategory(String str) {
        List<Tag> tags = new ArrayList<Tag>();

        if (StringUtils.isNotBlank(str)) {

            JSONArray jsonArray = JSONArray.parseObject(str).getJSONObject("categories").getJSONArray("0");
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                Tag tag = new Tag();
                tag.setSlug(object.getString("slug"));
                tag.setType(URLUtils.getLeetcodeUrl() + "/api" + object.getString("url").replace("problemset", "problems"));
                tag.setName(object.getString("title"));
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
