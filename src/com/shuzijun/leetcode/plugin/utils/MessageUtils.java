package com.shuzijun.leetcode.plugin.utils;

import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.awt.RelativePoint;

import javax.swing.*;
import java.awt.*;

/**
 * @author shuzijun
 */
public class MessageUtils {

    public static void showMsg(JComponent component, MessageType messageType,String title,String body){
        JBPopupFactory factory = JBPopupFactory.getInstance();
        BalloonBuilder builder = factory.createHtmlTextBalloonBuilder(body, messageType, null);
        builder.setTitle(title);
        builder.setFillColor(JBColor.background());
        Balloon b = builder.createBalloon();
        Rectangle r = component.getBounds();
        RelativePoint p = new RelativePoint(component, new Point(r.x + r.width, r.y + 30));
        b.show(p, Balloon.Position.atRight);
    }
}
