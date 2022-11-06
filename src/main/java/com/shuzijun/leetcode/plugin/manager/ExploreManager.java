package com.shuzijun.leetcode.plugin.manager;

import java.util.List;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.utils.HttpRequest;
import com.shuzijun.leetcode.plugin.utils.HttpRequestUtils;
import com.shuzijun.leetcode.plugin.utils.HttpResponse;
import com.shuzijun.leetcode.plugin.utils.URLUtils;

/**
 * @author shuzijun
 */
public class ExploreManager {

  private final static Logger logger = LoggerFactory.getLogger(ExploreManager.class);

  public static List<Question> getCategory() {

    List<Question> categories = Lists.newArrayList();

    try {

      HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeGraphql(), "application/json");
      httpRequest.setBody(
        "{\"operationName\":\"GetCategories\",\"variables\":{\"num\":0},\"query\":\"query GetCategories($categorySlug: String, $num: Int) {\\n  categories(slug: $categorySlug) {\\n    id\\n    title\\n    slug\\n    cards(num: $num) {\\n      ...CardDetailFragment\\n      __typename\\n    }\\n    __typename\\n  }\\n  mostRecentCard {\\n    ...CardDetailFragment\\n    progress\\n    __typename\\n  }\\n  allProgress\\n}\\n\\nfragment CardDetailFragment on CardNode {\\n  id\\n  img\\n  title\\n  slug\\n  categorySlug\\n  description\\n  createdAt\\n  lastModified\\n  paidOnly\\n  published\\n  numChapters\\n  numItems\\n  __typename\\n}\\n\"}");
      httpRequest.addHeader("Accept", "application/json");
      HttpResponse response = HttpRequestUtils.executePost(httpRequest);
      if (response != null && response.getStatusCode() == 200) {

        String body = response.getBody();

        JSONArray jsonArray = JSONObject.parseObject(body).getJSONObject("data").getJSONArray("categories");
        for (int i = 0; i < jsonArray.size(); i++) {
          JSONObject object = jsonArray.getJSONObject(i);
          Question question = new Question(object.getString("title"), Constant.NODETYPE_CATEGORY);
          question.setTitleSlug(object.getString("slug"));
          categories.add(question);
        }

      }
    } catch (Exception i) {
      logger.error("获取GetCategories错误", i);
    }

    return categories;
  }

  public static List<Question> getCards(Question q) {

    List<Question> cards = Lists.newArrayList();
    try {
      HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeGraphql(), "application/json");
      httpRequest.setBody("{\"operationName\":\"GetCategories\",\"variables\":{\"categorySlug\":\"" + q.getTitleSlug()
        + "\",\"num\":null},\"query\":\"query GetCategories($categorySlug: String, $num: Int) {\\n  categories(slug: $categorySlug) {\\n    id\\n    title\\n    slug\\n    cards(num: $num) {\\n      ...CardDetailFragment\\n      __typename\\n    }\\n    __typename\\n  }\\n  mostRecentCard {\\n    ...CardDetailFragment\\n    progress\\n    __typename\\n  }\\n  allProgress\\n}\\n\\nfragment CardDetailFragment on CardNode {\\n  id\\n  img\\n  title\\n  slug\\n  categorySlug\\n  description\\n  createdAt\\n  lastModified\\n  paidOnly\\n  published\\n  numChapters\\n  numItems\\n  __typename\\n}\\n\"}");
      httpRequest.addHeader("Accept", "application/json");
      HttpResponse response = HttpRequestUtils.executePost(httpRequest);
      if (response != null && response.getStatusCode() == 200) {

        String body = response.getBody();

        JSONArray jsonArray = JSONObject.parseObject(body).getJSONObject("data").getJSONArray("categories")
          .getJSONObject(0).getJSONArray("cards");
        for (int i = 0; i < jsonArray.size(); i++) {
          JSONObject jsonObject = jsonArray.getJSONObject(i);
          Question card = new Question(jsonObject.getString("title"), Constant.NODETYPE_CARD);
          card.setTitleSlug(jsonObject.getString("slug"));
          card.setStatus(jsonObject.getBoolean("paidOnly") ? "lock" : null);
          cards.add(card);
        }
      }
    } catch (Exception i) {
      logger.error("获取GetCategories错误", i);
    }

    return cards;
  }

  public static List<Question> getChapters(Question q) {
    List<Question> chapters = Lists.newArrayList();

    try {
      HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeGraphql(), "application/json");
      httpRequest.setBody("{\"operationName\":\"GetChapters\",\"variables\":{\"cardSlug\":\"" + q.getTitleSlug()
        + "\"},\"query\":\"query GetChapters($cardSlug: String!) {\\n  chapters(cardSlug: $cardSlug) {\\n    descriptionText\\n    id\\n    title\\n    slug\\n    __typename\\n  }\\n}\\n\"}");
      httpRequest.addHeader("Accept", "application/json");
      HttpResponse response = HttpRequestUtils.executePost(httpRequest);
      if (response != null && response.getStatusCode() == 200) {

        String body = response.getBody();

        JSONArray jsonArray = JSONObject.parseObject(body).getJSONObject("data").getJSONArray("chapters");
        for (int i = 0; i < jsonArray.size(); i++) {
          JSONObject object = jsonArray.getJSONObject(i);
          Question question = new Question(object.getString("title"), Constant.NODETYPE_CHAPTER);
          question.setTitleSlug(q.getTitleSlug());

          question.setQuestionId(object.getString("id"));

          chapters.add(question);
        }

      }
    } catch (Exception i) {
      logger.error("获取GetChapters错误", i);
    }
    return chapters;

  }

  public static List<Question> getChapterItem(Question q) {
    List<Question> chapterItem = Lists.newArrayList();

    try {
      HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeGraphql(), "application/json");
      httpRequest.setBody("{\"operationName\":\"GetChapter\",\"variables\":{\"chapterId\":\"" + q.getQuestionId()
        + "\",\"cardSlug\":\"" + q.getTitleSlug()
        + "\"},\"query\":\"query GetChapter($chapterId: String, $cardSlug: String) {\\n  chapter(chapterId: $chapterId, cardSlug: $cardSlug) {\\n    ...ExtendedChapterDetail\\n    description\\n    __typename\\n  }\\n}\\n\\nfragment ExtendedChapterDetail on ChapterNode {\\n  id\\n  title\\n  slug\\n  items {\\n    id\\n    title\\n    type\\n    info\\n    paidOnly\\n    chapterId\\n    prerequisites {\\n      id\\n      chapterId\\n      __typename\\n    }\\n    __typename\\n  }\\n  __typename\\n}\\n\"}");
      httpRequest.addHeader("Accept", "application/json");
      HttpResponse response = HttpRequestUtils.executePost(httpRequest);
      if (response != null && response.getStatusCode() == 200) {

        String body = response.getBody();

        JSONArray jsonArray =
          JSONObject.parseObject(body).getJSONObject("data").getJSONObject("chapter").getJSONArray("items");
        for (int i = 0; i < jsonArray.size(); i++) {
          JSONObject object = jsonArray.getJSONObject(i);
          Question question = new Question(object.getString("title"), Constant.NODETYPE_ITEM);
          question.setQuestionId(object.getString("id"));
          question.setStatus(object.getBoolean("paidOnly") ? "lock" : null);
          if ("1".equals(object.getString("type"))) {
            question.setLeaf(!object.getBoolean("paidOnly"));
            question.setLangSlug(Constant.ITEM_TYPE_QUESTION);
            chapterItem.add(question);
          } /*else if("3".equals(object.getString("type"))){
               question.setLangSlug(Constant.ITEM_TYPE_HTML);
               chapterItem.add(question);
            }else if("0".equals(object.getString("type"))){
               question.setLangSlug(Constant.ITEM_TYPE_ARTICLE);
               chapterItem.add(question);
            }*/
        }
        if (chapterItem.isEmpty()) {
          chapterItem.add(new Question("no question"));
        }
      }
    } catch (Exception i) {
      logger.error("获取GetChapters错误", i);
    }
    return chapterItem;

  }

  public static Question getItem(Question q) {
    try {
      HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeGraphql(), "application/json");
      httpRequest.setBody("{\"operationName\":\"GetItem\",\"variables\":{\"itemId\":\"" + q.getQuestionId()
        + "\"},\"query\":\"query GetItem($itemId: String!) {\\n  item(id: $itemId) {\\n    id\\n    title\\n    type\\n    paidOnly\\n    lang\\n    question {\\n      questionId\\n      title\\n      titleSlug\\n      __typename\\n    }\\n    article {\\n      id\\n      title\\n      __typename\\n    }\\n    video {\\n      id\\n      __typename\\n    }\\n    htmlArticle {\\n      id\\n      __typename\\n    }\\n    webPage {\\n      id\\n      __typename\\n    }\\n    __typename\\n  }\\n  isCurrentUserAuthenticated\\n}\\n\"}");
      httpRequest.addHeader("Accept", "application/json");
      HttpResponse response = HttpRequestUtils.executePost(httpRequest);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        String body = response.getBody();
        JSONObject object = JSONObject.parseObject(body).getJSONObject("data").getJSONObject("item");
        if (object != null) {
          // todo 你妈的 status自定义的
          if (object.getBoolean("paidOnly")) {
            q.setStatus("lock");
            // 用本地的cn会员去取
          } else {
            q.setStatus(null);
          }

          JSONObject question = object.getJSONObject(q.getLangSlug());
          if (Constant.ITEM_TYPE_QUESTION.equals(q.getLangSlug())) {
            // todo langSlug表示语言版本 用途不详
            q.setQuestionId(question.getString("questionId"));
            q.setFrontendQuestionId(question.getString("questionId"));
            q.setTitleSlug(question.getString("titleSlug"));
          } else {
            q.setNodeType(Constant.NODETYPE_DEF);
            q.setQuestionId(question.getString("id"));
          }
        }
      }
    } catch (Exception i) {
      logger.error("获取GetChapters错误", i);
    }
    return q;
  }

  public static String GetHtmlArticle(Question q) {

    try {
      HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeGraphql(), "application/json");
      httpRequest.setBody("{\"operationName\":\"GetHtmlArticle\",\"variables\":{\"htmlArticleId\":\""
        + q.getQuestionId()
        + "\"},\"query\":\"query GetHtmlArticle($htmlArticleId: String!) {\\n  htmlArticle(id: $htmlArticleId) {\\n    id\\n    html\\n    originalLink\\n    __typename\\n  }\\n}\\n\"}");
      httpRequest.addHeader("Accept", "application/json");
      HttpResponse response = HttpRequestUtils.executePost(httpRequest);
      if (response != null && response.getStatusCode() == 200) {

        String body = response.getBody();

        JSONObject object = JSONObject.parseObject(body).getJSONObject("data").getJSONObject("htmlArticle");
        if (object != null) {
          return object.getString("html");
        }
      }
    } catch (Exception i) {
      logger.error("获取GetChapters错误", i);
    }
    return null;
  }

  public static String GetArticle(Question q) {

    try {
      HttpRequest httpRequest = HttpRequest.post(URLUtils.getLeetcodeGraphql(), "application/json");
      httpRequest.setBody("{\"operationName\":\"GetArticle\",\"variables\":{\"articleId\":\"" + q.getQuestionId()
        + "\"},\"query\":\"query GetArticle($articleId: String!) {\\n  article(id: $articleId) {\\n    id\\n    title\\n    body\\n    __typename\\n  }\\n}\\n\"}");
      httpRequest.addHeader("Accept", "application/json");
      HttpResponse response = HttpRequestUtils.executePost(httpRequest);
      if (response != null && response.getStatusCode() == 200) {

        String body = response.getBody();

        JSONObject object = JSONObject.parseObject(body).getJSONObject("data").getJSONObject("article");
        if (object != null) {
          return object.getString("body");
        }
      }
    } catch (Exception i) {
      logger.error("获取GetChapters错误", i);
    }
    return null;
  }

}
