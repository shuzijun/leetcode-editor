package com.shuzijun.leetcode.plugin.window.login;

import com.intellij.openapi.project.Project;
import com.shuzijun.lc.model.User;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
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

    @Test
    public void keepsCookieLoginCompatibilityEntryPoint() throws Exception {
        Method cookieLogin = HttpLogin.class.getMethod(
                "cookieLogin",
                Project.class,
                String.class
        );
        Method cookieLoginAsync = HttpLogin.class.getMethod(
                "cookieLoginAsync",
                Project.class,
                String.class
        );

        assertEquals(void.class, cookieLogin.getReturnType());
        assertEquals(CompletableFuture.class, cookieLoginAsync.getReturnType());
    }

    private static User user(boolean signedIn, boolean verified, boolean phoneVerified) {
        User user = new User();
        user.setSignedIn(signedIn);
        user.setVerified(verified);
        user.setPhoneVerified(phoneVerified);
        return user;
    }
}
