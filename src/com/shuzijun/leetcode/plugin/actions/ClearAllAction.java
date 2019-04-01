package com.shuzijun.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.LogUtils;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;

import java.io.File;

/**
 * @author shuzijun
 */
public class ClearAllAction extends AbstractAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config) {

        String filePath = PersistentConfig.getInstance().getTempFilePath();

        File file = new File(filePath);
        if (!file.exists() || !file.isDirectory()) {
            MessageUtils.showInfoMsg("info", PropertiesUtils.getInfo("clear.success"));
            return;
        }

        try {
            String[] tempList = file.list();
            File temp = null;
            for (int i = 0; i < tempList.length; i++) {
                if (filePath.endsWith(File.separator)) {
                    temp = new File(filePath + tempList[i]);
                } else {
                    temp = new File(filePath + File.separator + tempList[i]);
                }
                if (temp.isFile()) {
                    temp.delete();
                }
            }

            MessageUtils.showInfoMsg("info", PropertiesUtils.getInfo("clear.success"));
        } catch (Exception ee) {
            LogUtils.LOG.error("清理文件错误", ee);
            MessageUtils.showErrorMsg("error", PropertiesUtils.getInfo("clear.failed"));
        }

    }
}
