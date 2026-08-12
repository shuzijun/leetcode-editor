package com.shuzijun.leetcode.plugin.product;

import com.shuzijun.leetcode.plugin.adapter.defaults.DefaultCodeExecutionPresentationStrategy;
import com.shuzijun.leetcode.plugin.adapter.defaults.DefaultConsoleOutputFormatter;
import com.shuzijun.leetcode.plugin.adapter.defaults.DefaultConsolePresenter;
import com.shuzijun.leetcode.plugin.adapter.defaults.DefaultCookieLoginStrategy;
import com.shuzijun.leetcode.plugin.adapter.defaults.DefaultLanguageTemplateProvider;
import com.shuzijun.leetcode.plugin.adapter.defaults.DefaultNavigatorSessionStrategy;
import com.shuzijun.leetcode.plugin.adapter.defaults.DefaultNoteContentStrategy;
import com.shuzijun.leetcode.plugin.adapter.defaults.DefaultQuestionPresentationStrategy;
import com.shuzijun.leetcode.plugin.adapter.defaults.DefaultUserApiStrategy;
import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class DefaultProductServiceProviderTest {

    @Test
    public void exposesDefaultOwnedStrategiesWithoutIdeServiceKeys() {
        assertTrue(ProductServices.profile() instanceof DefaultProductProfile);
        assertTrue(ProductServices.licenseGate() instanceof DefaultLicenseGate);
        assertTrue(ProductServices.languageTemplateProvider()
                instanceof DefaultLanguageTemplateProvider);
        assertTrue(ProductServices.consolePresenter() instanceof DefaultConsolePresenter);
        assertTrue(ProductServices.consoleOutputFormatter() instanceof DefaultConsoleOutputFormatter);
        assertTrue(ProductServices.cookieLoginStrategy() instanceof DefaultCookieLoginStrategy);
        assertTrue(ProductServices.navigatorSessionStrategy()
                instanceof DefaultNavigatorSessionStrategy);
        assertTrue(ProductServices.userApiStrategy() instanceof DefaultUserApiStrategy);
        assertTrue(ProductServices.questionPresentationStrategy()
                instanceof DefaultQuestionPresentationStrategy);
        assertTrue(ProductServices.codeExecutionPresentationStrategy()
                instanceof DefaultCodeExecutionPresentationStrategy);
        assertTrue(ProductServices.noteContentStrategy() instanceof DefaultNoteContentStrategy);
        assertSame(ProductServices.profile(), ProductServices.profile());
        assertSame(ProductServices.licenseGate(), ProductServices.licenseGate());
    }
}
