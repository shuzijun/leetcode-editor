package com.shuzijun.leetcode.plugin.timer;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author shuzijun
 */
public class TimerBarWidget implements CustomStatusBarWidget {

    public final static String ID = PluginConstant.LEETCODE_TIMER_BAR_WIDGET;

    private Long second = 0L;
    private String name = "";
    private Project project;


    private static Color Level1 = new Color(92, 184, 92);
    private static Color Level2 = new Color(240, 173, 78);
    private static Color Level3 = new Color(217, 83, 79);

    public TimerBarWidget(Project project) {
        this.project = project;
        loaColor();
    }
    public static void loaColor(){
        Config config = PersistentConfig.getInstance().getInitConfig();
        if (config != null) {
            Color[] colors = config.getFormatLevelColour();
            Level1 = colors[0];
            Level2 = colors[1];
            Level3 = colors[2];
        }
    }

    private JLabel label = new JLabel(time());

    private Timer timer = new Timer(1000, new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            second += 1;
            if (second < 30 * 60) {
                label.setForeground(Level1);
            } else if (second < 60 * 60) {
                label.setForeground(Level2);
            } else {
                label.setForeground(Level3);
            }
            label.setText(time());
        }
    });

    private String time() {
        return String.format("[%s]%02d:%02d:%02d", name, second / 60 / 60, second / 60 % 60, second % 60);
    }

    @Override
    public JComponent getComponent() {
        return label;
    }

    @NotNull
    @Override
    public String ID() {
        return ID;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        label.setVisible(false);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (timer.isRunning()) {
                    timer.stop();
                } else {
                    timer.start();
                }
            }
        });
        Config config = PersistentConfig.getInstance().getInitConfig();
        if (config != null) {
            if (StringUtils.isNotBlank(config.getLevelColour())) {
                String[] colors = config.getLevelColour().split(";");
                if (colors.length > 0) {
                    try {
                        Level1 = new Color(Integer.parseInt(colors[0].replace("#",""), 16));
                    } catch (Exception ignore) {
                    }
                }
                if (colors.length > 1) {
                    try {
                        Level2 = new Color(Integer.parseInt(colors[1].replace("#",""), 16));
                    } catch (Exception ignore) {
                    }
                }
                if (colors.length > 2) {
                    try {
                        Level3 = new Color(Integer.parseInt(colors[2].replace("#",""), 16));
                    } catch (Exception ignore) {
                    }
                }
            }
        }
    }

    @Override
    public void dispose() {
        timer.stop();
    }

    public void startTimer(String name) {
        if (!this.name.equals(name)) {
            this.name = name;
            this.second = 0L;
        }
        timer.start();
        if (!label.isVisible()) {
            label.setVisible(true);
        }

    }

    public void stopTimer() {
        timer.stop();
    }

    public void reset() {
        this.name = "";
        this.second = 0L;
        timer.stop();
        label.setVisible(false);
    }


}
