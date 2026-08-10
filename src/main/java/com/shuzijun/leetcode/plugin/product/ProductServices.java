package com.shuzijun.leetcode.plugin.product;

import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.setting.ProjectConfig;
import com.shuzijun.leetcode.plugin.setting.StatisticsData;
import com.shuzijun.leetcode.plugin.spi.CodeExecutionPresentationStrategy;
import com.shuzijun.leetcode.plugin.spi.ConsolePresenter;
import com.shuzijun.leetcode.plugin.spi.CookieLoginStrategy;
import com.shuzijun.leetcode.plugin.spi.LanguageTemplateProvider;
import com.shuzijun.leetcode.plugin.spi.NavigatorSessionStrategy;
import com.shuzijun.leetcode.plugin.spi.NoteContentStrategy;
import com.shuzijun.leetcode.plugin.spi.QuestionPresentationStrategy;
import com.shuzijun.leetcode.plugin.spi.UserApiStrategy;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ServiceLoader;

public final class ProductServices {

    private static final ProductServiceProvider PROVIDER = loadProvider();

    private ProductServices() {
    }

    public static ProductProfile profile() {
        return PROVIDER.profile();
    }

    public static LicenseGate licenseGate() {
        return PROVIDER.licenseGate();
    }

    public static LanguageTemplateProvider languageTemplateProvider() {
        return PROVIDER.languageTemplateProvider();
    }

    public static ConsolePresenter consolePresenter() {
        return PROVIDER.consolePresenter();
    }

    public static CookieLoginStrategy cookieLoginStrategy() {
        return PROVIDER.cookieLoginStrategy();
    }

    public static NavigatorSessionStrategy navigatorSessionStrategy() {
        return PROVIDER.navigatorSessionStrategy();
    }

    public static UserApiStrategy userApiStrategy() {
        return PROVIDER.userApiStrategy();
    }

    public static QuestionPresentationStrategy questionPresentationStrategy() {
        return PROVIDER.questionPresentationStrategy();
    }

    public static CodeExecutionPresentationStrategy codeExecutionPresentationStrategy() {
        return PROVIDER.codeExecutionPresentationStrategy();
    }

    public static NoteContentStrategy noteContentStrategy() {
        return PROVIDER.noteContentStrategy();
    }

    public static PersistentConfig persistentConfig() {
        return PROVIDER.persistentConfig();
    }

    public static ProjectConfig projectConfig(Project project) {
        return PROVIDER.projectConfig(project);
    }

    public static StatisticsData statisticsData(Project project) {
        return PROVIDER.statisticsData(project);
    }

    public static MessageUtils messageUtils(Project project) {
        return PROVIDER.messageUtils(project);
    }

    private static ProductServiceProvider loadProvider() {
        return ServiceLoader.load(
                        ProductServiceProvider.class,
                        ProductServiceProvider.class.getClassLoader()
                )
                .findFirst()
                .orElseGet(ProductServices::loadDefaultProvider);
    }

    private static ProductServiceProvider loadDefaultProvider() {
        try {
            return (ProductServiceProvider) Class.forName(
                            "com.shuzijun.leetcode.plugin.product.DefaultProductServiceProvider"
                    )
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (ClassNotFoundException
                 | NoSuchMethodException
                 | InstantiationException
                 | IllegalAccessException
                 | InvocationTargetException exception) {
            throw new IllegalStateException("No product service provider is available", exception);
        }
    }
}
