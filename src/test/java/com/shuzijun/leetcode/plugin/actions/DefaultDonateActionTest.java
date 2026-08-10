package com.shuzijun.leetcode.plugin.actions;

import com.shuzijun.leetcode.plugin.actions.toolbar.DonateAction;
import com.shuzijun.leetcode.plugin.utils.BrowserUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;

public class DefaultDonateActionTest {

    private final AtomicReference<String> openedUrl = new AtomicReference<>();

    @Before
    public void setUp() {
        BrowserUtils.setTestBrowser(openedUrl::set);
    }

    @After
    public void tearDown() {
        BrowserUtils.setTestBrowser(null);
    }

    @Test
    public void opensTheDefaultDonationPage() {
        new DonateAction().actionPerformed(null);

        assertEquals("https://shuzijun.cn/donate.html", openedUrl.get());
    }
}
