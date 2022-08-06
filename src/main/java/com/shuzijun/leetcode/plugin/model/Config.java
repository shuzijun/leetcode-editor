package com.shuzijun.leetcode.plugin.model;

import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.Transient;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shuzijun
 */
public class Config implements Cloneable {

    private Integer version;

    private String pluginVersion;

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
    @Transient
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
    private Boolean update = true;

    /**
     * 使用代理
     */
    private Boolean proxy = false;

    /**
     * 自定义代码生成
     */
    private Boolean customCode = false;
    /**
     * 适应英文描述
     */
    private Boolean englishContent = false;

    /**
     * 自定义文件名
     */
    private String customFileName = Constant.CUSTOM_FILE_NAME;
    /**
     * 自定义代码
     */
    private String customTemplate = Constant.CUSTOM_TEMPLATE;
    /**
     * 用户cookie
     */
    private Map<String, String> userCookie = new HashMap<>();

    /**
     * 题目颜色
     */
    private String levelColour = Constant.LEVEL_COLOUR;

    /**
     * 使用cookie登录
     */
    private boolean cookie = false;

    /**
     * question Split Editor
     */
    private String questionEditor = "Left";

    /**
     * Content  Multiline Comment
     */
    private Boolean multilineComment = false;

    /**
     * html Content
     */
    private Boolean htmlContent = false;

    /**
     * showTopics
     */
    private Boolean showTopics = true;

    /**
     * showToolIcon
     */
    private Boolean showToolIcon = true;

    /**
     * convergeEditor
     */
    private Boolean convergeEditor = true;

    /**
     * 使用的导航
     */
    private String navigatorName;

    /**
     * 显示编辑器标志
     */
    private boolean showQuestionEditorSign = true;

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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getCodeType() {
        return codeType;
    }

    public CodeTypeEnum getCodeTypeEnum(Project project) {
        CodeTypeEnum codeTypeEnum = CodeTypeEnum.getCodeTypeEnum(codeType);
        if (codeTypeEnum == null) {
            MessageUtils.getInstance(project).showWarnMsg("", PropertiesUtils.getInfo("config.code"));
        }
        return codeTypeEnum;
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

    public boolean getUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean getProxy() {
        return proxy;
    }

    public void setProxy(Boolean proxy) {
        this.proxy = proxy;
    }

    public Boolean getCustomCode() {
        return customCode;
    }

    public void setCustomCode(Boolean customCode) {
        this.customCode = customCode;
    }

    public String getCustomFileName() {
        if (!customCode) {
            return Constant.CUSTOM_FILE_NAME;
        } else {
            return customFileName;
        }
    }

    public void setCustomFileName(String customFileName) {
        this.customFileName = customFileName;
    }

    public String getCustomTemplate() {
        if (!customCode) {
            return Constant.CUSTOM_TEMPLATE;
        } else {
            return customTemplate;
        }
    }

    public void setCustomTemplate(String customTemplate) {
        this.customTemplate = customTemplate;
    }

    public Map<String, String> getUserCookie() {
        return userCookie;
    }

    public void setUserCookie(Map<String, String> userCookie) {
        this.userCookie = userCookie;
    }

    public String getAlias() {
        if ("leetcode.com".equals(getUrl())) {
            return "en";
        } else {
            return "cn";
        }
    }

    public void addCookie(String user, String cookie) {
        userCookie.put(user, cookie);
    }

    public String getCookie(String user) {
        if (userCookie == null) {
            return null;
        }
        return userCookie.get(user);
    }

    public String getLevelColour() {
        return levelColour;
    }

    @Transient
    public Color[] getFormatLevelColour() {
        Color[] formatColors = new Color[3];
        formatColors[0] = new Color(92, 184, 92);
        formatColors[1] = new Color(240, 173, 78);
        formatColors[2] = new Color(217, 83, 79);
        String[] colors = getLevelColour().split(";");
        if (colors.length > 0) {
            try {
                formatColors[0] = new Color(Integer.parseInt(colors[0].replace("#", ""), 16));
            } catch (Exception ignore) {
            }
        }
        if (colors.length > 1) {
            try {
                formatColors[1] = new Color(Integer.parseInt(colors[1].replace("#", ""), 16));
            } catch (Exception ignore) {
            }
        }
        if (colors.length > 2) {
            try {
                formatColors[2] = new Color(Integer.parseInt(colors[2].replace("#", ""), 16));
            } catch (Exception ignore) {
            }
        }
        return formatColors;
    }

    public void setLevelColour(String levelColour) {
        if (levelColour == null || levelColour.isEmpty()) {
            this.levelColour = Constant.LEVEL_COLOUR;
        } else {
            this.levelColour = levelColour;
        }
    }

    @Transient
    public void setFormatLevelColour(Color... colors) {
        StringBuilder levelColour = new StringBuilder();
        if (colors != null && colors.length > 0) {
            for (Color color : colors) {
                String R = Integer.toHexString(color.getRed());
                R = R.length() < 2 ? ('0' + R) : R;
                String G = Integer.toHexString(color.getGreen());
                G = G.length() < 2 ? ('0' + G) : G;
                String B = Integer.toHexString(color.getBlue());
                B = B.length() < 2 ? ('0' + B) : B;

                levelColour.append('#').append(R).append(G).append(B).append(";");
            }
        }
        this.levelColour = levelColour.toString();
    }

    public Boolean getEnglishContent() {
        return englishContent;
    }

    public void setEnglishContent(Boolean englishContent) {
        this.englishContent = englishContent;
    }

    public boolean isCookie() {
        return cookie;
    }

    public void setCookie(boolean cookie) {
        this.cookie = cookie;
    }

    @Transient
    public Boolean isShowQuestionEditor() {
        return !"Disable".equals(questionEditor) && !"false".equals(questionEditor);
    }

    @Transient
    public Boolean isLeftQuestionEditor() {
        return "Left".equals(questionEditor) || "true".equals(questionEditor) || !isShowQuestionEditor();
    }

    public String getQuestionEditor() {
        return questionEditor;
    }

    public void setQuestionEditor(String questionEditor) {
        this.questionEditor = questionEditor;
    }

    public Boolean getMultilineComment() {
        return multilineComment;
    }

    public void setMultilineComment(Boolean multilineComment) {
        this.multilineComment = multilineComment;
    }

    public Boolean getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(Boolean htmlContent) {
        this.htmlContent = htmlContent;
    }

    public Boolean getShowTopics() {
        return showTopics;
    }

    public void setShowTopics(Boolean showTopics) {
        this.showTopics = showTopics;
    }

    public Boolean getShowToolIcon() {
        return showToolIcon;
    }

    public void setShowToolIcon(Boolean showToolIcon) {
        this.showToolIcon = showToolIcon;
    }

    public Boolean getConvergeEditor() {
        return convergeEditor;
    }

    public void setConvergeEditor(Boolean convergeEditor) {
        this.convergeEditor = convergeEditor;
    }

    public String getNavigatorName() {
        return navigatorName;
    }

    public void setNavigatorName(String navigatorName) {
        this.navigatorName = navigatorName;
    }

    public boolean isShowQuestionEditorSign() {
        return showQuestionEditorSign;
    }

    public void setShowQuestionEditorSign(boolean showQuestionEditorSign) {
        this.showQuestionEditorSign = showQuestionEditorSign;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public void setPluginVersion(String pluginVersion) {
        this.pluginVersion = pluginVersion;
    }

    public boolean isModified(Config config) {

        if (config == null) {
            return false;
        }

        return new EqualsBuilder()
                .append(showQuestionEditorSign, config.showQuestionEditorSign)
                .append(loginName, config.loginName)
                .append(filePath, config.filePath)
                .append(codeType, config.codeType)
                .append(url, config.url)
                .append(update, config.update)
                .append(proxy, config.proxy)
                .append(customCode, config.customCode)
                .append(englishContent, config.englishContent)
                .append(customFileName, config.customFileName)
                .append(customTemplate, config.customTemplate)
                .append(levelColour, config.levelColour)
                .append(cookie, config.cookie)
                .append(questionEditor, config.questionEditor)
                .append(multilineComment, config.multilineComment)
                .append(htmlContent, config.htmlContent)
                .append(showTopics, config.showTopics)
                .append(showToolIcon, config.showToolIcon)
                .append(convergeEditor, config.convergeEditor)
                .isEquals();
    }


    @Override
    public Config clone() {
        Config config = null;
        try {
            config = (Config) super.clone();
        } catch (CloneNotSupportedException ignore) {
        }
        return config;
    }
}
