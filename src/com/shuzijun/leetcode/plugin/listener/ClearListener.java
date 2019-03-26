package com.shuzijun.leetcode.plugin.listener;

import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.ToolWindow;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * @author shuzijun
 */
public class ClearListener implements ActionListener {

    private final static Logger logger = LoggerFactory.getLogger(ClearListener.class);
    private ToolWindow toolWindow;

    public ClearListener(ToolWindow toolWindow) {
        this.toolWindow = toolWindow;
    }

    @Override

    public void actionPerformed(ActionEvent e) {

        String filePath = PersistentConfig.getInstance().getTempFilePath();

        File file = new File(filePath);
        if (!file.exists() || !file.isDirectory()) {
            MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.INFO, "info", PropertiesUtils.getInfo("clear.success"));
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

            MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.INFO, "info", PropertiesUtils.getInfo("clear.success"));
        } catch (Exception ee) {
            logger.error("清理文件错误", ee);
            MessageUtils.showMsg(toolWindow.getContentManager().getComponent(), MessageType.ERROR, "error", PropertiesUtils.getInfo("clear.failed"));
        }

    }

}
