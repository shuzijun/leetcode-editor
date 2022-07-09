package com.shuzijun.leetcode.plugin.model;

/**
 * @author shuzijun
 */
public class Statistics {

    /**
     * 解决
     */
    private Integer solvedTotal;

    /**
     * 总数
     */
    private Integer questionTotal;

    /**
     * 难度
     */
    private Integer easy;

    /**
     * 难度
     */
    private Integer medium;

    /**
     * 难度
     */
    private Integer hard;

    public Integer getSolvedTotal() {
        return solvedTotal;
    }

    public void setSolvedTotal(Integer solvedTotal) {
        this.solvedTotal = solvedTotal;
    }

    public Integer getQuestionTotal() {
        return questionTotal;
    }

    public void setQuestionTotal(Integer questionTotal) {
        this.questionTotal = questionTotal;
    }

    public Integer getEasy() {
        return easy;
    }

    public void setEasy(Integer easy) {
        this.easy = easy;
    }

    public Integer getMedium() {
        return medium;
    }

    public void setMedium(Integer medium) {
        this.medium = medium;
    }

    public Integer getHard() {
        return hard;
    }

    public void setHard(Integer hard) {
        this.hard = hard;
    }
}
