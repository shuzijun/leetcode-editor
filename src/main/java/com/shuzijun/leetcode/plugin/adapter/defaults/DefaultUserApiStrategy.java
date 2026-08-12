package com.shuzijun.leetcode.plugin.adapter.defaults;

import com.shuzijun.lc.UserQueryMode;
import com.shuzijun.leetcode.plugin.spi.UserApiStrategy;
import org.jetbrains.annotations.NotNull;

public final class DefaultUserApiStrategy implements UserApiStrategy {

    @Override
    public @NotNull UserQueryMode queryMode() {
        return UserQueryMode.GLOBAL_DATA;
    }
}
