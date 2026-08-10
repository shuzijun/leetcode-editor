package com.shuzijun.leetcode.plugin.product;

public final class ProductProfiles {

    private ProductProfiles() {
    }

    public static ProductProfile current() {
        return ProductServices.profile();
    }
}
