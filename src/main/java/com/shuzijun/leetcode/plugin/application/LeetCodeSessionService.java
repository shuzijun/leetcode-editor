package com.shuzijun.leetcode.plugin.application;

import com.shuzijun.lc.LcClient;
import com.shuzijun.lc.RequestContext;
import com.shuzijun.lc.errors.LcException;
import com.shuzijun.lc.model.Session;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class LeetCodeSessionService {

    private static final ShortLivedCache<List<Session>> SESSION_CACHE =
            new ShortLivedCache<>(30, TimeUnit.SECONDS);

    @NotNull
    public List<Session> list(String userSlug, boolean cache) throws LcException {
        if (!cache) {
            return load(userSlug);
        }
        try {
            return SESSION_CACHE.get(cacheKey(userSlug), () -> load(userSlug));
        } catch (LcException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new LcException("Failed to load sessions", exception);
        }
    }

    public boolean switchTo(Integer sessionId) throws LcException {
        boolean switched = client().api().sessions()
                .switchTo(sessionId, RequestContext.DEFAULT);
        if (switched) {
            invalidateAfterSwitch();
        }
        return switched;
    }

    static void invalidateAfterSwitch() {
        SESSION_CACHE.invalidateAll();
        LeetCodeApiService.invalidateCaches();
    }

    @NotNull
    private List<Session> load(String userSlug) throws LcException {
        List<Session> sessions = client().api().sessions()
                .list(userSlug, RequestContext.DEFAULT);
        return Collections.unmodifiableList(sessions);
    }

    @NotNull
    static String cacheKey(String userSlug) {
        return cacheKey(URLUtils.getLeetcodeHost(), userSlug);
    }

    @NotNull
    static String cacheKey(String host, String userSlug) {
        return StringUtils.defaultString(host) + "\n" + StringUtils.defaultString(userSlug);
    }

    @NotNull
    private LcClient client() {
        return LcClientFactory.create();
    }
}
