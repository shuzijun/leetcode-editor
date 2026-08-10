package com.shuzijun.leetcode.plugin.adapter.defaults;

import com.shuzijun.lc.UserQueryMode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefaultUserApiStrategyTest {

    @Test
    public void preservesDefaultGlobalDataQueryMode() {
        assertEquals(UserQueryMode.GLOBAL_DATA, new DefaultUserApiStrategy().queryMode());
    }
}
