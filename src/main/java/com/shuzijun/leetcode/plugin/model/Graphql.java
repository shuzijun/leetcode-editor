package com.shuzijun.leetcode.plugin.model;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.util.io.FileUtilRt;
import com.shuzijun.leetcode.plugin.utils.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author shuzijun
 */
public class Graphql {

    private String operationName;

    private Map variables;

    private String query;

    private Graphql(String operationName, Map variables, String query) {
        this.operationName = operationName;
        this.variables = variables;
        this.query = query;
    }

    public String getOperationName() {
        return operationName;
    }

    public Map getVariables() {
        return variables;
    }

    public String getQuery() {
        return query;
    }

    public String generate() {
        return JSONObject.toJSONString(this);
    }

    public static GraphqlBuilder builder() {
        return new GraphqlBuilder();
    }

    public static class GraphqlBuilder {

        private static final String PATH = "/graphql/";

        private static final String CNSUFFIX = "_cn.graphql";

        private String url = URLUtils.getLeetcodeGraphql();

        private String operationName;

        private Map<String, Object> variables = new HashMap<>();

        private String query;

        private String suffix = ".graphql";

        private boolean cache = false;

        private String cacheParam;

        private GraphqlBuilder() {

        }

        public GraphqlBuilder url(String url) {
            this.url = url;
            return this;
        }

        public GraphqlBuilder cn(boolean isCn) {
            if (isCn) {
                this.suffix = CNSUFFIX;
            }
            return this;
        }

        public GraphqlBuilder operationName(String operationName) {
            this.operationName = operationName;
            this.query(operationName);
            return this;
        }

        public GraphqlBuilder operationName(String operationName, String operationNameAlias) {
            this.operationName = operationNameAlias;
            this.query(operationName);
            return this;
        }

        private GraphqlBuilder query(String operationName) {
            try (InputStream inputStream = GraphqlBuilder.class.getResourceAsStream(PATH + operationName + suffix)) {
                if (inputStream == null) {
                    LogUtils.LOG.error(PATH + operationName + suffix + " Path is empty");
                } else {

                    this.query = new String(inputStream.readAllBytes());
                }
            } catch (IOException e) {
                LogUtils.LOG.error(PATH + operationName + suffix + " Loading exception", e);
            }
            return this;
        }

        public GraphqlBuilder variables(String key, Object value) {
            variables.put(key, value);
            return this;
        }

        public GraphqlBuilder cache(boolean cache) {
            this.cache = cache;
            return this;
        }

        public GraphqlBuilder cacheParam(String cacheParam) {
            this.cacheParam = cacheParam;
            this.cache = true;
            return this;
        }

        public Graphql build() {
            return new Graphql(operationName, variables, query);
        }

        @NotNull
        public HttpResponse request() {
            return HttpRequest.builderPost(url, "application/json")
                    .body(build().generate())
                    .addHeader("Accept", "application/json")
                    .cache(cache).cacheParam(cacheParam).request();
        }
    }

    public static void main(String[] args) throws IOException {
        String path = "/Users/arronshentu/Downloads/public/leetcode-editor/doc/txt.json";
        Path path1 = Paths.get(path);
        byte[] bytes = Files.readAllBytes(path1);
        JSONObject jsonObject = JSONObject.parseObject(new String(bytes)).getJSONObject("data").getJSONObject("question");
        Question question = new Question();
        question.setQuestionId(jsonObject.getString("questionId"));
        question.setTestCase(jsonObject.getString("sampleTestCase"));
        question.setExampleTestcases(jsonObject.getString("exampleTestcases"));
        question.setStatus(jsonObject.get("status") == null ? "" : jsonObject.getString("status"));
        question.setTitle(jsonObject.getString("title"));

        JSONArray topicTags = jsonObject.getJSONArray("topicTags");
        for (int i = 0, n = topicTags.size(); i < n; i++) {
            Object topicTag = topicTags.get(i);
            if (topicTag instanceof JSONObject) {
                JSONObject tag = (JSONObject) topicTag;
                if (tag.getString("name") != null && "Design".equals(tag.getString("name"))) {
                    question.setDesign(true);
                }
            }
            if (i == n - 1 && Objects.equals(!question.isDesign(), true)) {
                question.setDesign(false);
            }
        }
        JSONObject metaData = jsonObject.getJSONObject("metaData");
        question.setFunctionName(metaData.getString("name"));
//        question.setParamTypes(metaData.getJSONArray("params").stream().map(t -> {
//            String type = ((JSONObject) t).getString("type");
//            type = typeMapping(type);
//            return type;
//        }).collect(Collectors.toList()));
//        question.setReturnType(typeMapping(metaData.getJSONObject("return").getString("type")));

        Graphql build = Graphql.builder().operationName("questionData").variables("titleSlug", "threeSum").build();
        String generate = build.generate();
        System.out.println(generate);
        LocalDate now = LocalDate.now();
        List<String> paths = new ArrayList<>(Arrays.asList("./", "./", "alias"));
        StringBuilder sb = new StringBuilder();
        paths.add(DateTimeFormatter.ofPattern("_yyyyMMdd").format(now));
        for (String p : paths) {
            sb.append(p).append(File.separator);
        }
        System.out.println(sb);
    }

}
