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
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.window.ConsoleWindowFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

/**
 * @author shuzijun
 */
@Service
public final class MessageUtils implements Disposable {

    public static final String FLAG = "\033";

    private final Project project;
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
        showConsole(MessageLevel.INFO, title, body);
    }

    public void showWarnMsg(String title, String body) {
        showConsole(MessageLevel.WARNING, title, body);
    }

    public void showErrorMsg(String title, String body) {
        showConsole(MessageLevel.ERROR, title, body);
    }

    private void printTitle(MessageLevel level, String title) {
        consoleView.print(
                DateFormatUtils.format(new Date(), "HH:mm:ss") + "  " + level.label + "  ",
                level.contentType
        );
        consoleView.print(
                isGenericTitle(title) ? level.defaultTitle : title.trim(),
                ConsoleViewContentType.NORMAL_OUTPUT
        );
        consoleView.print("\n", ConsoleViewContentType.NORMAL_OUTPUT);
    }

    private void printBody(String body, ConsoleViewContentType contentType) {
        String[] bodys = StringUtils.defaultString(body).split("\n", -1);
        for (String s : bodys) {
            if (s.contains(FLAG)) {
                String[] sc = s.split(FLAG, -1);
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

    private boolean isGenericTitle(String title) {
        return StringUtils.isBlank(title)
                || "info".equalsIgnoreCase(title)
                || "warning".equalsIgnoreCase(title)
                || "error".equalsIgnoreCase(title);
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

    private void showConsole(MessageLevel level, String title, String body) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed()) {
                return;
            }
            if (toolWindow == null) {
                toolWindow = ToolWindowManager.getInstance(project).getToolWindow(ConsoleWindowFactory.ID);
            }
            if (toolWindow == null) {
                return;
            }
            if (!toolWindow.isAvailable()) {
                toolWindow.setAvailable(true);
            }
            if (consoleView == null) {
                toolWindow.show(() -> appendToConsole(level, title, body));
                return;
            }
            appendToConsole(level, title, body);
        }, ignored -> project.isDisposed());
    }

    private void appendToConsole(MessageLevel level, String title, String body) {
        if (project.isDisposed()) {
            return;
        }
        if (consoleView == null) {
            this.consoleView = ConsoleWindowFactory.getConsoleView(project);
        }
        if (consoleView == null) {
            return;
        }
        if (level == MessageLevel.ERROR && !toolWindow.isActive()) {
            toolWindow.activate(null);
        }
        printTitle(level, title);
        printBody(body, level.contentType);
        consoleView.print("\n", ConsoleViewContentType.NORMAL_OUTPUT);
        if (toolWindow.isVisible()) {
            consoleView.requestScrollingToEnd();
        }
    }

    @Override
    public void dispose() {
        consoleView = null;
        toolWindow = null;
    }

    private enum MessageLevel {
        INFO("INFO", "LeetCode result", ConsoleViewContentType.NORMAL_OUTPUT),
        WARNING("WARNING", "LeetCode warning", ConsoleViewContentType.LOG_WARNING_OUTPUT),
        ERROR("ERROR", "LeetCode error", ConsoleViewContentType.ERROR_OUTPUT);

        private final String label;
        private final String defaultTitle;
        private final ConsoleViewContentType contentType;

        MessageLevel(String label, String defaultTitle, ConsoleViewContentType contentType) {
            this.label = label;
            this.defaultTitle = defaultTitle;
            this.contentType = contentType;
        }
    }
}
