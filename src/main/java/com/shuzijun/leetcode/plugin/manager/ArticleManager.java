package com.shuzijun.leetcode.plugin.manager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.Graphql;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.model.Solution;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;
import com.shuzijun.leetcode.plugin.utils.doc.CleanMarkdown;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shuzijun
 */
public class ArticleManager {

    public static File openArticle(String titleSlug, String articleSlug, Project project, Boolean isOpenEditor) {
        String filePath = PersistentConfig.getInstance().getTempFilePath() + Constant.DOC_SOLUTION + articleSlug + "." + PluginConstant.LEETCODE_EDITOR_VIEW;

        File file = new File(filePath);
        String host;
        if (!file.exists()) {
            String article = getArticle(titleSlug, articleSlug, project);
            if (URLUtils.isCn()) {
                host = URLUtils.getLeetcodeProblems() + titleSlug + "/solution/" + articleSlug + "/";
            } else {
                host = URLUtils.getLeetcodeProblems() + titleSlug + "/solution/";
            }
            if (StringUtils.isBlank(article)) {
                return file;
            }
            article = formatMarkdown(article, host);

            FileUtils.saveFile(file, article);
        }
        if (isOpenEditor) {
            FileUtils.openFileEditor(file, project);
        }
        return file;
    }

    private static String getArticle(String titleSlug, String articleSlug, Project project) {
        try {
            HttpResponse response = Graphql.builder().cn(URLUtils.isCn()).operationName("solutionDetailArticle").
                    variables("slug", articleSlug).variables("titleSlug", titleSlug).variables("orderBy", "DEFAULT").request();
            if (response.getStatusCode() == 200) {
                String content;
                if (URLUtils.isCn()) {
                    content = JSONObject.parseObject(response.getBody()).getJSONObject("data").getJSONObject("solutionArticle").getString("content");
                } else {
                    content = JSONObject.parseObject(response.getBody()).getJSONObject("data").getJSONObject("solutionArticle").getJSONObject("solution").getString("content");
                }
                if (StringUtils.isBlank(content)) {
                    MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.auth"));
                    return null;
                } else {
                    return content;
                }
            } else {
                MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("response.code"));
            }
        } catch (Exception e) {
            LogUtils.LOG.error("article acquisition failed", e);
            MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("response.code"));
        }
        return null;
    }


    public static String formatMarkdown(String content, String host) {
        return CleanMarkdown.cleanMarkdown(content, host);
    }

    public static List<Solution> getSolutionList(String titleSlug, Project project) {
        List<Solution> solutionList = new ArrayList<>();

        try {
            HttpResponse response = Graphql.builder().cn(URLUtils.isCn()).operationName("questionSolutionArticles").
                    variables("questionSlug", titleSlug).variables("first", 200).variables("skip", 0).variables("orderBy", "DEFAULT").request();
            if (response.getStatusCode() == 200) {
                JSONArray edges = JSONObject.parseObject(response.getBody()).getJSONObject("data").getJSONObject("questionSolutionArticles").getJSONArray("edges");
                for (int i = 0; i < edges.size(); i++) {
                    JSONObject node = edges.getJSONObject(i).getJSONObject("node");
                    Solution solution = new Solution();
                    solution.setTitle(node.getString("title"));
                    solution.setSlug(node.getString("slug"));
                    solution.setSummary(node.getString("summary"));

                    StringBuilder tagsSb = new StringBuilder();
                    JSONArray tags = node.getJSONArray("tags");
                    for (int j = 0; j < tags.size(); j++) {
                        tagsSb.append("[").append(tags.getJSONObject(j).getString("name")).append("] ");
                    }
                    solution.setTags(tagsSb.toString());
                    solutionList.add(solution);
                }
            } else {
                MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("response.code"));
            }
        } catch (Exception e) {
            LogUtils.LOG.error("solutionList acquisition failed", e);
            MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("response.code"));
        }
        return solutionList;

    }
}
