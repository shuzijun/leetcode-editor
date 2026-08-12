package com.shuzijun.leetcode.plugin.application;

import com.shuzijun.lc.RequestContext;
import com.shuzijun.lc.errors.LcException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

final class DetailRequestCoordinator {

    private static final SingleFlightRequestRegistry<
            SingleFlightRequestRegistry.RequestKey, Object> REQUESTS =
            new SingleFlightRequestRegistry<>();

    private DetailRequestCoordinator() {
    }

    @Nullable
    static <T> T load(
            @NotNull SingleFlightRequestRegistry.RequestKey key,
            @NotNull RequestLoader<T> loader,
            @NotNull Function<? super T, ? extends T> copier
    ) throws LcException {
        SingleFlightRequestRegistry.Subscription<Object> subscription = REQUESTS.subscribe(
                key,
                () -> loader.load(RequestContext.builder()
                        .cancellationToken(Thread.currentThread()::isInterrupted)
                        .build()),
                value -> copier.apply(cast(value))
        );
        try {
            return cast(subscription.future().get());
        } catch (InterruptedException exception) {
            subscription.cancel();
            Thread.currentThread().interrupt();
            throw new LcException("Detail request interrupted", exception);
        } catch (CancellationException exception) {
            throw new LcException("Detail request cancelled", exception);
        } catch (ExecutionException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof LcException) {
                throw (LcException) cause;
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new LcException("Detail request failed", asException(cause));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object value) {
        return (T) value;
    }

    private static Exception asException(Throwable cause) {
        return cause instanceof Exception
                ? (Exception) cause
                : new Exception(cause);
    }

    @FunctionalInterface
    interface RequestLoader<T> {
        T load(RequestContext context) throws LcException;
    }
}
