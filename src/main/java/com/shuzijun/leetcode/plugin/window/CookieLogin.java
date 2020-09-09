package com.shuzijun.leetcode.plugin.window;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.shuzijun.leetcode.plugin.utils.HttpRequestUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shuzijun
 */
public class CookieLogin implements LoginFrame {
    private JTree tree;
    private Project project;


    public CookieLogin(Project project, JTree tree) {
        this.tree = tree;
        this.project = project;
    }

    @Override
    public void loadComponent() {
        CookiePanel cookiePanel = new CookiePanel(project);
        if (cookiePanel.showAndGet()) {
            String cookiesString = cookiePanel.cookieText();
            if (StringUtils.isBlank(cookiesString)) {
                JOptionPane.showMessageDialog(null, "cookie is null");
                return;
            }
            final List<HttpCookie> cookieList = new ArrayList<>();
            String[] cookies = cookiesString.split(";");
            for (String cookieString : cookies) {
                String[] cookie = cookieString.trim().split("=");
                if (cookie.length >= 2) {
                    try {
                        HttpCookie basicClientCookie = new HttpCookie(cookie[0], cookie[1]);
                        basicClientCookie.setDomain("." + URLUtils.getLeetcodeHost());
                        basicClientCookie.setPath("/");
                        cookieList.add(basicClientCookie);
                    } catch (IllegalArgumentException ignore) {

                    }
                }
            }
            HttpRequestUtils.setCookie(cookieList);

            ProgressManager.getInstance().run(new Task.Backgroundable(project, "leetcode.loginSuccess", false) {
                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    if (HttpRequestUtils.isLogin()) {
                        HttpLogin.loginSuccess(tree, project, cookieList);
                    } else {
                        JOptionPane.showMessageDialog(null, PropertiesUtils.getInfo("login.failed"));
                    }

                }
            });
        }
    }

    class CookiePanel extends DialogWrapper {

        private JPanel jpanel;
        private JTextArea caseText;

        public CookiePanel(Project project) {
            super(project, Boolean.TRUE);

            jpanel = new JBPanel();
            jpanel.setLayout(new BorderLayout());
            caseText = new JTextArea();
            caseText.setLineWrap(true);
            caseText.setMinimumSize(new Dimension(400, 200));
            caseText.setPreferredSize(new Dimension(400, 200));
            jpanel.add(new JBScrollPane(caseText, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
            setModal(true);
            init();
            setTitle("Cookie login");
        }

        @NotNull
        @Override
        protected Action getOKAction() {
            Action action = super.getOKAction();
            action.putValue(Action.NAME, "login");
            return action;
        }

        @NotNull
        @Override
        protected Action[] createActions() {
            Action helpAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    BrowserUtil.browse("https://github.com/shuzijun/leetcode-editor/blob/master/doc/LoginHelp.md");
                }

            };
            helpAction.putValue(Action.NAME, "help");
            Action[] actions = new Action[]{helpAction, this.getOKAction(), this.getCancelAction()};
            return actions;
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            return jpanel;
        }

        public String cookieText() {
            return caseText.getText();
        }
    }
}
