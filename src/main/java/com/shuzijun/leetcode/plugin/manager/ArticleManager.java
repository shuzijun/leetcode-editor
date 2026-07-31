package com.shuzijun.leetcode.plugin.manager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.Graphql;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.model.Solution;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;
import com.shuzijun.leetcode.plugin.utils.doc.CleanMarkdown;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author shuzijun
 */
public class ArticleManager {

    private static final int SOLUTION_PAGE_COUNT = 4;
    private static final int SOLUTION_PAGE_SIZE = 30;
    private static final long SOLUTION_LOAD_TIMEOUT_SECONDS = 35;

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
            CountDownLatch latch = new CountDownLatch(SOLUTION_PAGE_COUNT);
            List<Solution>[] results = new List[SOLUTION_PAGE_COUNT];
            List<Future<?>> requests = new ArrayList<>(SOLUTION_PAGE_COUNT);

            for (int i = 0; i < SOLUTION_PAGE_COUNT; i++) {
                int pageIndex = i;
                int skip = pageIndex * SOLUTION_PAGE_SIZE;
                requests.add(AppExecutorUtil.getAppExecutorService().submit(() -> {
                    try {
                        List<Solution> solutions = new ArrayList<>();
                        HttpResponse response = Graphql.builder().cn(URLUtils.isCn()).operationName("questionSolutionArticles").
                                variables("questionSlug", titleSlug).variables("first", SOLUTION_PAGE_SIZE).variables("skip", skip).variables("orderBy", "DEFAULT").request();
                        if (response.getStatusCode() == 200) {
                            JSONArray edges = JSONObject.parseObject(response.getBody()).getJSONObject("data").getJSONObject("questionSolutionArticles").getJSONArray("edges");
                            for (int j = 0; j < edges.size(); j++) {
                                JSONObject node = edges.getJSONObject(j).getJSONObject("node");
                                Solution solution = new Solution();
                                solution.setTitle(node.getString("title"));
                                solution.setSlug(node.getString("slug"));
                                solution.setSummary(node.getString("summary"));

                                StringBuilder tagsSb = new StringBuilder();
                                JSONArray tags = node.getJSONArray("tags");
                                for (int k = 0; k < tags.size(); k++) {
                                    tagsSb.append("[").append(tags.getJSONObject(k).getString("name")).append("] ");
                                }
                                solution.setTags(tagsSb.toString());
                                solutions.add(solution);
                            }
                            results[pageIndex] = solutions;
                        } else {
                            MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("response.code"));
                        }
                    } finally {
                        latch.countDown();
                    }
                }));
            }

            if (!latch.await(SOLUTION_LOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                LogUtils.LOG.warn("Timed out loading solution list for " + titleSlug);
                for (Future<?> request : requests) {
                    request.cancel(true);
                }
            }
            for (List<Solution> pageResult : results) {
                if (pageResult != null) {
                    solutionList.addAll(pageResult);
                }
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            LogUtils.LOG.warn("Interrupted while loading solution list for " + titleSlug, exception);
        } catch (Exception e) {
            LogUtils.LOG.error("solutionList acquisition failed", e);
            MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("response.code"));
        }
        return solutionList;

    }
}
