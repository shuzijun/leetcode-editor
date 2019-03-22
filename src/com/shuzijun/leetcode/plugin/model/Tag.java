package com.shuzijun.leetcode.plugin.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author shuzijun
 */
public class Tag {

    private String slug;
    private String name;
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
}
