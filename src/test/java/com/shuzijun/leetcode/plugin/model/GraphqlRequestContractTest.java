package com.shuzijun.leetcode.plugin.model;

import com.alibaba.fastjson.JSONObject;
import com.shuzijun.leetcode.plugin.utils.HttpRequestUtils;
import com.shuzijun.leetcode.plugin.utils.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GraphqlRequestContractTest {

    @Before
    public void setUp() {
        System.setProperty("leetcode.test.base.url", "http://localhost:8080");
    }

    @After
    public void tearDown() {
        HttpRequestUtils.setTestResponseProvider(null);
        System.clearProperty("leetcode.test.base.url");
    }

    @Test
    public void sendsFavoriteMutationWithFavoriteAndQuestionIds() {
        JSONObject request = requestGraphql("addQuestionToFavorite", builder -> builder
                .variables("favoriteIdHash", "favorite-list")
                .variables("questionId", "1"));

        assertEquals("addQuestionToFavorite", request.getString("operationName"));
        assertEquals("favorite-list", request.getJSONObject("variables").getString("favoriteIdHash"));
        assertEquals("1", request.getJSONObject("variables").getString("questionId"));
        assertTrue(request.getString("query").contains("mutation addQuestionToFavorite"));
    }

    @Test
    public void sendsNotePullAndPushOperationsWithExpectedFields() {
        JSONObject pullRequest = requestGraphql("getNote", builder -> builder.variables("titleSlug", "two-sum"));
        assertEquals("two-sum", pullRequest.getJSONObject("variables").getString("titleSlug"));
        assertTrue(pullRequest.getString("query").contains("query getNote"));

        JSONObject pushRequest = requestGraphql("updateNote", builder -> builder
                .variables("titleSlug", "two-sum")
                .variables("content", "# My note"));
        assertEquals("# My note", pushRequest.getJSONObject("variables").getString("content"));
        assertTrue(pushRequest.getString("query").contains("mutation updateNote"));
    }

    @Test
    public void sendsSubmissionHistoryAndDetailOperationsWithExpectedArguments() {
        JSONObject listRequest = requestGraphql("submissions", builder -> builder
                .variables("offset", 0)
                .variables("limit", 100)
                .variables("questionSlug", "two-sum"));
        assertEquals(100, listRequest.getJSONObject("variables").getIntValue("limit"));
        assertEquals("two-sum", listRequest.getJSONObject("variables").getString("questionSlug"));

        JSONObject detailRequest = requestGraphql("submissionDetail", "submissionDetails",
                builder -> builder.variables("id", "123"));
        assertEquals("submissionDetails", detailRequest.getString("operationName"));
        assertEquals("123", detailRequest.getJSONObject("variables").getString("id"));
        assertTrue(detailRequest.getString("query").contains("query submissionDetails"));
    }

    @Test
    public void keepsVariablesStronglyTypedAndInInsertionOrder() {
        Map<String, Object> variables = Graphql.builder()
                .operationName("getNote")
                .variables("titleSlug", "two-sum")
                .variables("includeContent", true)
                .build()
                .getVariables();

        assertEquals("two-sum", variables.get("titleSlug"));
        assertEquals(true, variables.get("includeContent"));
        assertEquals("[titleSlug, includeContent]", variables.keySet().toString());
    }

    private static JSONObject requestGraphql(String operationName, GraphqlBuilderCustomizer customizer) {
        return executeRequest(Graphql.builder().operationName(operationName), customizer);
    }

    private static JSONObject requestGraphql(String operationName, String operationNameAlias,
                                             GraphqlBuilderCustomizer customizer) {
        return executeRequest(Graphql.builder().operationName(operationName, operationNameAlias), customizer);
    }

    private static JSONObject executeRequest(Graphql.GraphqlBuilder builder,
                                             GraphqlBuilderCustomizer customizer) {
        AtomicReference<HttpRequest> capturedRequest = new AtomicReference<>();
        HttpRequestUtils.setTestResponseProvider(request -> {
            capturedRequest.set(request);
            HttpResponse response = new HttpResponse();
            response.setStatusCode(200);
            response.setBody("{\"data\":{}}");
            return response;
        });

        customizer.customize(builder);
        builder.request();

        return JSONObject.parseObject(capturedRequest.get().getBody());
    }

    @FunctionalInterface
    private interface GraphqlBuilderCustomizer {
        void customize(Graphql.GraphqlBuilder builder);
    }
}
