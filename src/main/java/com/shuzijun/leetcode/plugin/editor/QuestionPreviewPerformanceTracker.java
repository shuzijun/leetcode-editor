package com.shuzijun.leetcode.plugin.editor;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Service(Service.Level.PROJECT)
public final class QuestionPreviewPerformanceTracker {

    private static final Logger LOG = Logger.getInstance(QuestionPreviewPerformanceTracker.class);

    public enum Milestone {
        ACTION_STARTED,
        QUESTION_READY,
        CONTENT_FILE_READY,
        EDITOR_OPEN_REQUESTED,
        BROWSER_CREATED,
        MAIN_FRAME_LOADED,
        VDITOR_READABLE,
        VISUAL_STABLE,
        READABLE_TIMEOUT
    }

    private final AtomicLong generations = new AtomicLong();
    private final Map<Long, Trace> traces = new ConcurrentHashMap<>();
    private final Map<String, Long> latestByTitleSlug = new ConcurrentHashMap<>();
    private final Map<String, Long> latestByContentPath = new ConcurrentHashMap<>();
    private final LongSupplier clock;

    public QuestionPreviewPerformanceTracker() {
        this(System::nanoTime);
    }

    QuestionPreviewPerformanceTracker(LongSupplier clock) {
        this.clock = clock;
    }

    public static QuestionPreviewPerformanceTracker getInstance(Project project) {
        return project.getService(QuestionPreviewPerformanceTracker.class);
    }

    public Trace begin(@Nullable String titleSlug) {
        long generation = generations.incrementAndGet();
        Trace trace = new Trace(generation, clock);
        traces.put(generation, trace);
        trace.mark(Milestone.ACTION_STARTED);
        bind(trace, titleSlug);
        return trace;
    }

    public void bind(@NotNull Trace trace, @Nullable String titleSlug) {
        if (titleSlug == null || titleSlug.isBlank()) {
            return;
        }
        trace.titleSlug = titleSlug;
        latestByTitleSlug.put(titleSlug, trace.generation);
    }

    public @Nullable Trace latest(@Nullable String titleSlug) {
        if (titleSlug == null) {
            return null;
        }
        Long generation = latestByTitleSlug.get(titleSlug);
        return generation == null ? null : traces.get(generation);
    }

    public void bindContentPath(@NotNull Trace trace, @Nullable String contentPath) {
        if (contentPath != null && !contentPath.isBlank()) {
            latestByContentPath.put(contentPath, trace.generation);
        }
    }

    public void activate(@NotNull Trace trace) {
        if (trace.titleSlug != null) {
            latestByTitleSlug.put(trace.titleSlug, trace.generation);
        }
    }

    public @Nullable Trace latestForContentPath(@Nullable String contentPath) {
        if (contentPath == null) {
            return null;
        }
        Long generation = latestByContentPath.get(contentPath);
        return generation == null ? null : traces.get(generation);
    }

    public boolean isCurrent(@NotNull Trace trace) {
        String titleSlug = trace.titleSlug;
        Long generation = titleSlug == null ? null : latestByTitleSlug.get(titleSlug);
        return generation != null && generation == trace.generation;
    }

    public long latestReadableElapsedMillis(@Nullable String titleSlug) {
        Trace trace = latest(titleSlug);
        return trace == null ? -1 : trace.elapsedMillis(Milestone.VDITOR_READABLE);
    }

    public boolean latestTimedOut(@Nullable String titleSlug) {
        Trace trace = latest(titleSlug);
        return trace != null && trace.has(Milestone.READABLE_TIMEOUT);
    }

    public long latestGeneration(@Nullable String titleSlug) {
        Trace trace = latest(titleSlug);
        return trace == null ? -1 : trace.generation();
    }

    public void readable(@NotNull Trace trace) {
        if (!isCurrent(trace) || !trace.mark(Milestone.VDITOR_READABLE)) {
            return;
        }
        log(trace, Milestone.VDITOR_READABLE);
    }

    public void visualStable(@NotNull Trace trace) {
        if (!isCurrent(trace) || !trace.mark(Milestone.VISUAL_STABLE)) {
            return;
        }
        log(trace, Milestone.VISUAL_STABLE);
    }

    public void timeout(@NotNull Trace trace) {
        if (!isCurrent(trace) || !trace.mark(Milestone.READABLE_TIMEOUT)) {
            return;
        }
        log(trace, Milestone.READABLE_TIMEOUT);
    }

    private void log(Trace trace, Milestone milestone) {
        String message = "Question preview performance"
                + " titleSlug=" + trace.titleSlug
                + " generation=" + trace.generation
                + " milestone=" + milestone
                + " elapsedMs=" + trace.elapsedMillis(milestone)
                + " stagesMs=" + trace.stageDurationsMillis();
        LOG.info(message);
        writeTestMetric(message);
    }

    private static void writeTestMetric(String message) {
        String output = System.getProperty("leetcode.test.preview.metrics.file");
        if (output == null || output.isBlank()) {
            return;
        }
        try {
            Files.writeString(
                    Path.of(output),
                    message + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException exception) {
            LOG.warn("Unable to write question preview test metric", exception);
        }
    }

    public static final class Trace {
        private final long generation;
        private final LongSupplier clock;
        private final Map<Milestone, Long> timestamps = new ConcurrentHashMap<>();
        private volatile String titleSlug;

        private Trace(long generation, LongSupplier clock) {
            this.generation = generation;
            this.clock = clock;
        }

        public long generation() {
            return generation;
        }

        public @Nullable String titleSlug() {
            return titleSlug;
        }

        public boolean mark(Milestone milestone) {
            return timestamps.putIfAbsent(milestone, clock.getAsLong()) == null;
        }

        public boolean has(Milestone milestone) {
            return timestamps.containsKey(milestone);
        }

        public long elapsedMillis(Milestone milestone) {
            Long start = timestamps.get(Milestone.ACTION_STARTED);
            Long end = timestamps.get(milestone);
            return start == null || end == null ? -1 : (end - start) / 1_000_000L;
        }

        public Map<Milestone, Long> stageDurationsMillis() {
            EnumMap<Milestone, Long> result = new EnumMap<>(Milestone.class);
            Long previous = null;
            List<Map.Entry<Milestone, Long>> ordered = timestamps.entrySet().stream()
                    .sorted(Comparator.comparingLong(Map.Entry::getValue))
                    .toList();
            for (Map.Entry<Milestone, Long> entry : ordered) {
                result.put(entry.getKey(), previous == null ? 0 : (entry.getValue() - previous) / 1_000_000L);
                previous = entry.getValue();
            }
            return Collections.unmodifiableMap(result);
        }
    }
}
