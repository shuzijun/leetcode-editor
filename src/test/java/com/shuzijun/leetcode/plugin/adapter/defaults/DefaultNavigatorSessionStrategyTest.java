package com.shuzijun.leetcode.plugin.adapter.defaults;

import com.shuzijun.lc.model.User;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefaultNavigatorSessionStrategyTest {

    @Test
    public void usesUsernameAsDefaultToolWindowTitle() {
        User user = new User();
        user.setUsername("public-user");
        user.setRealName("Default User");

        assertEquals(
                "public-user",
                new DefaultNavigatorSessionStrategy().displayName(user)
        );
    }
}
