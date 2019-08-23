package com.shuzijun.leetcode.plugin.listener;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.shuzijun.leetcode.plugin.utils.LogUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * @author shuzijun
 */
public class DonateListener implements ActionListener {
    private JCheckBox jcb;

    public DonateListener(JCheckBox jcb) {
        this.jcb = jcb;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (jcb.isSelected()) {
            Project project = ProjectManager.getInstance().getDefaultProject();
            DonateListener.DonatePanel dialog = new DonateListener.DonatePanel(project);
            dialog.setTitle(PropertiesUtils.getInfo("donate.info"));
            dialog.showAndGet();
        }
    }

    private class DonatePanel extends DialogWrapper {

        private JPanel jpanel;

        public DonatePanel(@Nullable Project project) {
            super(project, true);
            jpanel = new JPanel();
            try {
                jpanel.add(new JLabel(new ImageIcon(ImageIO.read(this.getClass().getClassLoader().getResourceAsStream("image/WeChat.png")))));
                jpanel.add(new JLabel(new ImageIcon(ImageIO.read(this.getClass().getClassLoader().getResourceAsStream("image/AliPay.png")))));
            } catch (IOException e) {
               LogUtils.LOG.error("加载图片失败",e);
            }
            jpanel.setMinimumSize(new Dimension(400, 200));
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
