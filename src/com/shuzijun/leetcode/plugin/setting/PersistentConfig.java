package com.shuzijun.leetcode.plugin.setting;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.components.*;
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

    public Config getConfig() {
        Config config = initConfig.get(INITNAME);
        if (config == null) {
            MessageUtils.showWarnMsg("warning", PropertiesUtils.getInfo("config.first"));
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

    public void savePassword(String password){
        CredentialAttributes credentialAttributes = createCredentialAttributes(); // see previous sample
        Credentials credentials = new Credentials("leetcode-editor", password);
        PasswordSafe.getInstance().set(credentialAttributes, credentials);
    }

    public String getPassword(String password) {
        if (getConfig().getVersion() != null) {
            return  PasswordSafe.getInstance().getPassword(createCredentialAttributes());
        } else {
            return password;
        }

    }

    private CredentialAttributes createCredentialAttributes() {
        return new CredentialAttributes(CredentialAttributesKt.generateServiceName("leetcode-editor", "leetcode-editor"));
    }
}
