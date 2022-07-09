package com.shuzijun.leetcode.plugin.model;

import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * @author shuzijun
 */
public class Find {

    private Map<String, List<Tag>> filter = Maps.newConcurrentMap();

    private Map<String, Sort> sortMap = Maps.newConcurrentMap();


    public Map<String, List<Tag>> getFilter() {

        return filter;
    }

    public Map<String, Sort> getSortMap() {

        return sortMap;
    }

    public void addFilter(String key, List<Tag> tags) {
        filter.put(key.toLowerCase(), tags);
    }

    public void addSort(String key, Sort sort) {
        sortMap.put(key.toLowerCase(), sort);
    }

    public List<Tag> getFilter(String key) {
        if (key == null) {
            return null;
        }
        return filter.get(key.toLowerCase());
    }

    public void clearFilter() {
        for (String key : filter.keySet()) {
            List<Tag> tagList = filter.get(key);
            for (Tag tag : tagList) {
                tag.setSelect(Boolean.FALSE);
            }
        }

    }

    public Sort getSort(String key) {
        if (key == null) {
            return null;
        }
        return sortMap.get(key.toLowerCase());
    }

    public void operationType(String key) {
        sortMap.forEach((s, sort) -> {
            if (!s.equalsIgnoreCase(key)) {
                sort.resetType();
            } else {
                sort.operationType();
            }
        });

    }

    public void resetFilterData(String key, List<Tag> tags) {
        if (CollectionUtils.isNotEmpty(getFilter(key))) {
            Map<String, Tag> oldListsMap = Maps.uniqueIndex(getFilter(key), tag -> tag.getSlug());
            Map<String, Tag> newListsMap = Maps.uniqueIndex(tags, tag -> tag.getSlug());
            newListsMap.forEach((s, tag) -> {
                if (oldListsMap.containsKey(s)) {
                    tag.setSelect(oldListsMap.get(s).isSelect());
                }
            });
            addFilter(key, tags);
        }
    }

}
