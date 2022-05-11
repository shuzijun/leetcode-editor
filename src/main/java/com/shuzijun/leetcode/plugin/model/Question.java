package com.shuzijun.leetcode.plugin.model;

import org.apache.commons.lang.StringUtils;

/**
 * @author shuzijun
 */
public class Question {

    private String title;
    private String questionId;
    private String questionTypename;
    private String typeName;
    private Integer level;
    private String status;
    private String titleSlug;
    private boolean leaf = Boolean.FALSE;
    private String testCase;
    private String langSlug;
    private String nodeType = Constant.NODETYPE_DEF;
    /**
     * 页面的题目编号
     */
    private String frontendQuestionId;
    /**
     * 题目描述
     */
    private String content;

    /**
     * 题目代码
     */
    private String code;

    /**
     * 文章类型
     */
    private Integer articleLive;

    /**
     * 文章标识
     */
    private String articleSlug;


    public Question() {

    }

    public Question(String title) {
        this.title = title;
    }

    public Question(String title, String nodeType) {
        this.title = title;
        this.nodeType = nodeType;
    }

    public String getTitle() {
        return title;
    }

    public String getFormTitle() {
        StringBuffer sb = new StringBuffer();
        if (StringUtils.isNotBlank(frontendQuestionId)) {
            sb.append("[").append(frontendQuestionId).append("]");
        }
        return sb.append(title).toString();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getQuestionTypename() {
        return questionTypename;
    }

    public void setQuestionTypename(String questionTypename) {
        this.questionTypename = questionTypename;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public void setLeaf(boolean leaf) {
        this.leaf = leaf;
    }

    public String getTitleSlug() {
        return titleSlug;
    }

    public void setTitleSlug(String titleSlug) {
        this.titleSlug = titleSlug;
    }

    public String getTestCase() {
        return testCase;
    }

    public void setTestCase(String testCase) {
        this.testCase = testCase;
    }

    public String getLangSlug() {
        return langSlug;
    }

    public void setLangSlug(String langSlug) {
        this.langSlug = langSlug;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getFrontendQuestionId() {
        return frontendQuestionId;
    }

    public void setFrontendQuestionId(String frontendQuestionId) {
        this.frontendQuestionId = frontendQuestionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getArticleLive() {
        return articleLive;
    }

    public void setArticleLive(Integer articleLive) {
        this.articleLive = articleLive;
    }

    public String getArticleSlug() {
        return articleSlug;
    }

    public void setArticleSlug(String articleSlug) {
        this.articleSlug = articleSlug;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();


        if ("notac".equalsIgnoreCase(status)) {
            sb.append("❓");
        } else if ("ac".equalsIgnoreCase(status)) {
            sb.append("✔");
        } else if ("lock".equalsIgnoreCase(status)) {
            sb.append(" $ ");
        } else if (leaf && level != null) {
            sb.append("   ");
        }

        if (StringUtils.isNotBlank(frontendQuestionId) && leaf) {
            sb.append("[").append(frontendQuestionId).append("]");
        }
        return sb.append(title).toString();


    }
}
