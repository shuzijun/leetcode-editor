package com.shuzijun.leetcode.plugin.spi;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ContributionRegistry {

    private ContributionRegistry() {
    }

    @NotNull
    public static <T extends OrderedContribution> List<T> ordered(@NotNull List<T> contributions) {
        List<T> ordered = new ArrayList<>(contributions);
        ordered.sort(Comparator.comparingInt(OrderedContribution::getOrder)
                .thenComparing(OrderedContribution::getId));
        Set<String> ids = new HashSet<>();
        for (T contribution : ordered) {
            if (!ids.add(contribution.getId())) {
                throw new IllegalArgumentException("Duplicate contribution id: " + contribution.getId());
            }
        }
        return ordered;
    }
}
