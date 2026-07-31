package com.shuzijun.leetcode.plugin.timer;

import org.junit.Test;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TimerBarWidgetTest {

    @Test
    public void startsStopsAndResetsTheTimerDisplay() throws Exception {
        TimerBarWidget widget = new TimerBarWidget(null);

        SwingUtilities.invokeAndWait(() -> widget.startTimer("Two Sum"));
        JLabel label = (JLabel) widget.getComponent();
        assertTrue(label.isVisible());
        assertEquals("[Two Sum]00:00:00", label.getText());
        assertTrue(timer(widget).isRunning());

        SwingUtilities.invokeAndWait(widget::stopTimer);
        assertFalse(timer(widget).isRunning());

        SwingUtilities.invokeAndWait(widget::reset);
        assertFalse(label.isVisible());
        assertEquals("[]00:00:00", label.getText());
        assertFalse(timer(widget).isRunning());
    }

    @Test
    public void restartsFromZeroWhenTheQuestionChanges() throws Exception {
        TimerBarWidget widget = new TimerBarWidget(null);

        SwingUtilities.invokeAndWait(() -> widget.startTimer("Two Sum"));
        SwingUtilities.invokeAndWait(() -> widget.startTimer("Add Two Numbers"));

        assertEquals("[Add Two Numbers]00:00:00", ((JLabel) widget.getComponent()).getText());
        widget.dispose();
    }

    private static Timer timer(TimerBarWidget widget) throws Exception {
        Field field = TimerBarWidget.class.getDeclaredField("timer");
        field.setAccessible(true);
        return (Timer) field.get(widget);
    }
}
