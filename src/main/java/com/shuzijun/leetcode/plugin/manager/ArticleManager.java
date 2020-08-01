package com.shuzijun.leetcode.plugin.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.model.Solution;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.*;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author shuzijun
 */
public class ArticleManager {

    private static final Pattern latexPattern = Pattern.compile("\\$+[\\s|\\S]*?\\$+");
    private static final Pattern imageListPattern = Pattern.compile("<(!\\[.*]\\(.*\\),*)+>?");
    private static final Pattern imagePattern = Pattern.compile("<img.*\\.\\..*>?");
    private static final Pattern linkPattern = Pattern.compile("]\\(\\.\\.[\\w|/]*?\\.\\w+\\)?");
    private static final Pattern gifPattern = Pattern.compile("!\\[.*?]\\(.*?\\.gif\\)?");
    private static final Pattern playgroundPattern = Pattern.compile("<iframe.*playground.*</iframe>?");
    private static final Pattern imageGroupPattern = Pattern.compile("!\\?!\\.\\..*\\?!?");

    public static void openArticle(Question question, Project project) {

        String filePath = PersistentConfig.getInstance().getTempFilePath() + "doc" + File.separator + question.getArticleSlug() + ".md";

        File file = new File(filePath);
        if (!file.exists()) {
            String article;
            if (URLUtils.isCn()) {
                article = getCnArticle(question, project);
            } else {
                article = getEnArticle(question, project);
            }
            if (StringUtils.isBlank(article)) {
                return;
            }
            article = formatMarkdown(article, project);

            FileUtils.saveFile(file, article);
        }
        FileUtils.openFileEditor(file, project);
    }

    private static String getEnArticle(Question question, Project project) {
        try {
            HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeGraphql(), "application/json");
            httpRequest.setBody("{\"operationName\":\"QuestionNote\",\"variables\":{\"titleSlug\":\"" + question.getArticleSlug() + "\"},\"query\":\"query QuestionNote($titleSlug: String!) " +
                    "{\\n  question(titleSlug: $titleSlug) {\\n    questionId\\n    article\\n    solution {\\n      id\\n      url\\n      content\\n      contentTypeId\\n      canSeeDetail\\n      paidOnly\\n      " +
                    "rating {\\n        id\\n        count\\n        average\\n        userRating {\\n          score\\n          __typename\\n        }\\n        __typename\\n      }\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\"}");
            httpRequest.addHeader("Accept", "application/json");
            HttpResponse response = HttpRequestUtils.executePost(httpRequest);
            if (response.getStatusCode() == 200) {
                String content = JSONObject.parseObject(response.getBody()).getJSONObject("data").getJSONObject("question").getJSONObject("solution").getString("content");
                if (StringUtils.isBlank(content)) {
                    MessageUtils.getInstance(project).showWarnMsg("error", PropertiesUtils.getInfo("request.auth"));
                    return null;
                }else {
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
                }else {
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


    public static String formatMarkdown(String content, Project project) {
        String article = content.replaceAll("\\{:\\w+=\"?.*\"?}?", "");
        Matcher latexMatcher = latexPattern.matcher(content);
        while (latexMatcher.find()) {
            String group = latexMatcher.group();
            if (group.contains("\\")) {
                String fileName = "p_" + group.replaceAll("\\$+| |/|>|<|\\(|\\)|\\s|\\[|]", "_").replace("\\", "") + ".png";
                String filePath = PersistentConfig.getInstance().getTempFilePath() + "doc" + File.separator + fileName;
                File file = new File(filePath);
                if (!file.exists()) {
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    try {
                        TeXFormula formula = new TeXFormula(group.replaceAll("\\$+|\\s", " "));
                        formula.createPNG(TeXConstants.STYLE_DISPLAY, 14, filePath, null, JBColor.BLACK);
                    } catch (Exception e) {
                        LogUtils.LOG.error("TeXFormula error", e);
                    }
                }
                article = article.replace(group, "![" + group.replaceAll("\\$+|\\s", "").replaceAll("([\\[\\]])", "\\\\$1") + " ](./" + fileName + ") ");
            } else {
                article = article.replace(group, group.replaceAll("\\$+", "*"));
            }
        }
        Matcher imageListMatcher = imageListPattern.matcher(content);
        while (imageListMatcher.find()) {
            article = article.replace(imageListMatcher.group(), imageListMatcher.group().replaceAll("[<>,]"," "));
        }

        Matcher linkMatcher = linkPattern.matcher(content);
        while (linkMatcher.find()) {
            article = article.replace(linkMatcher.group(), linkMatcher.group().replace("..", URLUtils.getLeetcodeProblems()));
        }
        Matcher imageMatcher = imagePattern.matcher(content);
        while (imageMatcher.find()) {
            article = article.replace(imageMatcher.group(), imageMatcher.group().replace("..", URLUtils.getLeetcodeProblems()));
        }
        Matcher gifMatcher = gifPattern.matcher(content);
        while (gifMatcher.find()) {
            article = article.replace(gifMatcher.group(), gifMatcher.group().replace("!", " "));
        }

        Matcher playgroundMatcher = playgroundPattern.matcher(content);
        while (playgroundMatcher.find()) {
            String group = playgroundMatcher.group();
            String name = Jsoup.parse(group).select("iframe").attr("name");
            HttpRequest playgroundHttpRequest = HttpRequest.post(URLUtils.getLeetcodeGraphql(), "application/json");
            playgroundHttpRequest.setBody("{\"operationName\":\"fetchPlayground\",\"variables\":{},\"query\":\"query fetchPlayground {\\n  playground(uuid: \\\"" + name + "\\\") {\\n    testcaseInput\\n    name\\n    isUserOwner\\n" +
                    "    isLive\\n    showRunCode\\n    showOpenInPlayground\\n    selectedLangSlug\\n    isShared\\n    __typename\\n  }\\n  allPlaygroundCodes(uuid: \\\"" + name + "\\\") {\\n    code\\n    langSlug\\n    __typename\\n  }\\n}\\n\"}");
            playgroundHttpRequest.addHeader("Accept", "application/json");
            HttpResponse playgroundResponse = HttpRequestUtils.executePost(playgroundHttpRequest);
            if (playgroundResponse.getStatusCode() == 200) {
                String playgroundCode = JSONObject.parseObject(playgroundResponse.getBody()).getJSONObject("data").getJSONArray("allPlaygroundCodes").getJSONObject(0).getString("code");
                article = article.replace(group, "```\n" + playgroundCode + "\n```");
            }
        }

        Matcher imageGroupMatcher = imageGroupPattern.matcher(content);
        while (imageGroupMatcher.find()) {
            String group = imageGroupMatcher.group();
            HttpRequest imageGroupHttpRequest = HttpRequest.get(URLUtils.getLeetcodeProblems()+group.replace("!?!..","").replaceAll(":\\d+,\\d+!\\?!",""));
            HttpResponse imageGroupResponse = HttpRequestUtils.executeGet(imageGroupHttpRequest);
            if (imageGroupResponse.getStatusCode() == 200) {
                JSONArray jsonArray = JSON.parseObject(imageGroupResponse.getBody()).getJSONArray("timeline");
                StringBuffer imgs = new StringBuffer();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    imgs.append("![").append(i).append(" ](").append(URLUtils.getLeetcodeProblems()).append(jsonObject.getString("image").replace("..","")).append(" )  ");
                }
                article = article.replace(group, imgs.toString());
            }

        }



        return article;
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
