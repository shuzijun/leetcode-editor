package com.shuzijun.leetcode.plugin.listener;

import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.FileUtils;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.io.File;
import java.io.IOException;

/**
 * @author shuzijun
 */
public class UpdatePluginListener implements AncestorListener {
    @Override
    public void ancestorAdded(AncestorEvent event) {
        Config config = PersistentConfig.getInstance().getInitConfig();
        if (config == null) {
            return;
        } else {
            //如果存在历史版本，并且为2版本之前，则复制历史数据到新缓存目录下，并且更新版本号
            if (Constant.PLUGIN_CONFIG_VERSION_1.equals(config.getVersion()) || config.getVersion() == null) {
                File oldFile = new File(config.getFilePath() + File.separator + PersistentConfig.OLDPATH + File.separator);
                if (oldFile.getParentFile().exists()) {
                    File newFile = new File(config.getFilePath() + File.separator + PersistentConfig.PATH + File.separator);
                    if (!newFile.getParentFile().exists()) {
                        newFile.getParentFile().mkdirs();
                    }
                    try {
                        FileUtils.copyDirectory(oldFile, newFile);
                    } catch (IOException e) {

                    }
                }
                config.setVersion(Constant.PLUGIN_CONFIG_VERSION_2);
                PersistentConfig.getInstance().setInitConfig(config);
            }
        }

    }

    @Override
    public void ancestorRemoved(AncestorEvent event) {

    }

    @Override
    public void ancestorMoved(AncestorEvent event) {

    }
}
