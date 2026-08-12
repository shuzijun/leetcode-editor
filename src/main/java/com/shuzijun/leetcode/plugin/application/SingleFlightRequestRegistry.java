package com.shuzijun.leetcode.plugin.application;

import com.intellij.util.concurrency.AppExecutorUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * Coalesces concurrent requests while keeping cancellation and returned values
 * isolated per consumer.
 */
public final class SingleFlightRequestRegistry<K, V> {

    private final ExecutorService executor;
    private final Map<K, Flight> flights = new HashMap<>();

    public SingleFlightRequestRegistry() {
        this(AppExecutorUtil.getAppExecutorService());
    }

    SingleFlightRequestRegistry(@NotNull ExecutorService executor) {
        this.executor = executor;
    }

    @NotNull
    public Subscription<V> subscribe(
            @NotNull K key,
            @NotNull Callable<V> loader,
            @NotNull Function<? super V, ? extends V> copier
    ) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(loader, "loader");
        Objects.requireNonNull(copier, "copier");

        Flight flight;
        SubscriptionImpl subscription;
        boolean start;
        synchronized (flights) {
            flight = flights.get(key);
            start = flight == null;
            if (start) {
                flight = new Flight(key, loader);
                flights.put(key, flight);
            }
            subscription = new SubscriptionImpl(flight, copier);
            flight.subscriptions.add(subscription);
        }
        if (start) {
            flight.start();
        }
        return subscription;
    }

    public interface Subscription<T> extends AutoCloseable {

        @NotNull
        CompletableFuture<T> future();

        boolean cancel();

        boolean isCancelled();

        @Override
        default void close() {
            cancel();
        }
    }

    /**
     * Required request dimensions for rendered question details. Callers may
     * include a resource discriminator when one question has multiple details.
     */
    public static final class RequestKey {
        private final String host;
        private final String titleSlug;
        private final String language;
        private final String renderSettings;
        private final String resource;

        public RequestKey(
                @NotNull String host,
                @NotNull String titleSlug,
                @NotNull String language,
                @NotNull String renderSettings
        ) {
            this(host, titleSlug, language, renderSettings, "");
        }

        public RequestKey(
                @NotNull String host,
                @NotNull String titleSlug,
                @NotNull String language,
                @NotNull String renderSettings,
                @NotNull String resource
        ) {
            this.host = Objects.requireNonNull(host, "host");
            this.titleSlug = Objects.requireNonNull(titleSlug, "titleSlug");
            this.language = Objects.requireNonNull(language, "language");
            this.renderSettings = Objects.requireNonNull(renderSettings, "renderSettings");
            this.resource = Objects.requireNonNull(resource, "resource");
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof RequestKey)) {
                return false;
            }
            RequestKey that = (RequestKey) other;
            return host.equals(that.host)
                    && titleSlug.equals(that.titleSlug)
                    && language.equals(that.language)
                    && renderSettings.equals(that.renderSettings)
                    && resource.equals(that.resource);
        }

        @Override
        public int hashCode() {
            return Objects.hash(host, titleSlug, language, renderSettings, resource);
        }
    }

    private final class Flight {
        private final K key;
        private final Callable<V> loader;
        private final List<SubscriptionImpl> subscriptions = new ArrayList<>();
        private Future<?> task;
        private boolean finished;

        private Flight(K key, Callable<V> loader) {
            this.key = key;
            this.loader = loader;
        }

        private void start() {
            Future<?> submitted;
            try {
                submitted = executor.submit(() -> {
                    try {
                        complete(loader.call(), null);
                    } catch (Throwable error) {
                        complete(null, error);
                    }
                });
            } catch (Throwable error) {
                complete(null, error);
                return;
            }
            synchronized (flights) {
                task = submitted;
                if (subscriptions.isEmpty() && !finished) {
                    flights.remove(key, this);
                    submitted.cancel(true);
                }
            }
        }

        private void complete(V value, Throwable error) {
            List<SubscriptionImpl> consumers;
            synchronized (flights) {
                if (finished) {
                    return;
                }
                finished = true;
                flights.remove(key, this);
                consumers = new ArrayList<>(subscriptions);
                subscriptions.clear();
            }
            for (SubscriptionImpl consumer : consumers) {
                consumer.complete(value, error);
            }
        }
    }

    private final class SubscriptionImpl implements Subscription<V> {
        private final Flight flight;
        private final Function<? super V, ? extends V> copier;
        private final CompletableFuture<V> future = new CompletableFuture<>();

        private SubscriptionImpl(Flight flight, Function<? super V, ? extends V> copier) {
            this.flight = flight;
            this.copier = copier;
        }

        @NotNull
        @Override
        public CompletableFuture<V> future() {
            return future;
        }

        @Override
        public boolean cancel() {
            Future<?> taskToCancel = null;
            synchronized (flights) {
                if (!flight.subscriptions.remove(this)) {
                    return future.cancel(false);
                }
                if (flight.subscriptions.isEmpty() && !flight.finished) {
                    flights.remove(flight.key, flight);
                    taskToCancel = flight.task;
                }
            }
            boolean cancelled = future.cancel(false);
            if (taskToCancel != null) {
                taskToCancel.cancel(true);
            }
            return cancelled;
        }

        @Override
        public boolean isCancelled() {
            return future.isCancelled();
        }

        private void complete(V value, Throwable error) {
            if (error != null) {
                future.completeExceptionally(error);
                return;
            }
            try {
                future.complete(copier.apply(value));
            } catch (Throwable copyError) {
                future.completeExceptionally(copyError);
            }
        }
    }
}
