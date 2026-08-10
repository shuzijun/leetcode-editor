package com.shuzijun.leetcode.plugin.adapter.defaults;

import com.intellij.execution.filters.TextConsoleBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefaultConsolePresenterTest {

    @Test
    public void leavesTheDefaultConsoleBuilderUnchanged() {
        RecordingConsoleBuilder builder = new RecordingConsoleBuilder();

        new DefaultConsolePresenter().configure(builder);

        assertEquals(0, builder.filterCalls);
    }

    private static final class RecordingConsoleBuilder extends TextConsoleBuilder {
        private int filterCalls;

        @Override
        public com.intellij.execution.ui.ConsoleView getConsole() {
            return null;
        }

        @Override
        public void addFilter(com.intellij.execution.filters.Filter filter) {
            filterCalls++;
        }

        @Override
        public void setViewer(boolean isViewer) {
        }
    }
}
