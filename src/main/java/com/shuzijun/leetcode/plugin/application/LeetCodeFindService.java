package com.shuzijun.leetcode.plugin.application;

import com.shuzijun.lc.LcClient;
import com.shuzijun.lc.RequestContext;
import com.shuzijun.lc.errors.LcException;
import com.shuzijun.leetcode.plugin.model.Tag;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class LeetCodeFindService {

    private static final ShortLivedCache<List<com.shuzijun.lc.model.Tag>> FIND_CACHE =
            new ShortLivedCache<>(30, TimeUnit.SECONDS);

    @NotNull
    public List<Tag> tags() throws LcException {
        List<com.shuzijun.lc.model.Tag> sdkTags = cached(
                cacheKey(URLUtils.getLeetcodeHost(), "tags", null),
                () -> client().api().questions().tags(RequestContext.DEFAULT)
        );
        boolean translatedName = "translatedName".equals(URLUtils.getTagName());
        List<Tag> tags = new ArrayList<>(sdkTags.size());
        for (com.shuzijun.lc.model.Tag sdkTag : sdkTags) {
            tags.add(LcModelMapper.toTag(sdkTag, translatedName));
        }
        return tags;
    }

    @NotNull
    public List<Tag> lists(String username) throws LcException {
        List<com.shuzijun.lc.model.Tag> sdkTags = cached(
                cacheKey(URLUtils.getLeetcodeHost(), "lists", username),
                () -> client().api().questions().lists(RequestContext.DEFAULT)
        );
        List<Tag> tags = new ArrayList<>(sdkTags.size());
        for (com.shuzijun.lc.model.Tag sdkTag : sdkTags) {
            tags.add(LcModelMapper.toTag(sdkTag, false));
        }
        return tags;
    }

    @NotNull
    public List<Tag> categories() throws LcException {
        List<com.shuzijun.lc.model.Tag> sdkTags = cached(
                cacheKey(URLUtils.getLeetcodeHost(), "categories", null),
                () -> client().api().questions().categories(RequestContext.DEFAULT)
        );
        List<Tag> tags = new ArrayList<>(sdkTags.size());
        for (com.shuzijun.lc.model.Tag sdkTag : sdkTags) {
            tags.add(toCategory(sdkTag, URLUtils.getLeetcodeUrl()));
        }
        return tags;
    }

    @NotNull
    private List<com.shuzijun.lc.model.Tag> cached(
            String cacheKey,
            Loader loader
    ) throws LcException {
        try {
            return FIND_CACHE.get(
                    cacheKey,
                    () -> Collections.unmodifiableList(new ArrayList<>(loader.load()))
            );
        } catch (LcException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new LcException("Failed to load find data", exception);
        }
    }

    @NotNull
    static Tag toCategory(com.shuzijun.lc.model.Tag sdkTag, String baseUrl) {
        Tag tag = LcModelMapper.toTag(sdkTag, false);
        tag.setType(categoryUrl(baseUrl, sdkTag.getType()));
        return tag;
    }

    static String categoryUrl(String baseUrl, String relativeUrl) {
        if (relativeUrl == null) {
            return null;
        }
        return StringUtils.removeEnd(baseUrl, "/")
                + "/api"
                + relativeUrl.replace("problemset", "problems");
    }

    @NotNull
    static String cacheKey(String host, String kind, String username) {
        return StringUtils.defaultString(host)
                + "\n" + StringUtils.defaultString(kind)
                + "\n" + StringUtils.defaultString(username);
    }

    @NotNull
    private LcClient client() {
        return LcClientFactory.create();
    }

    @FunctionalInterface
    private interface Loader {
        List<com.shuzijun.lc.model.Tag> load() throws LcException;
    }
}
