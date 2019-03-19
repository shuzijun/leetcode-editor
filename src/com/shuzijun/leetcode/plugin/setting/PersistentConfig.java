package com.shuzijun.leetcode.plugin.setting;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.shuzijun.leetcode.plugin.model.Config;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shuzijun
 */
@State(name = "PersistentConfig", storages = {@Storage("leetcode-config.xml")})
public class PersistentConfig implements PersistentStateComponent<PersistentConfig> {

    public static String PATH = "leetcode-plugin";
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

    public void setInitConfig(Config config) {
        initConfig.put(INITNAME, config);
    }

    public String getTempFilePath() {
        return initConfig.get(INITNAME).getFilePath() + File.separator + PATH + File.separator;
    }

}
