package com.shuzijun.leetcode.plugin.model;

/**
 * @author shuzijun
 */
public class Solution {

    /**
     * 标题
     */
    private String title;
    /**
     * 标识
     */
    private String slug;
    /**
     * 标签
     */
    private String tags;
    /**
     * 概况
     */
    private String summary;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
