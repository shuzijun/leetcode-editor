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
    private String frontendQuestionId;

    public Question(String title) {
        this.title = title;
    }

    public Question(String title, String nodeType) {
        this.title = title;
        this.nodeType = nodeType;
    }

    public String getTitle() {
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

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();


        if ("notac".equals(status)) {
            sb.append("❓");
        } else if ("ac".equals(status)) {
            sb.append("✔");
        } else if ("lock".equals(status)) {
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
