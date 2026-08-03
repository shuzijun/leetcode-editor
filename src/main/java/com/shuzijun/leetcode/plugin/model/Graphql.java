package com.shuzijun.leetcode.plugin.model;

import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.util.io.FileUtilRt;
import com.shuzijun.leetcode.plugin.utils.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author shuzijun
 */
public class Graphql {

    private String operationName;

    private final Map<String, Object> variables;

    private final String query;

    private Graphql(String operationName, Map<String, Object> variables, String query) {
        this.operationName = operationName;
        this.variables = variables;
        this.query = query;
    }

    public String getOperationName() {
        return operationName;
    }

    public Map<String, Object> getVariables() {
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

        private final Map<String, Object> variables = new LinkedHashMap<>();

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
                    this.query = new String(FileUtilRt.loadBytes(inputStream), StandardCharsets.UTF_8);
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

}
