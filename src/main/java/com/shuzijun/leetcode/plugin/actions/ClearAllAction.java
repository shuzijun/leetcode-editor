package com.shuzijun.leetcode.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBPanel;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.LogUtils;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * @author shuzijun
 */
public class ClearAllAction extends AbstractAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config) {

        ClearAllWarningPanel dialog = new ClearAllWarningPanel(anActionEvent.getProject());
        dialog.setTitle("Clear All");

        if (dialog.showAndGet()) {
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

    private class ClearAllWarningPanel extends DialogWrapper {

        private JPanel jpanel;

        public ClearAllWarningPanel(@Nullable Project project) {
            super(project, true);
            jpanel = new JBPanel();
            jpanel.add(new JLabel("Clear All File？"));
            jpanel.setMinimumSize(new Dimension(200, 100));
            setModal(true);
            init();
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            return jpanel;
        }
    }
}
