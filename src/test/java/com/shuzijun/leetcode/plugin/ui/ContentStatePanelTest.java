package com.shuzijun.leetcode.plugin.ui;

import com.intellij.ui.components.labels.LinkLabel;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ContentStatePanelTest {

    @Test
    public void exposesCurrentStateAcrossContentLifecycle() {
        ContentStatePanel panel = new ContentStatePanel();

        panel.showLoading("Loading");
        assertEquals(ContentStatePanel.State.LOADING, panel.getState());

        panel.showContent(new JPanel());
        assertEquals(ContentStatePanel.State.CONTENT, panel.getState());

        panel.showEmpty("Empty", null, null);
        assertEquals(ContentStatePanel.State.EMPTY, panel.getState());

        panel.showError("Failed", "Retry", () -> {
        });
        assertEquals(ContentStatePanel.State.ERROR, panel.getState());

        panel.showLoginRequired("Sign in first", "Sign in", null);
        assertEquals(ContentStatePanel.State.LOGIN_REQUIRED, panel.getState());
    }

    @Test
    public void invokesRecoveryActionFromErrorState() {
        AtomicBoolean retried = new AtomicBoolean();
        ContentStatePanel panel = new ContentStatePanel();

        panel.showError("Failed", "Retry", () -> retried.set(true));
        LinkLabel<?> retry = findComponent(panel, LinkLabel.class);

        assertFalse(retried.get());
        retry.doClick();
        assertTrue(retried.get());
    }

    @Test
    public void replacesPreviousContentComponent() {
        ContentStatePanel panel = new ContentStatePanel();
        JPanel first = new JPanel();
        JPanel second = new JPanel();

        panel.showContent(first);
        panel.showContent(second);

        assertEquals(2, panel.getComponentCount());
        assertSame(second, findVisibleContent(panel));
    }

    @Test
    public void keepsContentMountedBehindLoadingState() {
        ContentStatePanel panel = new ContentStatePanel();
        JPanel content = new JPanel();

        panel.showLoadingOver(content, "Loading");

        assertEquals(ContentStatePanel.State.LOADING, panel.getState());
        assertSame(panel, content.getParent());
        assertTrue(content.isVisible());
    }

    private static Component findVisibleContent(ContentStatePanel panel) {
        for (Component component : panel.getComponents()) {
            if (component.isVisible()) {
                return component;
            }
        }
        throw new AssertionError("No visible content component");
    }

    private static <T extends Component> T findComponent(Container root, Class<T> type) {
        for (Component component : root.getComponents()) {
            if (type.isInstance(component)) {
                return type.cast(component);
            }
            if (component instanceof Container) {
                try {
                    return findComponent((Container) component, type);
                } catch (AssertionError ignored) {
                }
            }
        }
        throw new AssertionError("Missing component: " + type.getName());
    }
}
