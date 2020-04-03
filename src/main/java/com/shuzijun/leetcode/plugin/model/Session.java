package com.shuzijun.leetcode.plugin.model;

/**
 * @author shuzijun
 */
public class Session {

    /**
     * id
     */
    private Integer id;

    /**
     * 名称
     */
    private String name;

    /**
     * 解决
     */
    private Integer solvedTotal;

    /**
     * 总数
     */
    private Integer questionTotal;

    /**
     * 尝试
     */
    private Integer attempted;

    /**
     * 待解决
     */
    private Integer unsolved;

    /**
     * 经验
     */
    private Integer XP;

    /**
     * 币
     */
    private Integer point;

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


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().length() == 0) {
            this.name = "Anonymous Session";
        } else {
            this.name = name;
        }
    }

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

    public Integer getAttempted() {
        return attempted;
    }

    public void setAttempted(Integer attempted) {
        this.attempted = attempted;
    }

    public Integer getUnsolved() {
        return unsolved;
    }

    public void setUnsolved(Integer unsolved) {
        this.unsolved = unsolved;
    }

    public Integer getXP() {
        return XP;
    }

    public void setXP(Integer XP) {
        this.XP = XP;
    }

    public Integer getPoint() {
        return point;
    }

    public void setPoint(Integer point) {
        this.point = point;
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

    @Override
    public String toString() {
        return name;
    }
}
