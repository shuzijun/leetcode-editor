package com.shuzijun.leetcode.plugin.utils;

import com.intellij.execution.process.AnsiEscapeDecoder;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import com.shuzijun.leetcode.plugin.product.ProductProfiles;
import com.shuzijun.leetcode.plugin.product.ProductServices;
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
public class MessageUtils implements Disposable {

    public static final String FLAG = "\033";

    private final Project project;
    private ConsoleView consoleView;
    private ToolWindow toolWindow;
    private final AnsiEscapeDecoder ansiEscapeDecoder = new AnsiEscapeDecoder();

    public MessageUtils(Project project) {
        this.project = project;
        this.toolWindow = ToolWindowManager.getInstance(project)
                .getToolWindow(ProductProfiles.current().consoleToolWindowId());
    }

    @NotNull
    public static MessageUtils getInstance(Project project) {
        return ProductServices.messageUtils(project);
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
        ProductServices.consolePresenter().info(project, title, body);
    }

    public void showWarnMsg(String title, String body) {
        ProductServices.consolePresenter().warning(project, title, body);
    }

    public void showErrorMsg(String title, String body) {
        ProductServices.consolePresenter().error(project, title, body);
    }

    public void showConsoleInfo(String title, String body) {
        showConsole(MessageLevel.INFO, title, body);
    }

    public void showConsoleWarning(String title, String body) {
        showConsole(MessageLevel.WARNING, title, body);
    }

    public void showConsoleError(String title, String body) {
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
        String text = StringUtils.defaultString(body);
        if (!text.endsWith("\n")) {
            text += "\n";
        }
        ansiEscapeDecoder.escapeText(text, ProcessOutputTypes.STDOUT, (chunk, attributes) ->
                consoleView.print(chunk, ConsoleViewContentType.getConsoleViewType(attributes))
        );
    }

    private boolean isGenericTitle(String title) {
        return StringUtils.isBlank(title)
                || "info".equalsIgnoreCase(title)
                || "warning".equalsIgnoreCase(title)
                || "error".equalsIgnoreCase(title);
    }

    public static void showAllWarnMsg(String title, String body) {
        Notifications.Bus.notify(new Notification(ProductProfiles.current().notificationGroup(), title, body, NotificationType.WARNING));
    }

    public String getComponentName() {
        return this.getClass().getName();
    }

    public static String format(String body, String type) {
        String ansiType;
        if ("I".equals(type)) {
            ansiType = "[32m";
        } else if ("W".equals(type)) {
            ansiType = "[33m";
        } else if ("E".equals(type)) {
            ansiType = "[31m";
        } else {
            ansiType = type;
        }
        String safeBody = StringUtils.defaultString(body);
        return FLAG + ansiType + safeBody.replace("\n", FLAG + "[0m\n" + FLAG + ansiType) + FLAG + "[0m";
    }

    public static String formatDiff(String expected, String output) {
        if ((StringUtils.isBlank(expected) && StringUtils.isNotBlank(output)) || (StringUtils.isNotBlank(expected) && StringUtils.isBlank(output))) {
            return FLAG + "[31m" + output + FLAG + "[0m";
        } else if (StringUtils.isBlank(expected) || StringUtils.isBlank(output) || output.equals(expected)) {
            return output;
        } else {
            boolean isDiff = false;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < output.length(); i++) {
                if (i >= expected.length()) {
                    if (!isDiff) {
                        sb.append(FLAG).append("[31m");
                    }
                    sb.append(output.substring(i)).append(FLAG).append("[0m");
                    isDiff = true;
                    break;
                } else {
                    if (output.charAt(i) == expected.charAt(i)) {
                        if (isDiff) {
                            sb.append(FLAG).append("[0m");
                            isDiff = false;
                        }
                        sb.append(output.charAt(i));
                    } else {
                        if (!isDiff) {
                            sb.append(FLAG).append("[31m");
                            isDiff = true;
                        }
                        sb.append(output.charAt(i));
                    }
                }

            }
            if (isDiff) {
                sb.append(FLAG).append("[0m");
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
                toolWindow = ToolWindowManager.getInstance(project)
                        .getToolWindow(ProductProfiles.current().consoleToolWindowId());
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
