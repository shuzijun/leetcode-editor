package com.shuzijun.leetcode.plugin.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CompanyUtils {
    private final static Logger logger = LoggerFactory.getLogger(CompanyUtils.class);

    public static final String COMPANY_SOURCE_TYPE_BUILD_IN = "Build-in";
    public static final String COMPANY_SOURCE_TYPE_URL = "URL";
    public static final String COMPANY_SOURCE_TYPE_FILE = "File";

    private static final Lock LOCK = new ReentrantLock();
    private static volatile Map<String, Set<String>> COMPANY_QUESTION_MAP = null;

    public static Map<String, Set<String>> getCompanyQuestionMap() {
        refreshCompanies();
        return COMPANY_QUESTION_MAP;
    }

    public static void refreshCompanies() {
        if (!LOCK.tryLock()) {
            return;
        }
        Config config = PersistentConfig.getInstance().getConfig();
        Map<String, Set<String>> tempMap = null;
        try {
            if (COMPANY_SOURCE_TYPE_URL.equals(config.getCompanySourceType())) {
                tempMap = loadUrlCompanies(config.getCompanySourceUrl());
            } else {
                tempMap = loadBuildInCompanies();
            }
        } catch (Exception e) {
            tempMap = new TreeMap<>();

            logger.error("Load companies error, type: {}, url: {}", config.getCompanySourceType(), config.getCompanySourceUrl(), e);
        } finally {
            COMPANY_QUESTION_MAP = Optional.ofNullable(tempMap).orElse(new HashMap<>());

            LOCK.unlock();
        }
    }

    private static Map<String, Set<String>> loadBuildInCompanies() throws IOException {
        String companyJson = IOUtils.toString(CompanyUtils.class.getClassLoader().getResourceAsStream("company.json"));
        return JSON.parseObject(companyJson, new TypeReference<Map<String, Set<String>>>() {
        });
    }

    private static Map<String, Set<String>> loadUrlCompanies(String companySourceUrl) {
        HttpRequest request = HttpRequest.get(companySourceUrl);
        HttpResponse response = HttpRequestUtils.executeGet(request);
        if (response.getStatusCode() != 200 || StringUtils.isEmpty(response.getBody())) {
            throw new RuntimeException("load url companies error, url: " + companySourceUrl + ", response: " + response);
        }
        return JSON.parseObject(response.getBody(), new TypeReference<Map<String, Set<String>>>() {
        });
    }

    public static void main(String[] args) throws Exception {
        String companyJson = IOUtils.toString(CompanyUtils.class.getClassLoader().getResourceAsStream("company.json"));
        Map<String, TreeSet<Integer>> companyMap = JSON.parseObject(companyJson, new TypeReference<TreeMap<String, TreeSet<Integer>>>(){});
        System.out.println(companyMap);
    }
}
