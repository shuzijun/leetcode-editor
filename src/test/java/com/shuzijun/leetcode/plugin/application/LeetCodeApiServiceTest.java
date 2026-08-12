package com.shuzijun.leetcode.plugin.application;

import com.shuzijun.lc.model.ProblemSetParam;
import com.shuzijun.lc.model.Tag;
import com.shuzijun.lc.model.CodeSnippet;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.PageInfo;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.lc.model.QuestionView;
import com.shuzijun.lc.model.User;
import com.shuzijun.leetcode.plugin.spi.QuestionPresentationStrategy;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class LeetCodeApiServiceTest {

    @After
    public void clearCaches() {
        LeetCodeApiService.invalidateCaches();
    }

    @Test
    public void mapsEveryQuestionPageRequestFieldToTypedSdkParameters() {
        PageInfo<QuestionView> request = new PageInfo<>(3, 25);
        request.setCategorySlug("algorithms");
        request.getFilters().setSearchKeywords("two sum");
        request.getFilters().setOrderBy("FRONTEND_ID");
        request.getFilters().setSortOrder("DESCENDING");
        request.getFilters().setDifficulty("MEDIUM");
        request.getFilters().setStatus("TRIED");
        request.getFilters().setListId("favorite-list");
        request.getFilters().setTags(Arrays.asList("array", "hash-table"));

        ProblemSetParam sdkParam = LeetCodeApiService.toSdkProblemSetParam(request);

        assertEquals(3, sdkParam.getPageIndex());
        assertEquals(25, sdkParam.getPageSize());
        assertEquals(50, sdkParam.getSkip());
        assertEquals("algorithms", sdkParam.getCategorySlug());
        assertEquals("two sum", sdkParam.getFilters().getSearchKeywords());
        assertEquals("FRONTEND_ID", sdkParam.getFilters().getOrderBy());
        assertEquals("DESCENDING", sdkParam.getFilters().getSortOrder());
        assertEquals("MEDIUM", sdkParam.getFilters().getDifficulty());
        assertEquals("TRIED", sdkParam.getFilters().getStatus());
        assertEquals("favorite-list", sdkParam.getFilters().getListId());
        assertEquals(Arrays.asList("array", "hash-table"), sdkParam.getFilters().getTags());
    }

    @Test
    public void mapsTypedQuestionPageWithoutLosingDefaultDisplayState() {
        com.shuzijun.lc.model.QuestionView sdkQuestion = new com.shuzijun.lc.model.QuestionView();
        sdkQuestion.setQuestionId("101");
        sdkQuestion.setFrontendQuestionId("1");
        sdkQuestion.setTitle("Two Sum");
        sdkQuestion.setTitleCn("两数之和");
        sdkQuestion.setTitleSlug("two-sum");
        sdkQuestion.setLevel("Medium");
        sdkQuestion.setAcceptance(0.5d);
        sdkQuestion.setFrequency(0.25d);
        sdkQuestion.setStatus("TRIED");
        sdkQuestion.setPaidOnly(true);
        com.shuzijun.lc.model.PageInfo<com.shuzijun.lc.model.QuestionView> sdkPage =
                new com.shuzijun.lc.model.PageInfo<>(2, 50);
        sdkPage.setRowTotal(100);
        sdkPage.setRows(Collections.singletonList(sdkQuestion));
        PageInfo<QuestionView> target = new PageInfo<>(2, 50);

        LeetCodeApiService.applyQuestionPage(sdkPage, target, false, true);

        assertEquals(100, target.getRowTotal());
        assertEquals(1, target.getRows().size());
        QuestionView question = target.getRows().get(0);
        assertEquals("101", question.getQuestionId());
        assertEquals("1", question.getFrontendQuestionId());
        assertEquals("两数之和", question.getTitle());
        assertEquals("two-sum", question.getTitleSlug());
        assertEquals(Integer.valueOf(2), question.getLevel());
        assertEquals(0.5d, question.getAcceptance(), 0.0001d);
        assertEquals(0.25d, question.getFrequency(), 0.0001d);
        assertEquals("lock", question.getStatus());
        assertEquals("Two Sum", sdkQuestion.getTitle());
        assertEquals("tried", sdkQuestion.getStatus());
    }

    @Test
    public void mapsTypedQuestionDetailAndPreservesRichProductFields() {
        com.shuzijun.lc.model.Question sdkQuestion = new com.shuzijun.lc.model.Question();
        sdkQuestion.setQuestionId("101");
        sdkQuestion.setFrontendQuestionId("1");
        sdkQuestion.setTitle("Two Sum");
        sdkQuestion.setTitleCn("两数之和");
        sdkQuestion.setTitleSlug("two-sum");
        sdkQuestion.setLevel("Easy");
        sdkQuestion.setStatus("AC");
        sdkQuestion.setContent("<p>content</p>");
        sdkQuestion.setTranslatedContent("<p>内容</p>");
        sdkQuestion.setTestCase("[2,7]\n9");
        sdkQuestion.setExampleTestcases("[2,7]\n9");
        sdkQuestion.setLikes(100);
        sdkQuestion.setDislikes(5);
        sdkQuestion.setSimilarQuestions("[]");
        sdkQuestion.setHints(Collections.singletonList("Use a map"));
        sdkQuestion.setSolution("{\"id\":\"solution\"}");
        com.shuzijun.lc.model.CodeSnippet sdkSnippet = new com.shuzijun.lc.model.CodeSnippet();
        sdkSnippet.setLang("Java");
        sdkSnippet.setLangSlug("java");
        sdkSnippet.setCode("class Solution {\n}");
        sdkQuestion.setCodeSnippets(Collections.singletonList(sdkSnippet));
        Tag topic = new Tag();
        topic.setName("Array");
        topic.setTranslatedName("数组");
        sdkQuestion.setTopicTags(Collections.singletonList(topic));
        com.shuzijun.lc.model.CodeMetaData metaData = new com.shuzijun.lc.model.CodeMetaData();
        metaData.setName("twoSum");
        sdkQuestion.setCodeMetaData(metaData);

        Question question = LeetCodeApiService.toPluginQuestion(
                sdkQuestion,
                true,
                true,
                true,
                new RecordingQuestionPresentationStrategy()
        );

        assertEquals("101", question.getQuestionId());
        assertEquals("1", question.getFrontendQuestionId());
        assertEquals("两数之和", question.getTitle());
        assertEquals("two-sum", question.getTitleSlug());
        assertEquals(Integer.valueOf(1), question.getLevel());
        assertEquals("ac", question.getStatus());
        assertEquals("[2,7]\n9", question.getTestCase());
        assertEquals("[2,7]\n9", question.getExampleTestcases());
        assertEquals(Constant.ARTICLE_LIVE_LIST, question.getArticleLive());
        assertEquals("translated=true,topics=true,title=Two Sum", question.getContent());
        assertEquals("[]", question.getSimilarQuestions());
        assertEquals(Collections.singletonList("Use a map"), question.getHints());
        assertNotNull(question.getCodeMetaData());
        assertEquals("twoSum", question.getCodeMetaData().getName());
        assertEquals(1, question.getCodeSnippets().size());
        CodeSnippet codeSnippet = question.getCodeSnippets().get(0);
        assertEquals("Java", codeSnippet.getLang());
        assertEquals("java", codeSnippet.getLangSlug());
        assertEquals("class Solution {\n}", codeSnippet.getCode());

        Question comQuestion = LeetCodeApiService.toPluginQuestion(
                sdkQuestion,
                false,
                false,
                false,
                new RecordingQuestionPresentationStrategy()
        );
        assertEquals(Constant.ARTICLE_LIVE_LIST, comQuestion.getArticleLive());
        assertNull(comQuestion.getArticleSlug());
    }

    @Test
    public void separatesQuestionCacheKeysByHostAndSlug() {
        assertEquals(
                "leetcode.com\ntwo-sum",
                LeetCodeApiService.questionKey("leetcode.com", "two-sum")
        );
        assertEquals(
                "leetcode.cn\ntwo-sum",
                LeetCodeApiService.questionKey("leetcode.cn", "two-sum")
        );
    }

    @Test
    public void invalidatesEveryApiCacheForOnlyRequestedHost() throws Exception {
        String firstHost = "cache-first.example.com";
        String secondHost = "cache-second.example.com";
        ShortLivedCache<Object> pageCache = cache("QUESTION_PAGE_CACHE");
        ShortLivedCache<Object> questionCache = cache("QUESTION_CACHE");
        ShortLivedCache<Object> catalogCache = cache("QUESTION_CATALOG_CACHE");
        ShortLivedCache<Object> dailyCache = cache("DAILY_QUESTION_CACHE");
        Object firstPage = new Object();
        Object secondPage = new Object();
        Object firstQuestion = new Object();
        Object secondQuestion = new Object();
        Object firstCatalog = new Object();
        Object secondCatalog = new Object();
        Object firstDaily = new Object();
        Object secondDaily = new Object();

        pageCache.put(firstHost + "\npage", firstPage);
        pageCache.put(secondHost + "\npage", secondPage);
        questionCache.put(firstHost + "\nquestion", firstQuestion);
        questionCache.put(secondHost + "\nquestion", secondQuestion);
        catalogCache.put(firstHost, firstCatalog);
        catalogCache.put(secondHost, secondCatalog);
        dailyCache.put(firstHost + "\ndaily", firstDaily);
        dailyCache.put(secondHost + "\ndaily", secondDaily);

        LeetCodeApiService.invalidateCaches(firstHost);
        LeetCodeApiService.invalidateCaches(firstHost);

        assertNull(pageCache.getIfPresent(firstHost + "\npage"));
        assertNull(questionCache.getIfPresent(firstHost + "\nquestion"));
        assertNull(catalogCache.getIfPresent(firstHost));
        assertNull(dailyCache.getIfPresent(firstHost + "\ndaily"));
        assertSame(secondPage, pageCache.getIfPresent(secondHost + "\npage"));
        assertSame(secondQuestion, questionCache.getIfPresent(secondHost + "\nquestion"));
        assertSame(secondCatalog, catalogCache.getIfPresent(secondHost));
        assertSame(secondDaily, dailyCache.getIfPresent(secondHost + "\ndaily"));
    }

    @Test
    public void invalidatesAllApiCaches() throws Exception {
        String host = "cache-all.example.com";
        ShortLivedCache<Object> pageCache = cache("QUESTION_PAGE_CACHE");
        ShortLivedCache<Object> questionCache = cache("QUESTION_CACHE");
        ShortLivedCache<Object> catalogCache = cache("QUESTION_CATALOG_CACHE");
        ShortLivedCache<Object> dailyCache = cache("DAILY_QUESTION_CACHE");

        pageCache.put(host + "\npage", new Object());
        questionCache.put(host + "\nquestion", new Object());
        catalogCache.put(host, new Object());
        dailyCache.put(host + "\ndaily", new Object());

        LeetCodeApiService.invalidateCaches();
        LeetCodeApiService.invalidateCaches();

        assertNull(pageCache.getIfPresent(host + "\npage"));
        assertNull(questionCache.getIfPresent(host + "\nquestion"));
        assertNull(catalogCache.getIfPresent(host));
        assertNull(dailyCache.getIfPresent(host + "\ndaily"));
    }

    @SuppressWarnings("unchecked")
    private static ShortLivedCache<Object> cache(String fieldName) throws Exception {
        Field field = LeetCodeApiService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (ShortLivedCache<Object>) field.get(null);
    }

    private static final class RecordingQuestionPresentationStrategy
            implements QuestionPresentationStrategy {

        @Override
        public @NotNull String renderContent(
                @NotNull com.shuzijun.lc.model.Question question,
                boolean translatedContent,
                boolean showTopics
        ) {
            return "translated=" + translatedContent
                    + ",topics=" + showTopics
                    + ",title=" + question.getTitle();
        }
    }
}
