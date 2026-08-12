package com.shuzijun.leetcode.plugin.spi;

import com.shuzijun.lc.UserQueryMode;
import org.jetbrains.annotations.NotNull;

public interface UserApiStrategy {

    @NotNull UserQueryMode queryMode();
}
