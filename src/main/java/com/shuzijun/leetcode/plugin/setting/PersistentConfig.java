package com.shuzijun.leetcode.plugin.setting;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author shuzijun
 */
@State(name = "PersistentConfig" + PluginConstant.ACTION_SUFFIX, storages = {@Storage(value = PluginConstant.ACTION_PREFIX + "-config.xml", roamingType = RoamingType.DISABLED)})
public class PersistentConfig implements PersistentStateComponent<PersistentConfig> {

    public static String PATH = "leetcode" + File.separator + "editor";
    public static String OLDPATH = "leetcode-plugin";
    private static String INITNAME = "initConfig";

    private Map<String, Config> initConfig = new HashMap<>();


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


    @Nullable
    public Config getInitConfig() {
        Config config = initConfig.get(INITNAME);
        if (config != null && config.getVersion() != null && config.getVersion() < Constant.PLUGIN_CONFIG_VERSION_3) {
            if (URLUtils.leetcodecnOld.equals(config.getUrl())) {
                config.setUrl(URLUtils.leetcodecn);
            }
            Iterator<String> iterator = config.getUserCookie().keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                String value = config.getCookie(key);
                if (StringUtils.isBlank(value) || key.startsWith(URLUtils.leetcodecnOld)) {
                    iterator.remove();
                }
            }
            config.setVersion(Constant.PLUGIN_CONFIG_VERSION_3);
            setInitConfig(config);
        }
        return config;
    }

    @NotNull
    public Config getConfig() {
        Config config = getInitConfig();
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
        LocalDate now = LocalDate.now();
        List<String> paths = new ArrayList<>(Arrays.asList(getConfig().getFilePath(), PATH, initConfig.get(INITNAME).getAlias()));
        StringBuilder sb = new StringBuilder();
        paths.add(DateTimeFormatter.ofPattern("_yyyyMMdd").format(now));
        for (String path : paths) {
            sb.append(path).append(File.separator);
        }
        return sb.toString();
    }

    public void savePassword(String password, String username) {
        if (username == null || password == null) {
            return;
        }
        PasswordSafe.getInstance().set(new CredentialAttributes(PluginConstant.PLUGIN_ID, username, this.getClass()), new Credentials(username, password == null ? "" : password));
    }

    public String getPassword(String username) {
        if (getConfig().getVersion() != null && username != null) {
            return PasswordSafe.getInstance().getPassword(new CredentialAttributes(PluginConstant.PLUGIN_ID, username, this.getClass()));
        }
        return null;

    }

}
