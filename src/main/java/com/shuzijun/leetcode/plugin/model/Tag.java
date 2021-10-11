package com.shuzijun.leetcode.plugin.model;

import java.util.HashSet;
import java.util.Set;

/**
 * @author shuzijun
 */
public class Tag {

    private String slug;
    private String name;
    private String type;
    private boolean isSelect = false;
    private Set<String> questions = new HashSet<>();

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public Set<String> getQuestions() {
        return questions;
    }

    public void addQuestion(String questionId) {
        questions.add(questionId);
    }
}
