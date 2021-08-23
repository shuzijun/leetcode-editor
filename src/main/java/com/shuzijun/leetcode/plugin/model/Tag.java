package com.shuzijun.leetcode.plugin.model;

import org.apache.commons.lang.StringUtils;

import java.util.Comparator;
import java.util.TreeSet;

/**
 * @author shuzijun
 */
public class Tag {

    private String slug;
    private String name;
    private String type;
    private boolean isSelect = false;
    private TreeSet<String> questions = new TreeSet<String>(new Comparator<String>() {
        @Override
        public int compare(String arg0, String arg1) {
            if (StringUtils.isNumeric(arg0) && StringUtils.isNumeric(arg1)) {
                return Integer.valueOf(arg0).compareTo(Integer.valueOf(arg1));
            } else if (StringUtils.isNumeric(arg0)) {
                return  -1;
            } else if (StringUtils.isNumeric(arg1)) {
                return 1;
            } else {
                return arg0.compareTo(arg1);
            }
        }
    });

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

    public TreeSet<String> getQuestions() {
        return questions;
    }
    public void addQuestion(String questionId) {
        questions.add(questionId);
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
}
