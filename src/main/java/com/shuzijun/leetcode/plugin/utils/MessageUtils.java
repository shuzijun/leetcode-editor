package com.shuzijun.leetcode.plugin.utils;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.window.ConsoleWindowFactory;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

/**
 * @author shuzijun
 */
@Service
public final class MessageUtils implements Disposable {

    public static String FLAG = "\033";

    private Project project;
    private ConsoleView consoleView;
    private ToolWindow toolWindow;

    public MessageUtils(Project project) {
        this.project = project;
        this.toolWindow = ToolWindowManager.getInstance(project).getToolWindow(ConsoleWindowFactory.ID);
    }

    @NotNull
    public static MessageUtils getInstance(Project project) {
        return project.getService(MessageUtils.class);
    }


    public static void showMsg(JComponent component, MessageType messageType, String title, String body) {
        JBPopupFactory factory = JBPopupFactory.getInstance();
        BalloonBuilder builder = factory.createHtmlTextBalloonBuilder(body, messageType, null);
        builder.setTitle(title);
        builder.setFillColor(JBColor.background());
        Balloon b = builder.createBalloon();
        Rectangle r = component.getBounds();
        RelativePoint p = new RelativePoint(component, new Point(r.x + r.width, r.y + 30));
        b.show(p, Balloon.Position.atRight);
    }

    public void showInfoMsg(String title, String body) {
        showConsole(() -> {
            printTitle(title, ConsoleViewContentType.NORMAL_OUTPUT);
            printBody(body, ConsoleViewContentType.NORMAL_OUTPUT);
            consoleView.print("\n", ConsoleViewContentType.NORMAL_OUTPUT);
        });
    }

    public void showWarnMsg(String title, String body) {
        showConsole(() -> {
            printTitle(title, ConsoleViewContentType.LOG_INFO_OUTPUT);
            printBody(body, ConsoleViewContentType.LOG_INFO_OUTPUT);
            consoleView.print("\n", ConsoleViewContentType.LOG_INFO_OUTPUT);
        });
    }

    public void showErrorMsg(String title, String body) {
        showConsole(() -> {
            printTitle(title, ConsoleViewContentType.ERROR_OUTPUT);
            printBody(body, ConsoleViewContentType.ERROR_OUTPUT);
            consoleView.print("\n", ConsoleViewContentType.ERROR_OUTPUT);
        });
    }

    private void printTitle(String title, ConsoleViewContentType contentType) {
        if (title.equals("info") || title.equals("warning") || title.equals("error")) {
            consoleView.print("> " + DateFormatUtils.format(new Date(), "yyyy/MM/dd' 'HH:mm:ss") + "\n", contentType);
        } else {
            consoleView.print("> " + DateFormatUtils.format(new Date(), "yyyy/MM/dd' 'HH:mm:ss") + "\t" + title + "\n", contentType);
        }
    }

    private void printBody(String body, ConsoleViewContentType contentType) {
        String[] bodys = body.split("\n");
        for (String s : bodys) {
            if (s.contains(FLAG)) {
                String[] sc = s.split(FLAG);
                for (int i = 0; i < sc.length; i++) {
                    if (i % 2 == 0) {
                        consoleView.print(sc[i], contentType);
                    } else {
                        String childStr = sc[i];
                        if (childStr.startsWith("I")) {
                            consoleView.print(sc[i].substring(1), ConsoleViewContentType.NORMAL_OUTPUT);
                        } else if (childStr.startsWith("W")) {
                            consoleView.print(sc[i].substring(1), ConsoleViewContentType.LOG_INFO_OUTPUT);
                        } else if (childStr.startsWith("E")) {
                            consoleView.print(sc[i].substring(1), ConsoleViewContentType.ERROR_OUTPUT);
                        } else {
                            consoleView.print(sc[i].substring(1), contentType);
                        }
                    }
                }
                consoleView.print("\n", contentType);
            } else {
                consoleView.print(s + "\n", contentType);
            }

        }
    }

    public static void showAllWarnMsg(String title, String body) {
        Notifications.Bus.notify(new Notification(PluginConstant.NOTIFICATION_GROUP, title, body, NotificationType.WARNING));
    }

    public String getComponentName() {
        return this.getClass().getName();
    }

    public static String format(String body, String type) {
        return FLAG + type + body.replace("\n", FLAG + "\n" + FLAG + type) + FLAG;
    }

    public static String formatDiff(String expected, String output) {
        if ((StringUtils.isBlank(expected) && StringUtils.isNotBlank(output)) || (StringUtils.isNotBlank(expected) && StringUtils.isBlank(output))) {
            return FLAG + "E" + output + FLAG;
        } else if (StringUtils.isBlank(expected) || StringUtils.isBlank(output) || output.equals(expected)) {
            return output;
        } else {
            boolean isDiff = false;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < output.length(); i++) {
                if (i >= expected.length()) {
                    if (!isDiff) {
                        sb.append(FLAG).append("E");
                    }
                    sb.append(output.substring(i)).append(FLAG);
                    isDiff = true;
                    break;
                } else {
                    if (output.charAt(i) == expected.charAt(i)) {
                        if (isDiff) {
                            sb.append(FLAG);
                            isDiff = false;
                        }
                        sb.append(output.charAt(i));
                    } else {
                        if (!isDiff) {
                            sb.append(FLAG).append("E");
                            isDiff = true;
                        }
                        sb.append(output.charAt(i));
                    }
                }

            }
            if (isDiff) {
                sb.append(FLAG);
            }
            return sb.toString();
        }
    }

    private void showConsole(Runnable runnable) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (toolWindow == null) {
                toolWindow = ToolWindowManager.getInstance(project).getToolWindow(ConsoleWindowFactory.ID);
            }
            if (toolWindow == null) {
                return;
            }
            if (!toolWindow.isAvailable()) {
                toolWindow.setAvailable(true);
            }
            if (!toolWindow.isActive()) {
                toolWindow.activate(null);
            }
            if (consoleView == null) {
                this.consoleView = ConsoleWindowFactory.getDataContext(project).getData(DataKeys.LEETCODE_CONSOLE_VIEW);
            }
            consoleView.requestScrollingToEnd();
            runnable.run();
        });


    }

    @Override
    public void dispose() {
        if (consoleView != null) {
            Disposer.dispose(consoleView);
        }
    }
}
