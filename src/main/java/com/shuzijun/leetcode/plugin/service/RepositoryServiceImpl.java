package com.shuzijun.leetcode.plugin.service;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import com.shuzijun.leetcode.platform.RepositoryService;
import com.shuzijun.leetcode.platform.model.LeetcodeEditor;
import com.shuzijun.leetcode.platform.model.Topic;
import com.shuzijun.leetcode.platform.notifier.*;
import com.shuzijun.leetcode.platform.repository.*;
import com.shuzijun.leetcode.platform.service.HttpRequestService;
import com.shuzijun.leetcode.platform.service.URLService;
import com.shuzijun.leetcode.plugin.model.PluginTopic;
import com.shuzijun.leetcode.plugin.setting.ProjectConfig;
import org.jetbrains.annotations.NotNull;

public class RepositoryServiceImpl implements RepositoryService {

    private final Project project;
    private final URLService urlService;
    private final HttpRequestService httpRequestService;
    private final ArticleService articleService;
    private final CodeService codeService;
    private final CodeTopService codeTopService;
    private final ConfigService configService;
    private final FavoriteService favoriteService;
    private final FindService findService;
    private final NoteService noteService;
    private final QuestionService questionService;
    private final SessionService sessionService;
    private final SubmissionService submissionService;
    private final ViewService viewService;

    private final ProjectConfig projectConfig;

    public RepositoryServiceImpl(Project project) {
        this.project = project;
        this.configService = project.getService(ConfigServiceImpl.class);
        this.configService.registerRepository(this);
        this.articleService = project.getService(ArticleServiceImpl.class);
        this.articleService.registerRepository(this);
        this.codeService = project.getService(CodeServiceImpl.class);
        this.codeService.registerRepository(this);
        this.codeTopService = project.getService(CodeTopServiceImpl.class);
        this.codeTopService.registerRepository(this);
        this.favoriteService = project.getService(FavoriteServiceImpl.class);
        this.favoriteService.registerRepository(this);
        this.findService = project.getService(FindServiceImpl.class);
        this.findService.registerRepository(this);
        this.noteService = project.getService(NoteServiceImpl.class);
        this.noteService.registerRepository(this);
        this.questionService = project.getService(QuestionServiceImpl.class);
        this.questionService.registerRepository(this);
        this.sessionService = project.getService(SessionServiceImpl.class);
        this.sessionService.registerRepository(this);
        this.submissionService = project.getService(SubmissionServiceImpl.class);
        this.submissionService.registerRepository(this);
        this.viewService = project.getService(ViewServiceImpl.class);
        this.viewService.registerRepository(this);
        this.urlService = URLService.getInstance(configService);
        this.httpRequestService = HttpRequestService.getInstance(urlService);
        this.projectConfig = ProjectConfig.getInstance(project);

    }

    public static RepositoryService getInstance(Project project) {
        return project.getService(RepositoryServiceImpl.class);
    }


    @Override
    public boolean isPro() {
        return false;
    }

    @Override
    public Project getProject() {
        return project;
    }

    @Override
    public URLService getUrlService() {
        return urlService;
    }

    @Override
    public HttpRequestService getHttpRequestService() {
        return httpRequestService;
    }

    @Override
    public ArticleService getArticleService() {
        return articleService;
    }

    @Override
    public CodeService getCodeService() {
        return codeService;
    }

    @Override
    public LeetcodeEditor getLeetcodeEditor(String path) {
        return projectConfig.getEditor(path);
    }

    @Override
    public CodeTopService getCodeTopService() {
        return codeTopService;
    }

    @Override
    public ConfigService getConfigService() {
        return configService;
    }

    @Override
    public FavoriteService getFavoriteService() {
        return favoriteService;
    }

    @Override
    public FindService getFindService() {
        return findService;
    }

    @Override
    public NoteService getNoteService() {
        return noteService;
    }

    @Override
    public QuestionService getQuestionService() {
        return questionService;
    }

    @Override
    public SessionService getSessionService() {
        return sessionService;
    }

    @Override
    public SubmissionService getSubmissionService() {
        return submissionService;
    }

    @Override
    public ViewService getViewService() {
        return viewService;
    }


    @Override
    public <L> void subscribeNotifier(@NotNull Topic<L> topic, @NotNull L l, @NotNull Disposable parentDisposable) {
        MessageBusConnection messageBusConnection = ApplicationManager.getApplication().getMessageBus().connect(parentDisposable);
        if (topic == Topic.AllQuestionNotifier) {
            AllQuestionNotifier handler = (AllQuestionNotifier) l;
            messageBusConnection.subscribe(PluginTopic.ALL_QUESTION_TOPIC, handler);
        } else if (topic == Topic.ConfigNotifier) {
            ConfigNotifier handler = (ConfigNotifier) l;
            messageBusConnection.subscribe(PluginTopic.CONFIG_TOPIC, handler);
        } else if (topic == Topic.LoginNotifier) {
            LoginNotifier handler = (LoginNotifier) l;
            messageBusConnection.subscribe(PluginTopic.LOGIN_TOPIC, handler);
        } else if (topic == Topic.QuestionStatusNotifier) {
            QuestionStatusNotifier handler = (QuestionStatusNotifier) l;
            messageBusConnection.subscribe(PluginTopic.QUESTION_STATUS_TOPIC, handler);
        } else if (topic == Topic.QuestionSubmitNotifier) {
            QuestionSubmitNotifier handler = (QuestionSubmitNotifier) l;
            messageBusConnection.subscribe(PluginTopic.QUESTION_SUBMIT_TOPIC, handler);
        }

    }
}
