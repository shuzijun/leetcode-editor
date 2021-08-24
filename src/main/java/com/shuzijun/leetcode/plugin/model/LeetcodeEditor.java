package com.shuzijun.leetcode.plugin.model;

/**
 * @author shuzijun
 */
public class LeetcodeEditor {

    private Integer version = 1;

    /**
     * file path
     */
    private String path;

    /**
     * frontendQuestionId
     */
    private String frontendQuestionId;


    /**
     * content file path
     */
    private String contentPath;

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFrontendQuestionId() {
        return frontendQuestionId;
    }

    public void setFrontendQuestionId(String frontendQuestionId) {
        this.frontendQuestionId = frontendQuestionId;
    }

    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

}
