package com.shuzijun.leetcode.plugin.application;

import com.shuzijun.lc.LcClient;
import com.shuzijun.lc.RequestContext;
import com.shuzijun.lc.errors.LcException;
import com.shuzijun.lc.model.ProblemSetParam;
import com.shuzijun.lc.model.QuestionView;
import com.shuzijun.lc.model.User;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.PageInfo;
import com.shuzijun.leetcode.plugin.model.Question;
import com.shuzijun.leetcode.plugin.model.QuestionIndex;
import com.shuzijun.leetcode.plugin.product.ProductServices;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.spi.QuestionPresentationStrategy;
import com.shuzijun.leetcode.plugin.utils.LogUtils;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class LeetCodeApiService {

    private static final ShortLivedCache<
            com.shuzijun.lc.model.PageInfo<com.shuzijun.lc.model.QuestionView>>
            QUESTION_PAGE_CACHE = new ShortLivedCache<>(30, TimeUnit.SECONDS);
    private static final ShortLivedCache<Question> QUESTION_CACHE = new ShortLivedCache<>(30);
    private static final ShortLivedCache<QuestionCatalog> QUESTION_CATALOG_CACHE =
            new ShortLivedCache<>(2, TimeUnit.DAYS);
    private static final ShortLivedCache<QuestionView> DAILY_QUESTION_CACHE =
            new ShortLivedCache<>(5, 2, TimeUnit.DAYS);

    @NotNull
    public User loadUser() {
        try {
            com.shuzijun.lc.model.User sdkUser = client().api().account().user(
                    ProductServices.userApiStrategy().queryMode(),
                    RequestContext.DEFAULT
            );
            sdkUser.setUserSlug(StringUtils.defaultIfBlank(
                    sdkUser.getUserSlug(),
                    sdkUser.getUsername()
            ));
            sdkUser.setRealName(StringUtils.defaultIfBlank(
                    sdkUser.getRealName(),
                    sdkUser.getUsername()
            ));
            return sdkUser;
        } catch (LcException exception) {
            LogUtils.LOG.error("Request user status failed", exception);
            return new User();
        }
    }

    @NotNull
    public PageInfo<QuestionView> loadQuestionPage(
            @NotNull PageInfo<QuestionView> pageInfo,
            User user
    ) throws LcException {
        String cacheIdentity = user == null ? "" : StringUtils.defaultString(user.getUsername());
        String cacheKey = questionPageCacheKey(pageInfo, cacheIdentity);
        com.shuzijun.lc.model.PageInfo<com.shuzijun.lc.model.QuestionView> sdkPage;
        try {
            sdkPage = QUESTION_PAGE_CACHE.get(cacheKey, () ->
                    client().api().questions().list(
                            toSdkProblemSetParam(pageInfo),
                            RequestContext.DEFAULT
                    )
            );
        } catch (LcException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new LcException("Failed to load question page", exception);
        }
        boolean translatedTitle = URLUtils.isCn()
                && !PersistentConfig.getInstance().getConfig().getEnglishContent();
        return applyQuestionPage(
                sdkPage,
                pageInfo,
                user != null && user.isPremium(),
                translatedTitle
        );
    }

    @NotNull
    public Question loadQuestion(@NotNull String titleSlug) throws LcException {
        String key = questionKey(URLUtils.getLeetcodeHost(), titleSlug);
        try {
            return QUESTION_CACHE.get(key, () -> loadQuestionFromApi(titleSlug));
        } catch (LcException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new LcException("Failed to load question", exception);
        }
    }

    @NotNull
    private Question loadQuestionFromApi(@NotNull String titleSlug) throws LcException {
        com.shuzijun.lc.model.Question sdkQuestion = client().api().questions().get(
                titleSlug,
                RequestContext.DEFAULT
        );
        Config config = PersistentConfig.getInstance().getConfig();
        boolean cn = URLUtils.isCn();
        boolean translatedContent = cn && !config.getEnglishContent();
        return toPluginQuestion(
                sdkQuestion,
                cn,
                translatedContent,
                config.getShowTopics(),
                ProductServices.questionPresentationStrategy()
        );
    }

    @NotNull
    public List<QuestionView> loadAllQuestions(User user) throws LcException {
        return loadAllQuestions(user, false);
    }

    @NotNull
    public List<QuestionView> loadAllQuestions(User user, boolean reset) throws LcException {
        String host = URLUtils.getLeetcodeHost();
        if (reset) {
            QUESTION_CATALOG_CACHE.invalidate(host);
        }
        try {
            return QUESTION_CATALOG_CACHE.get(host, () -> loadQuestionCatalog(user)).questions;
        } catch (LcException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new LcException("Failed to load all questions", exception);
        }
    }

    private QuestionCatalog loadQuestionCatalog(User user) throws LcException {
        boolean premium = user != null && user.isPremium();
        boolean translatedTitle = URLUtils.isCn()
                && !PersistentConfig.getInstance().getConfig().getEnglishContent();
        List<QuestionView> questions = new ArrayList<>();
        for (com.shuzijun.lc.model.QuestionView sdkQuestion
                : client().api().questions().all(RequestContext.DEFAULT)) {
            questions.add(toDisplayQuestionView(sdkQuestion, premium, translatedTitle));
        }
        Collections.sort(questions, (first, second) ->
                first.frontendQuestionIdCompareTo(second));
        return new QuestionCatalog(questions);
    }

    public QuestionIndex getQuestionIndex(@NotNull String titleSlug) {
        QuestionCatalog catalog = QUESTION_CATALOG_CACHE.getIfPresent(URLUtils.getLeetcodeHost());
        return catalog == null ? null : catalog.position(titleSlug);
    }

    public Question getCachedQuestion(@NotNull String titleSlug, @NotNull String host) {
        return QUESTION_CACHE.getIfPresent(questionKey(host, titleSlug));
    }

    public QuestionView loadQuestionOfToday() throws LcException {
        String key = dailyQuestionKey(URLUtils.getLeetcodeHost());
        try {
            return DAILY_QUESTION_CACHE.get(key, this::loadQuestionOfTodayFromApi);
        } catch (LcException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new LcException("Failed to load the daily question", exception);
        }
    }

    private QuestionView loadQuestionOfTodayFromApi() throws LcException {
        com.shuzijun.lc.model.Question sdkQuestion = client().api().questions()
                .today(RequestContext.DEFAULT);
        if (sdkQuestion == null) {
            return null;
        }
        boolean translatedTitle = URLUtils.isCn()
                && !PersistentConfig.getInstance().getConfig().getEnglishContent();
        QuestionView question = toDisplayQuestionView(sdkQuestion, true, translatedTitle);
        question.setStatus("day");
        return question;
    }

    public String pickQuestion(PageInfo<?> pageInfo) throws LcException {
        return client().api().questions().random(
                toSdkProblemSetParam(pageInfo),
                RequestContext.DEFAULT
        );
    }

    public static void invalidateQuestionPageCache() {
        QUESTION_PAGE_CACHE.invalidateAll();
    }

    public static void invalidateQuestionPageCache(String host) {
        if (StringUtils.isBlank(host)) {
            return;
        }
        QUESTION_PAGE_CACHE.invalidateMatching(key -> key.startsWith(host + "\n"));
    }

    public static void invalidateCaches() {
        QUESTION_PAGE_CACHE.invalidateAll();
        QUESTION_CACHE.invalidateAll();
        QUESTION_CATALOG_CACHE.invalidateAll();
        DAILY_QUESTION_CACHE.invalidateAll();
    }

    public static void invalidateCaches(String host) {
        if (StringUtils.isBlank(host)) {
            return;
        }
        String hostPrefix = host + "\n";
        QUESTION_PAGE_CACHE.invalidateMatching(key -> key.startsWith(hostPrefix));
        QUESTION_CACHE.invalidateMatching(key -> key.startsWith(hostPrefix));
        QUESTION_CATALOG_CACHE.invalidate(host);
        DAILY_QUESTION_CACHE.invalidateMatching(key -> key.startsWith(hostPrefix));
    }

    @NotNull
    static ProblemSetParam toSdkProblemSetParam(PageInfo<?> pageInfo) {
        ProblemSetParam param = new ProblemSetParam(pageInfo.getPageIndex(), pageInfo.getPageSize());
        param.setCategorySlug(pageInfo.getCategorySlug());
        ProblemSetParam.Filters sdkFilters = param.getFilters();
        PageInfo.Filters filters = pageInfo.getFilters();
        sdkFilters.setSearchKeywords(filters.getSearchKeywords());
        sdkFilters.setOrderBy(filters.getOrderBy());
        sdkFilters.setSortOrder(filters.getSortOrder());
        sdkFilters.setDifficulty(filters.getDifficulty());
        sdkFilters.setStatus(filters.getStatus());
        sdkFilters.setListId(filters.getListId());
        sdkFilters.setTags(filters.getTags());
        return param;
    }

    @NotNull
    static PageInfo<QuestionView> applyQuestionPage(
            com.shuzijun.lc.model.PageInfo<com.shuzijun.lc.model.QuestionView> sdkPage,
            PageInfo<QuestionView> target,
            boolean premium,
            boolean translatedTitle
    ) {
        List<QuestionView> questions = new ArrayList<>();
        for (com.shuzijun.lc.model.QuestionView sdkQuestion : sdkPage.getRows()) {
            questions.add(toDisplayQuestionView(sdkQuestion, premium, translatedTitle));
        }
        target.setRowTotal(sdkPage.getRowTotal());
        target.setRows(questions);
        return target;
    }

    @NotNull
    static QuestionView toDisplayQuestionView(
            com.shuzijun.lc.model.QuestionView sdkQuestion,
            boolean premium,
            boolean translatedTitle
    ) {
        QuestionView question = sdkQuestion.copy();
        question.setTitle(translatedTitle
                ? StringUtils.defaultIfBlank(sdkQuestion.getTitleCn(), sdkQuestion.getTitle())
                : sdkQuestion.getTitle());
        if (sdkQuestion.isPaidOnly() && !premium) {
            question.setStatus("lock");
        }
        return question;
    }

    @NotNull
    static Question toPluginQuestion(
            com.shuzijun.lc.model.Question sdkQuestion,
            boolean cn,
            boolean translatedContent,
            boolean showTopics,
            QuestionPresentationStrategy presentationStrategy
    ) {
        Question question = new Question();
        question.setQuestionId(sdkQuestion.getQuestionId());
        question.setFrontendQuestionId(sdkQuestion.getFrontendQuestionId());
        question.setTitle(translatedContent
                ? StringUtils.defaultIfBlank(sdkQuestion.getTitleCn(), sdkQuestion.getTitle())
                : sdkQuestion.getTitle());
        question.setTitleSlug(sdkQuestion.getTitleSlug());
        question.setLevel(String.valueOf(sdkQuestion.getLevel()));
        question.setStatus(sdkQuestion.getStatus());
        question.setCategory(sdkQuestion.getCategory());
        question.setContent(presentationStrategy.renderContent(
                sdkQuestion,
                translatedContent,
                showTopics
        ));
        question.setTestCase(sdkQuestion.getTestCase());
        question.setExampleTestcases(sdkQuestion.getExampleTestcases());
        question.setCodeSnippets(sdkQuestion.getCodeSnippets());
        question.setSimilarQuestions(sdkQuestion.getSimilarQuestions());
        question.setHints(sdkQuestion.getHints());
        question.setCodeMetaData(sdkQuestion.getCodeMetaData());
        question.setArticleLive(Constant.ARTICLE_LIVE_LIST);
        return question;
    }

    @NotNull
    private static String questionPageCacheKey(PageInfo<?> pageInfo, String cacheIdentity) {
        return URLUtils.getLeetcodeHost()
                + "\n" + cacheIdentity
                + "\n" + pageInfo.getPageIndex()
                + "\n" + pageInfo.getPageSize()
                + "\n" + pageInfo.getCategorySlug()
                + "\n" + pageInfo.getFilters();
    }

    @NotNull
    static String questionKey(String host, String titleSlug) {
        return StringUtils.defaultString(host) + "\n" + StringUtils.defaultString(titleSlug);
    }

    @NotNull
    static String dailyQuestionKey(String host) {
        return StringUtils.defaultString(host) + "\n"
                + new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
    }

    @NotNull
    private LcClient client() {
        return LcClientFactory.create();
    }

    private static final class QuestionCatalog {
        private final List<QuestionView> questions;
        private final Map<String, Integer> indexes;

        private QuestionCatalog(List<QuestionView> questions) {
            this.questions = Collections.unmodifiableList(new ArrayList<>(questions));
            Map<String, Integer> indexBySlug = new HashMap<>();
            for (int index = 0; index < questions.size(); index++) {
                indexBySlug.put(questions.get(index).getTitleSlug(), index);
            }
            this.indexes = Collections.unmodifiableMap(indexBySlug);
        }

        private QuestionIndex position(String titleSlug) {
            Integer index = indexes.get(titleSlug);
            if (index == null) {
                return null;
            }
            QuestionIndex position = new QuestionIndex();
            position.setIndex(index);
            position.setQuestionView(questions.get(index));
            return position;
        }
    }
}
