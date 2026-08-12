package com.shuzijun.leetcode.plugin.window.login;

import com.intellij.openapi.project.Project;
import com.shuzijun.lc.model.User;
import com.shuzijun.leetcode.plugin.window.NavigatorTabsPanel;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;

public class HttpLoginNotificationTest {

    @Test
    public void keepsTheAsynchronousLoginSynchronizationContract() throws Exception {
        Method loadUser = NavigatorTabsPanel.class.getMethod("loadUser", boolean.class);
        Method notifyLogin = HttpLogin.class.getMethod(
                "notifyLoginAfterUserLoaded",
                Project.class,
                String.class
        );

        assertEquals(CompletableFuture.class, loadUser.getReturnType());
        ParameterizedType futureType = (ParameterizedType) loadUser.getGenericReturnType();
        assertEquals(User.class, futureType.getActualTypeArguments()[0]);
        assertEquals(void.class, notifyLogin.getReturnType());
    }
}
