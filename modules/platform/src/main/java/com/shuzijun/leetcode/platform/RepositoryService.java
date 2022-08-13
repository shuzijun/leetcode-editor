package com.shuzijun.leetcode.platform;

import com.intellij.openapi.project.Project;
import com.shuzijun.leetcode.platform.repository.*;
import com.shuzijun.leetcode.platform.service.HttpRequestService;
import com.shuzijun.leetcode.platform.service.URLService;
import com.shuzijun.leetcode.plugin.model.Graphql;
import com.shuzijun.leetcode.plugin.model.HttpRequest;


public interface RepositoryService {

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

    default Graphql.GraphqlBuilder Graphql() {
        return Graphql.builder(this);
    }
    default HttpRequest.HttpRequestBuilder HttpRequest(){
        return HttpRequest.builder(getHttpRequestService());
    }
}
