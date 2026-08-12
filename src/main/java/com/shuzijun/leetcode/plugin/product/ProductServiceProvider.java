package com.shuzijun.leetcode.plugin.product;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.setting.ProjectConfig;
import com.shuzijun.leetcode.plugin.setting.StatisticsData;
import com.shuzijun.leetcode.plugin.spi.CodeExecutionPresentationStrategy;
import com.shuzijun.leetcode.plugin.spi.ConsoleOutputFormatter;
import com.shuzijun.leetcode.plugin.spi.ConsolePresenter;
import com.shuzijun.leetcode.plugin.spi.CookieLoginStrategy;
import com.shuzijun.leetcode.plugin.spi.LanguageTemplateProvider;
import com.shuzijun.leetcode.plugin.spi.NavigatorSessionStrategy;
import com.shuzijun.leetcode.plugin.spi.NoteContentStrategy;
import com.shuzijun.leetcode.plugin.spi.QuestionPresentationStrategy;
import com.shuzijun.leetcode.plugin.spi.UserApiStrategy;
import com.shuzijun.leetcode.plugin.utils.MessageUtils;

public abstract class ProductServiceProvider {

    private final ProductProfile profile;
    private final LicenseGate licenseGate;
    private final LanguageTemplateProvider languageTemplateProvider;
    private final ConsolePresenter consolePresenter;
    private final ConsoleOutputFormatter consoleOutputFormatter;
    private final CookieLoginStrategy cookieLoginStrategy;
    private final NavigatorSessionStrategy navigatorSessionStrategy;
    private final UserApiStrategy userApiStrategy;
    private final QuestionPresentationStrategy questionPresentationStrategy;
    private final CodeExecutionPresentationStrategy codeExecutionPresentationStrategy;
    private final NoteContentStrategy noteContentStrategy;
    private final Class<? extends PersistentConfig> persistentConfigClass;
    private final Class<? extends ProjectConfig> projectConfigClass;
    private final Class<? extends StatisticsData> statisticsDataClass;
    private final Class<? extends MessageUtils> messageUtilsClass;

    protected ProductServiceProvider(
            ProductProfile profile,
            LicenseGate licenseGate,
            LanguageTemplateProvider languageTemplateProvider,
            ConsolePresenter consolePresenter,
            ConsoleOutputFormatter consoleOutputFormatter,
            CookieLoginStrategy cookieLoginStrategy,
            NavigatorSessionStrategy navigatorSessionStrategy,
            UserApiStrategy userApiStrategy,
            QuestionPresentationStrategy questionPresentationStrategy,
            CodeExecutionPresentationStrategy codeExecutionPresentationStrategy,
            NoteContentStrategy noteContentStrategy,
            Class<? extends PersistentConfig> persistentConfigClass,
            Class<? extends ProjectConfig> projectConfigClass,
            Class<? extends StatisticsData> statisticsDataClass,
            Class<? extends MessageUtils> messageUtilsClass
    ) {
        this.profile = profile;
        this.licenseGate = licenseGate;
        this.languageTemplateProvider = languageTemplateProvider;
        this.consolePresenter = consolePresenter;
        this.consoleOutputFormatter = consoleOutputFormatter;
        this.cookieLoginStrategy = cookieLoginStrategy;
        this.navigatorSessionStrategy = navigatorSessionStrategy;
        this.userApiStrategy = userApiStrategy;
        this.questionPresentationStrategy = questionPresentationStrategy;
        this.codeExecutionPresentationStrategy = codeExecutionPresentationStrategy;
        this.noteContentStrategy = noteContentStrategy;
        this.persistentConfigClass = persistentConfigClass;
        this.projectConfigClass = projectConfigClass;
        this.statisticsDataClass = statisticsDataClass;
        this.messageUtilsClass = messageUtilsClass;
    }

    public final ProductProfile profile() {
        return profile;
    }

    public final LicenseGate licenseGate() {
        return licenseGate;
    }

    public final LanguageTemplateProvider languageTemplateProvider() {
        return languageTemplateProvider;
    }

    public final ConsolePresenter consolePresenter() {
        return consolePresenter;
    }

    public final ConsoleOutputFormatter consoleOutputFormatter() {
        return consoleOutputFormatter;
    }

    public final CookieLoginStrategy cookieLoginStrategy() {
        return cookieLoginStrategy;
    }

    public final NavigatorSessionStrategy navigatorSessionStrategy() {
        return navigatorSessionStrategy;
    }

    public final UserApiStrategy userApiStrategy() {
        return userApiStrategy;
    }

    public final QuestionPresentationStrategy questionPresentationStrategy() {
        return questionPresentationStrategy;
    }

    public final CodeExecutionPresentationStrategy codeExecutionPresentationStrategy() {
        return codeExecutionPresentationStrategy;
    }

    public final NoteContentStrategy noteContentStrategy() {
        return noteContentStrategy;
    }

    public final PersistentConfig persistentConfig() {
        return ApplicationManager.getApplication().getService(persistentConfigClass);
    }

    public final ProjectConfig projectConfig(Project project) {
        return project.getService(projectConfigClass);
    }

    public final StatisticsData statisticsData(Project project) {
        return project.getService(statisticsDataClass);
    }

    public final MessageUtils messageUtils(Project project) {
        return project.getService(messageUtilsClass);
    }
}
