package com.shuzijun.leetcode.plugin.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shuzijun
 */
public class Config {

    private Integer version;

    private String id;

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

    /**
     * 检查更新
     */
    private Boolean updata = true;

    private List<String> favoriteList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public boolean getUpdata() {
        return updata;
    }

    public void setUpdata(boolean updata) {
        this.updata = updata;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public List<String> getFavoriteList() {
        if(favoriteList == null || favoriteList.isEmpty()){
            favoriteList = new ArrayList<>();
            favoriteList.add("Favorite");
        }
        return favoriteList;
    }

    public void setFavoriteList(List<String> favoriteList) {
        this.favoriteList = favoriteList;
    }

    public String getAlias() {
        if ("leetcode.com".equals(getUrl())) {
            return "en";
        } else {
            return "cn";
        }
    }
}
