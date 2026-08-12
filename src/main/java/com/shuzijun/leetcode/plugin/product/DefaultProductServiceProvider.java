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
public final class DefaultProductServiceProvider extends ProductServiceProvider {

    public DefaultProductServiceProvider() {
        super(
                new DefaultProductProfile(),
                new DefaultLicenseGate(),
                new DefaultLanguageTemplateProvider(),
                new DefaultConsolePresenter(),
                new DefaultConsoleOutputFormatter(),
                new DefaultCookieLoginStrategy(),
                new DefaultNavigatorSessionStrategy(),
                new DefaultUserApiStrategy(),
                new DefaultQuestionPresentationStrategy(),
                new DefaultCodeExecutionPresentationStrategy(),
                new DefaultNoteContentStrategy(),
                DefaultPersistentConfig.class,
                DefaultProjectConfig.class,
                DefaultStatisticsData.class,
                DefaultMessageUtils.class
        );
    }
}
