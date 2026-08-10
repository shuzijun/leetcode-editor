package com.shuzijun.leetcode.plugin.ui;

import com.intellij.icons.AllIcons;
import com.intellij.ui.AnimatedIcon;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Displays content and its loading, empty, or recoverable error state without
 * forcing each editor tab to invent a different interaction.
 */
public final class ContentStatePanel extends JPanel {

    public enum State {
        IDLE,
        LOADING,
        CONTENT,
        EMPTY,
        ERROR,
        LOGIN_REQUIRED
    }

    private static final String STATE_CARD = "state";
    private static final String CONTENT_CARD = "content";

    private final JPanel statePanel = new JPanel(new GridBagLayout());
    private State state = State.IDLE;

    public ContentStatePanel() {
        super();
        setLayout(new OverlayLayout(this));
        statePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statePanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        add(statePanel);
        showState(State.IDLE, "", null, null);
    }

    public State getState() {
        return state;
    }

    public void showLoading(@NotNull String message) {
        showState(State.LOADING, message, null, null);
    }

    public void showLoadingOver(@NotNull JComponent content, @NotNull String message) {
        replaceContent(content);
        showState(State.LOADING, message, null, null);
    }

    public void showContent(@NotNull JComponent content) {
        replaceContent(content);
        state = State.CONTENT;
        statePanel.setVisible(false);
        content.setVisible(true);
        revalidate();
        repaint();
    }

    public void showEmpty(@NotNull String message, @Nullable String actionText, @Nullable Runnable action) {
        showState(State.EMPTY, message, actionText, action);
    }

    public void showError(@NotNull String message, @NotNull String retryText, @Nullable Runnable retry) {
        showState(State.ERROR, message, retry == null ? null : retryText, retry);
    }

    public void showLoginRequired(@NotNull String message, @NotNull String signInText, @Nullable Runnable signIn) {
        showState(State.LOGIN_REQUIRED, message, signIn == null ? null : signInText, signIn);
    }

    private void showState(State newState, String message, String actionText, Runnable action) {
        state = newState;
        statePanel.removeAll();

        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        if (newState == State.LOADING) {
            row.add(new JBLabel(AnimatedIcon.Default.INSTANCE));
            row.add(Box.createHorizontalStrut(JBUI.scale(8)));
        } else if (newState == State.ERROR) {
            row.add(new JBLabel(AllIcons.General.Error));
            row.add(Box.createHorizontalStrut(JBUI.scale(8)));
        } else if (newState == State.LOGIN_REQUIRED) {
            row.add(new JBLabel(AllIcons.General.Warning));
            row.add(Box.createHorizontalStrut(JBUI.scale(8)));
        }
        row.add(new JBLabel(message));
        if (actionText != null && action != null) {
            row.add(Box.createHorizontalStrut(JBUI.scale(8)));
            row.add(LinkLabel.create(actionText, action));
        }

        statePanel.add(row);
        statePanel.setVisible(true);
        revalidate();
        repaint();
    }

    private void replaceContent(JComponent content) {
        Component previous = findContent();
        if (previous != null && previous != content) {
            remove(previous);
        }
        if (content.getParent() != this) {
            content.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.setAlignmentY(Component.CENTER_ALIGNMENT);
            add(content);
        }
        content.setVisible(true);
    }

    private Component findContent() {
        for (Component component : getComponents()) {
            if (component != statePanel) {
                return component;
            }
        }
        return null;
    }
}
