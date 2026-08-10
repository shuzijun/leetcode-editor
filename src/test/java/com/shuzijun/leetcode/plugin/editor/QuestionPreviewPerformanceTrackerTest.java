package com.shuzijun.leetcode.plugin.editor;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

public class QuestionPreviewPerformanceTrackerTest {

    @Test
    public void keepsOnlyTheLatestTraceCurrentForAQuestion() {
        AtomicLong clock = new AtomicLong();
        QuestionPreviewPerformanceTracker tracker =
                new QuestionPreviewPerformanceTracker(() -> clock.addAndGet(1_000_000L));

        QuestionPreviewPerformanceTracker.Trace first = tracker.begin("two-sum");
        QuestionPreviewPerformanceTracker.Trace second = tracker.begin("two-sum");

        tracker.readable(first);
        tracker.readable(second);

        assertFalse(first.has(QuestionPreviewPerformanceTracker.Milestone.VDITOR_READABLE));
        assertTrue(second.has(QuestionPreviewPerformanceTracker.Milestone.VDITOR_READABLE));
        assertSame(second, tracker.latest("two-sum"));
    }

    @Test
    public void bindsRandomQuestionAndContentPathToOneTrace() {
        AtomicLong clock = new AtomicLong();
        QuestionPreviewPerformanceTracker tracker =
                new QuestionPreviewPerformanceTracker(() -> clock.addAndGet(1_000_000L));
        QuestionPreviewPerformanceTracker.Trace trace = tracker.begin(null);

        tracker.bind(trace, "two-sum");
        tracker.bindContentPath(trace, "/tmp/doc/[1]two-sum.md");
        trace.mark(QuestionPreviewPerformanceTracker.Milestone.QUESTION_READY);
        tracker.readable(trace);

        assertSame(trace, tracker.latest("two-sum"));
        assertSame(trace, tracker.latestForContentPath("/tmp/doc/[1]two-sum.md"));
        assertTrue(trace.elapsedMillis(QuestionPreviewPerformanceTracker.Milestone.VDITOR_READABLE) > 0);
    }

    @Test
    public void recordsEachMilestoneOnce() {
        AtomicLong clock = new AtomicLong();
        QuestionPreviewPerformanceTracker tracker =
                new QuestionPreviewPerformanceTracker(() -> clock.addAndGet(1_000_000L));
        QuestionPreviewPerformanceTracker.Trace trace = tracker.begin("two-sum");

        assertTrue(trace.mark(QuestionPreviewPerformanceTracker.Milestone.QUESTION_READY));
        assertFalse(trace.mark(QuestionPreviewPerformanceTracker.Milestone.QUESTION_READY));
        assertEquals(
                Long.valueOf(1L),
                trace.stageDurationsMillis().get(QuestionPreviewPerformanceTracker.Milestone.QUESTION_READY)
        );
    }

    @Test
    public void activatingTheDisplayedPreviewMakesItsTraceCurrent() {
        AtomicLong clock = new AtomicLong();
        QuestionPreviewPerformanceTracker tracker =
                new QuestionPreviewPerformanceTracker(() -> clock.addAndGet(1_000_000L));
        QuestionPreviewPerformanceTracker.Trace displayed = tracker.begin("two-sum");
        tracker.begin("two-sum");

        tracker.activate(displayed);
        tracker.readable(displayed);

        assertSame(displayed, tracker.latest("two-sum"));
        assertTrue(displayed.has(QuestionPreviewPerformanceTracker.Milestone.VDITOR_READABLE));
    }
}
