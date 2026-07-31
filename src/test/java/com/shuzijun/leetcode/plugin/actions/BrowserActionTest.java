package com.shuzijun.leetcode.plugin.actions;

import com.shuzijun.leetcode.plugin.actions.toolbar.DonateAction;
import com.shuzijun.leetcode.plugin.actions.toolbar.HelpAction;
import com.shuzijun.leetcode.plugin.actions.toolbar.ShareAction;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.utils.BrowserUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;

public class BrowserActionTest {

    private final AtomicReference<String> openedUrl = new AtomicReference<>();

    @Before
    public void setUp() {
        BrowserUtils.setTestBrowser(openedUrl::set);
        System.setProperty("leetcode.test.base.url", "http://127.0.0.1:8080");
    }

    @After
    public void tearDown() {
        BrowserUtils.setTestBrowser(null);
        System.clearProperty("leetcode.test.base.url");
    }

    @Test
    public void toolbarBrowserActionsUseTheirExpectedUrls() {
        new HelpAction().actionPerformed(null);
        assertEquals("https://github.com/shuzijun/leetcode-editor", openedUrl.get());

        new DonateAction().actionPerformed(null);
        assertEquals("https://shuzijun.cn/donate.html", openedUrl.get());

        new ShareAction().actionPerformed(null, null);
        assertEquals("https://codetop.cc/?utm_source=leetcode_editor", openedUrl.get());
    }

    @Test
    public void treeAndEditorOpenInWebActionsUseTheQuestionUrl() {
        Question question = new Question();
        question.setTitleSlug("two-sum");

        new com.shuzijun.leetcode.plugin.actions.tree.OpenInWebAction()
                .actionPerformed(null, null, question);
        assertEquals("http://127.0.0.1:8080/problems/two-sum", openedUrl.get());

        new com.shuzijun.leetcode.plugin.actions.editor.OpenInWebAction()
                .actionPerformed(null, null, question);
        assertEquals("http://127.0.0.1:8080/problems/two-sum", openedUrl.get());
    }
}
