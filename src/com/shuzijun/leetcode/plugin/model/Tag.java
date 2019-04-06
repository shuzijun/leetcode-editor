package com.shuzijun.leetcode.plugin.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shuzijun
 */
public class Tag {

    private String slug;
    private String name;
    private String type;
    private List<Integer> questions = new ArrayList<Integer>();

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

    public List<Integer> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Integer> questions) {
        this.questions = questions;
    }

    public void addQuestion(Integer questionId) {
        questions.add(questionId);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
