package com.shuzijun.leetcode.plugin.actions.toolbar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.shuzijun.leetcode.plugin.actions.AbstractAction;
import com.shuzijun.leetcode.plugin.manager.NavigatorAction;
import com.shuzijun.leetcode.plugin.manager.SessionManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Session;
import com.shuzijun.leetcode.plugin.setting.StatisticsData;
import com.shuzijun.leetcode.plugin.utils.DataKeys;
import com.shuzijun.leetcode.plugin.utils.HttpRequestUtils;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.window.ProgressPanel;
import com.shuzijun.leetcode.plugin.window.WindowFactory;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author shuzijun
 */
public class ProgressAction extends AbstractAction implements DumbAware {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent, Config config) {
        if (!HttpRequestUtils.isLogin(anActionEvent.getProject())) {
            MessageUtils.getInstance(anActionEvent.getProject()).showWarnMsg("info", PropertiesUtils.getInfo("login.not"));
            return;
        }

        List<Session> sessionList = SessionManager.getSession(anActionEvent.getProject());
        if (sessionList.isEmpty()) {
            return;
        }
        AtomicReference<Session> sessionAtomic = new AtomicReference<>();
        ApplicationManager.getApplication().invokeAndWait(() -> {
            ProgressDialogPanel dialogPanel = new ProgressDialogPanel(anActionEvent.getProject(), sessionList);
            dialogPanel.setTitle("Progress");
            if (dialogPanel.showAndGet()) {
                sessionAtomic.set(dialogPanel.select());
            }
        });
        if (sessionAtomic.get() != null) {
            Session session = sessionAtomic.get();
            if (session.getId() == null) {
                return;
            } else {
                if (SessionManager.switchSession(anActionEvent.getProject(), session.getId())) {
                    NavigatorAction navigatorAction = WindowFactory.getDataContext(anActionEvent.getProject()).getData(DataKeys.LEETCODE_PROJECTS_NAVIGATORACTION);
                    navigatorAction.getFind().operationType("");
                    navigatorAction.findClear();
                    StatisticsData.refresh(anActionEvent.getProject());
                    actionPerformed(anActionEvent, config);
                }
            }
        }
    }


    private class ProgressDialogPanel extends DialogWrapper {

        private JPanel jpanel;
        private ProgressPanel progressPanel;

        public ProgressDialogPanel(@Nullable Project project, List<Session> sessionList) {
            super(project, true);
            progressPanel = new ProgressPanel(sessionList, project);
            jpanel = progressPanel.getPanel();
            setModal(true);
            init();
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            return jpanel;
        }

        public Session select() {
            return progressPanel.select();
        }


    }
}
