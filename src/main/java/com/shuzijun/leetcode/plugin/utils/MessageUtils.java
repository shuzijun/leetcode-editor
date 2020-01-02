package com.shuzijun.leetcode.plugin.utils;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;
import com.shuzijun.leetcode.plugin.model.Constant;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @author shuzijun
 */
public class MessageUtils implements ProjectComponent {

    private Project project;

    public MessageUtils(Project project) {
        this.project = project;
    }

    @Nullable
    public static MessageUtils getInstance(Project project) {
        return ServiceManager.getService(project, MessageUtils.class);
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
        Notifications.Bus.notify(new Notification(Constant.NOTIFICATION_GROUP, title, body, NotificationType.INFORMATION), project);
    }

    public void showWarnMsg(String title, String body) {
        Notifications.Bus.notify(new Notification(Constant.NOTIFICATION_GROUP, title, body, NotificationType.WARNING), project);
    }

    public void showErrorMsg(String title, String body) {
        Notifications.Bus.notify(new Notification(Constant.NOTIFICATION_GROUP, title, body, NotificationType.ERROR), project);
    }

    public static void showAllWarnMsg(String title, String body) {
        Notifications.Bus.notify(new Notification(Constant.NOTIFICATION_GROUP, title, body, NotificationType.WARNING));
    }
}
