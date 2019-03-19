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

    public Question(String title) {
        this.title = title;
    }

    public String getTitle() {
        StringBuffer sb = new StringBuffer();
        if (StringUtils.isNotBlank(questionId)) {
            sb.append("[").append(questionId).append("]");
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

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();


        if ("notac".equals(status)) {
            sb.append("❓");
        } else if ("ac".equals(status)) {
            sb.append("✔");
        } else if(leaf){
            sb.append("   ");
        }

        if (StringUtils.isNotBlank(questionId)) {
            sb.append("[").append(questionId).append("]");
        }
        return sb.append(title).toString();


    }
}
