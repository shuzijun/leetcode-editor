package com.shuzijun.leetcode.plugin.window.login;

import com.shuzijun.leetcode.plugin.model.User;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HttpLoginTest {

    @Test
    public void warnsOnlyForSignedInUsersExplicitlyMarkedAsUnverified() {
        User unverifiedUser = user(true, false, false);
        assertTrue(HttpLogin.shouldWarnUnverifiedUser(unverifiedUser));

        assertFalse(HttpLogin.shouldWarnUnverifiedUser(user(true, true, false)));
        assertFalse(HttpLogin.shouldWarnUnverifiedUser(user(true, false, true)));
        assertFalse(HttpLogin.shouldWarnUnverifiedUser(user(false, false, false)));
        assertFalse(HttpLogin.shouldWarnUnverifiedUser(null));
    }

    private static User user(boolean signedIn, boolean verified, boolean phoneVerified) {
        User user = new User();
        user.setSignedIn(signedIn);
        user.setVerified(verified);
        user.setPhoneVerified(phoneVerified);
        return user;
    }
}
