package com.shuzijun.leetcode.plugin.setting;

import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.ide.passwordSafe.PasswordSafeException;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shuzijun
 */
@State(name = "PersistentConfig", storages = {@Storage(value = "leetcode-config.xml", roamingType = RoamingType.DISABLED)})
public class PersistentConfig implements PersistentStateComponent<PersistentConfig> {

    public static String PATH = "leetcode" + File.separator + "editor";
    public static String OLDPATH = "leetcode-plugin";
    private static String INITNAME = "initConfig";

    private Map<String, Config> initConfig = new HashMap<>();


    @Nullable
    public static PersistentConfig getInstance() {
        return ServiceManager.getService(PersistentConfig.class);
    }

    @Nullable
    @Override
    public PersistentConfig getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull PersistentConfig persistentConfig) {
        XmlSerializerUtil.copyBean(persistentConfig, this);
    }

    public Config getInitConfig() {
        return initConfig.get(INITNAME);
    }

    public Config getConfig() {
        Config config = initConfig.get(INITNAME);
        if (config == null) {
            MessageUtils.showAllWarnMsg("warning", PropertiesUtils.getInfo("config.first"));
            throw new UnsupportedOperationException("not configured:File -> settings->tools->leetcode plugin");
        } else {
            return config;
        }

    }

    public void setInitConfig(Config config) {
        initConfig.put(INITNAME, config);
    }

    public String getTempFilePath() {
        return getConfig().getFilePath() + File.separator + PATH + File.separator + initConfig.get(INITNAME).getAlias() + File.separator;
    }

    public String getProblemCodeDirectory() {
        Config conf = getConfig();
        if (conf.getAddToProject()) {
            Project[] projects = ProjectManager.getInstance().getOpenProjects();
            if (projects.length > 0) {
                String projectBasePath = projects[0].getBasePath();
                String subDirectory = conf.getProblemSubDirectoryName();
                if (subDirectory.isEmpty() || subDirectory.equals("."))
                    return projectBasePath + File.separator;
                return projectBasePath + File.separator + subDirectory + File.separator;
            }
        }
        // fallback to temp directory
        return getTempFilePath();
    }

    public void savePassword(String password) {
        try {
            PasswordSafe.getInstance().storePassword
                    (null, this.getClass(), "leetcode-editor", password != null ? password : "");
        } catch (PasswordSafeException exception) {
            MessageUtils.showAllWarnMsg("warning", "Failed to save password");
        }
    }

    public String getPassword() {
        if (getConfig().getVersion() != null) {
            try {
                return PasswordSafe.getInstance().getPassword(null, this.getClass(), "leetcode-editor");
            } catch (PasswordSafeException exception) {
                MessageUtils.showAllWarnMsg("warning", "Password acquisition failed");
                return null;
            }

        }
        return null;

    }

}
