package com.shuzijun.leetcode.plugin.adapter.defaults;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.testFramework.ServiceContainerUtil;
import com.intellij.testFramework.TestApplicationManager;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.product.DefaultPersistentConfig;
import org.junit.Test;

import java.net.HttpCookie;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DefaultCookieLoginStrategyTest {

    @Test
    public void testParsesCookieValuesContainingEqualsSigns() {
        TestApplicationManager.getInstance();
        Disposable disposable = Disposer.newDisposable("DefaultCookieLoginStrategyTest");
        DefaultPersistentConfig persistentConfig = new DefaultPersistentConfig();
        Config config = new Config();
        config.setUrl("leetcode.cn");
        persistentConfig.setInitConfig(config);
        ServiceContainerUtil.registerOrReplaceServiceInstance(ApplicationManager.getApplication(),
                DefaultPersistentConfig.class, persistentConfig, disposable);

        try {
            List<HttpCookie> cookies = DefaultCookieLoginStrategy.parseCookies(
                    "csrftoken=token; LEETCODE_SESSION=session=value; invalid"
            );

            assertEquals(2, cookies.size());
            assertEquals("csrftoken", cookies.get(0).getName());
            assertEquals("token", cookies.get(0).getValue());
            assertEquals(".leetcode.cn", cookies.get(0).getDomain());
            assertEquals("LEETCODE_SESSION", cookies.get(1).getName());
            assertEquals("session=value", cookies.get(1).getValue());
            assertEquals(".leetcode.cn", cookies.get(1).getDomain());
            assertEquals("/", cookies.get(1).getPath());
        } finally {
            Disposer.dispose(disposable);
        }
    }
}
