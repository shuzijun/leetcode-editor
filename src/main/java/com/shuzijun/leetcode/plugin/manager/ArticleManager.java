package com.shuzijun.leetcode.plugin.manager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.model.Question;
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

    public static void openArticle(Question question, Project project) {

        String filePath = PersistentConfig.getInstance().getTempFilePath() + Constant.DOC_SOLUTION + question.getCnSlug() + "." + PluginConstant.LEETCODE_EDITOR_VIEW;

        File file = new File(filePath);
        String host = "";
        if (!file.exists()) {
            String article;
            if (URLUtils.isCn()) {
                article = getCnArticle(question, project);
                host = URLUtils.getLeetcodeProblems() + question.getTitleSlug() + "/solution/" + question.getArticleSlug() + "/";
            } else {
                article = getEnArticle(question, project);
                host = URLUtils.getLeetcodeProblems() + question.getTitleSlug() + "/solution/";
            }
            if (StringUtils.isBlank(article)) {
                return;
            }
            article = formatMarkdown(article, project, host);

            FileUtils.saveFile(file, article);
        }
        FileUtils.openFileEditor(file, project);
    }

    private static String getEnArticle(Question question, Project project) {
        try {
            HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeGraphql(), "application/json");
            httpRequest.setBody("{\"operationName\":\"QuestionNote\",\"variables\":{\"titleSlug\":\"" + question.getTitleSlug() + "\"},\"query\":\"query QuestionNote($titleSlug: String!) " +
                    "{\\n  question(titleSlug: $titleSlug) {\\n    questionId\\n    article\\n    solution {\\n      id\\n      content\\n      contentTypeId\\n      canSeeDetail\\n   " +
                    "   paidOnly\\n      hasVideoSolution\\n      paidOnlyVideo\\n      rating {\\n        id\\n        count\\n        average\\n        userRating {\\n          score\\n  " +
                    "        __typename\\n        }\\n        __typename\\n      }\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}");
            httpRequest.addHeader("Accept", "application/json");
            HttpResponse response = HttpRequestUtils.executePost(httpRequest);
            if (response.getStatusCode() == 200) {
                String content = JSONObject.parseObject(response.getBody()).getJSONObject("data").getJSONObject("question").getJSONObject("solution").getString("content");
                if (StringUtils.isBlank(content)) {
                    MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.auth"));
                    return null;
                } else {
                    return content;
                }
            }
        } catch (Exception e) {
            LogUtils.LOG.error("article acquisition failed", e);
            MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("response.code"));
        }
        return null;
    }


    private static String getCnArticle(Question question, Project project) {
        try {
            HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeGraphql(), "application/json");
            httpRequest.setBody("{\"operationName\":\"solutionDetailArticle\",\"variables\":{\"slug\":\"" + question.getArticleSlug() + "\",\"orderBy\":\"DEFAULT\"}," +
                    "\"query\":\"query solutionDetailArticle($slug: String!, $orderBy: SolutionArticleOrderBy!) {\\n  solutionArticle(slug: $slug, orderBy: $orderBy) " +
                    "{\\n    ...solutionArticle\\n    content\\n    question {\\n      questionTitleSlug\\n      __typename\\n    }\\n    position\\n    next {\\n      slug\\n   " +
                    "   title\\n      __typename\\n    }\\n    prev {\\n      slug\\n      title\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\\nfragment solutionArticle on SolutionArticleNode " +
                    "{\\n  uuid\\n  title\\n  slug\\n  chargeType\\n  status\\n  identifier\\n  canEdit\\n  reactionType\\n  reactionsV2 {\\n    count\\n    reactionType\\n    __typename\\n  }\\n  tags {\\n   " +
                    " name\\n    nameTranslated\\n    slug\\n    __typename\\n  }\\n  createdAt\\n  thumbnail\\n  author {\\n    username\\n    profile {\\n      userAvatar\\n      userSlug\\n      realName\\n    " +
                    "  __typename\\n    }\\n    __typename\\n  }\\n  summary\\n  topic {\\n    id\\n    commentCount\\n    viewCount\\n    __typename\\n  }\\n  byLeetcode\\n  isMyFavorite\\n  isMostPopular\\n " +
                    " isEditorsPick\\n  hitCount\\n  videosInfo {\\n    videoId\\n    coverUrl\\n    duration\\n    __typename\\n  }\\n  __typename\\n}\\n\"}");
            HttpResponse response = HttpRequestUtils.executePost(httpRequest);
            if (response.getStatusCode() == 200) {
                String content = JSONObject.parseObject(response.getBody()).getJSONObject("data").getJSONObject("solutionArticle").getString("content");
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


    public static String formatMarkdown(String content, Project project, String host) {
        return CleanMarkdown.cleanMarkdown(content, host);
    }

    public static List<Solution> getSolutionList(Question question, Project project) {
        List<Solution> solutionList = new ArrayList<>();

        try {
            HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeGraphql(), "application/json");
            httpRequest.setBody("{\"operationName\":\"questionSolutionArticles\",\"variables\":{\"questionSlug\":\"" + question.getTitleSlug() + "\",\"first\":10,\"skip\":0,\"orderBy\":\"DEFAULT\"},\"query\":\"query " +
                    "questionSolutionArticles($questionSlug: String!, $skip: Int, $first: Int, $orderBy: SolutionArticleOrderBy, $userInput: String, $tagSlugs: [String!]) {\\n  questionSolutionArticles(questionSlug: $questionSlug, " +
                    "skip: $skip, first: $first, orderBy: $orderBy, userInput: $userInput, tagSlugs: $tagSlugs) {\\n    totalNum\\n    edges {\\n      node {\\n        ...solutionArticle\\n        __typename\\n      }\\n      __typename\\n    }\\n " +
                    "   __typename\\n  }\\n}\\n\\nfragment solutionArticle on SolutionArticleNode {\\n  uuid\\n  title\\n  slug\\n  chargeType\\n  status\\n  identifier\\n  canEdit\\n  reactionType\\n  reactionsV2 {\\n    count\\n    reactionType\\n    " +
                    "__typename\\n  }\\n  tags {\\n    name\\n    nameTranslated\\n    slug\\n    __typename\\n  }\\n  createdAt\\n  thumbnail\\n  author {\\n    username\\n    profile {\\n      userAvatar\\n      userSlug\\n      realName\\n      __typename\\n    }\\n  " +
                    "  __typename\\n  }\\n  summary\\n  topic {\\n    id\\n    commentCount\\n    viewCount\\n    __typename\\n  }\\n  byLeetcode\\n  isMyFavorite\\n  isMostPopular\\n  isEditorsPick\\n  hitCount\\n  videosInfo {\\n    videoId\\n    coverUrl\\n    duration\\n    __typename\\n  }\\n  __typename\\n}\\n\"}");
            httpRequest.addHeader("Accept", "application/json");
            HttpResponse response = HttpRequestUtils.executePost(httpRequest);
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
