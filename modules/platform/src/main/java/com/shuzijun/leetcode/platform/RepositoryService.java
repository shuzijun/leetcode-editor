package com.shuzijun.leetcode.platform;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.platform.model.Graphql;
import com.shuzijun.leetcode.platform.model.HttpRequest;
import com.shuzijun.leetcode.platform.model.LeetcodeEditor;
import com.shuzijun.leetcode.platform.model.Topic;
import com.shuzijun.leetcode.platform.repository.*;
import com.shuzijun.leetcode.platform.service.HttpRequestService;
import com.shuzijun.leetcode.platform.service.URLService;
import org.jetbrains.annotations.NotNull;


public interface RepositoryService {

    boolean isPro();
    Project getProject();

    URLService getUrlService();

    HttpRequestService getHttpRequestService();

    ArticleService getArticleService();

    ConfigService getConfigService();

    FavoriteService getFavoriteService();

    FindService getFindService();

    NoteService getNoteService();

    QuestionService getQuestionService();

    SessionService getSessionService();

    SubmissionService getSubmissionService();

    ViewService getViewService();

    CodeTopService getCodeTopService();

    CodeService getCodeService();

    LeetcodeEditor getLeetcodeEditor(String path);

    <L> void subscribeNotifier(@NotNull Topic<L> topic, @NotNull L handler, @NotNull Disposable parentDisposable);

    default Graphql.GraphqlBuilder Graphql() {
        return Graphql.builder(this);
    }

    default HttpRequest.HttpRequestBuilder HttpRequest() {
        return HttpRequest.builder(getHttpRequestService());
    }
}
