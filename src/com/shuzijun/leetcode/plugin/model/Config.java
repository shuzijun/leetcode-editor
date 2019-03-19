package com.shuzijun.leetcode.plugin.model;

/**
 * @author shuzijun
 */
public class Config {

    /**
     * 配置名称
     */
    private String name;

    /**
     * 用户名
     */
    private String loginName;

    /**
     * 密码
     */
    private String password;

    /**
     * 临时文件路径
     */
    private String filePath;

    /**
     * 语言
     */
    private String codeType;

    /**
     * url
     */
    private String url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getCodeType() {
        return codeType;
    }

    public void setCodeType(String codeType) {
        this.codeType = codeType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
