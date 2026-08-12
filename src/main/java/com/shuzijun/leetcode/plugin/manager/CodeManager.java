package com.shuzijun.leetcode.plugin.manager;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.shuzijun.lc.errors.LcException;
import com.shuzijun.lc.model.CodeExecutionResult;
import com.shuzijun.lc.model.CodeStartResult;
import com.shuzijun.lc.model.SubmissionDetail;
import com.shuzijun.leetcode.plugin.application.CodeExecutionCoordinator;
import com.shuzijun.leetcode.plugin.application.LeetCodeCodeService;
import com.shuzijun.leetcode.plugin.application.LeetCodeServices;
import com.shuzijun.leetcode.plugin.application.LanguageTemplateService;
import com.shuzijun.leetcode.plugin.editor.QuestionPreviewJcefWarmup;
import com.shuzijun.leetcode.plugin.editor.QuestionPreviewPerformanceTracker;
import com.shuzijun.leetcode.plugin.listener.QuestionStatusNotifier;
import com.shuzijun.leetcode.plugin.listener.QuestionSubmitNotifier;
import com.shuzijun.lc.model.CodeMetaData;
import com.shuzijun.lc.model.CodeSnippet;
import com.shuzijun.lc.model.QuestionView;
import com.shuzijun.lc.model.Session;
import com.shuzijun.lc.model.Solution;
import com.shuzijun.lc.model.Submission;
import com.shuzijun.lc.model.User;
import com.shuzijun.leetcode.plugin.model.*;
import com.shuzijun.leetcode.plugin.product.ProductProfiles;
import com.shuzijun.leetcode.plugin.product.ProductServices;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.setting.ProjectConfig;
import com.shuzijun.leetcode.plugin.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author shuzijun
 */
public class CodeManager {

    private static final int MAX_POLL_ATTEMPTS = 100;
    private static final long POLL_INTERVAL_MILLIS = 300L;
    private static final long CANCELLATION_CHECK_INTERVAL_MILLIS = 50L;

    public static void openCode(String titleSlug, Project project) {
        Config config = PersistentConfig.getInstance().getInitConfig();
        openCode(titleSlug, project, config.getCodeTypeEnum(project));
    }

    public static void openCode(String titleSlug, Project project, CodeTypeEnum codeTypeEnum) {
        QuestionPreviewPerformanceTracker tracker = QuestionPreviewPerformanceTracker.getInstance(project);
        QuestionPreviewPerformanceTracker.Trace trace = tracker.begin(titleSlug);
        QuestionPreviewJcefWarmup.request();
        openCode(titleSlug, project, codeTypeEnum, trace);
    }

    static void openCode(String titleSlug, Project project, CodeTypeEnum codeTypeEnum,
                         QuestionPreviewPerformanceTracker.Trace trace) {
        Config config = PersistentConfig.getInstance().getInitConfig();
        if (codeTypeEnum == null) {
            return;
        }

        Question question = QuestionManager.getQuestionByTitleSlug(titleSlug, project);
        if (question == null) {
            return;
        }
        trace.mark(QuestionPreviewPerformanceTracker.Milestone.QUESTION_READY);
        ProjectConfig.getInstance(project).setLastOpenedQuestionTitleSlug(question.getTitleSlug());

        if (config.isShowQuestionEditor()) {
            openContent(titleSlug, project, false, codeTypeEnum, trace);
        }

        String filePath = PersistentConfig.getInstance().getTempFilePath()
                + LanguageTemplateService.fileName(codeTypeEnum.getLangSlug(), question)
                + codeTypeEnum.getSuffix();

        File file = new File(filePath);
        BiConsumer<LeetcodeEditor, String> fillPath = (e, s) -> e.setPath(s);
        if (file.exists()) {
            FileUtils.openFileEditorAndSaveState(file, project, question, fillPath, true, codeTypeEnum);
        } else {
            String content = question.getContent();
            try{
                question.setLangSlug(codeTypeEnum.getLangSlug());
                question.setContent(CommentUtils.createComment(content, codeTypeEnum, config));
                FileUtils.saveFile(file, LanguageTemplateService.template(codeTypeEnum.getLangSlug(), question));
                FileUtils.openFileEditorAndSaveState(file, project, question, fillPath, true, codeTypeEnum);
            }finally {
                question.setContent(content);
            }

        }
    }


    public static void openContent(String titleSlug, Project project, boolean isOpen) {
        Config config = PersistentConfig.getInstance().getInitConfig();
        openContent(titleSlug, project, isOpen, config.getCodeTypeEnum(project));
    }

    public static void openContent(String titleSlug, Project project, boolean isOpen, CodeTypeEnum codeTypeEnum) {
        QuestionPreviewPerformanceTracker tracker = QuestionPreviewPerformanceTracker.getInstance(project);
        QuestionPreviewPerformanceTracker.Trace trace = tracker.begin(titleSlug);
        QuestionPreviewJcefWarmup.request();
        openContent(titleSlug, project, isOpen, codeTypeEnum, trace);
    }

    private static void openContent(String titleSlug, Project project, boolean isOpen, CodeTypeEnum codeTypeEnum,
                                    QuestionPreviewPerformanceTracker.Trace trace) {
        if (codeTypeEnum == null) {
            return;
        }
        Question question = QuestionManager.getQuestionByTitleSlug(titleSlug, project);
        if (question == null) {
            return;
        }
        trace.mark(QuestionPreviewPerformanceTracker.Milestone.QUESTION_READY);

        String filePath = PersistentConfig.getInstance().getTempFilePath()
                + Constant.DOC_CONTENT
                + LanguageTemplateService.fileName("content", question)
                + ".md";

        File file = new File(filePath);
        BiConsumer<LeetcodeEditor, String> fillPath = (e, s) -> e.setContentPath(s);
        if (file.exists()) {
            FileUtils.openFileEditorAndSaveState(file, project, question, fillPath, isOpen, codeTypeEnum);
        } else {
            FileUtils.saveFile(file, question.getContent());
            FileUtils.openFileEditorAndSaveState(file, project, question, fillPath, isOpen, codeTypeEnum);
        }
        QuestionPreviewPerformanceTracker.getInstance(project).bindContentPath(trace, file.getPath());
        trace.mark(QuestionPreviewPerformanceTracker.Milestone.CONTENT_FILE_READY);
    }


    public static void SubmitCode(String titleSlug, Project project) {
        Config config = PersistentConfig.getInstance().getInitConfig();
        SubmitCode(titleSlug, project, config.getCodeTypeEnum(project));
    }

    public static void SubmitCode(String titleSlug, Project project, CodeTypeEnum codeTypeEnum) {
        Config config = PersistentConfig.getInstance().getInitConfig();
        Question question = QuestionManager.getQuestionByTitleSlug(titleSlug, project);
        if (question == null) {
            return;
        }
        String code = getCodeText(question, config, codeTypeEnum, project);
        if (StringUtils.isBlank(code)) {
            return;
        }
        CodeExecutionCoordinator.Execution execution = coordinator(project).tryStart(
                titleSlug,
                CodeExecutionCoordinator.ExecutionType.SUBMIT
        );
        if (execution == null) {
            MessageUtils.getInstance(project).showInfoMsg("", PropertiesUtils.getInfo("request.pending"));
            return;
        }
        ProgressManager.getInstance().run(
                new SubmitCheckTask(execution, codeTypeEnum, question, project, code)
        );
    }

    public static void RunCodeCode(String titleSlug, Project project) {
        Config config = PersistentConfig.getInstance().getInitConfig();
        RunCodeCode(titleSlug, project, config.getCodeTypeEnum(project));
    }

    public static void RunCodeCode(String titleSlug, Project project, CodeTypeEnum codeTypeEnum) {
        Config config = PersistentConfig.getInstance().getInitConfig();
        Question question = QuestionManager.getQuestionByTitleSlug(titleSlug, project);
        if (question == null) {
            return;
        }
        String code = getCodeText(question, config, codeTypeEnum, project);
        if (StringUtils.isBlank(code)) {
            return;
        }
        CodeExecutionCoordinator.Execution execution = coordinator(project).tryStart(
                titleSlug,
                CodeExecutionCoordinator.ExecutionType.RUN
        );
        if (execution == null) {
            MessageUtils.getInstance(project).showWarnMsg("", "Please wait for the result.");
            return;
        }
        ProgressManager.getInstance().run(
                new RunCodeCheckTask(
                        execution,
                        project,
                        question.getTestCase(),
                        codeTypeEnum,
                        question,
                        code
                )
        );
    }

    public static boolean isExecutionActive(
            @NotNull Project project,
            @NotNull String titleSlug,
            @NotNull CodeExecutionCoordinator.ExecutionType type
    ) {
        return coordinator(project).isActive(titleSlug, type);
    }

    public static boolean cancelExecution(
            @NotNull Project project,
            @NotNull String titleSlug,
            @NotNull CodeExecutionCoordinator.ExecutionType type
    ) {
        return coordinator(project).cancel(titleSlug, type);
    }

    private static String getCodeText(Question question, Config config, CodeTypeEnum codeTypeEnum, Project project) {
        if (codeTypeEnum == null) {
            return null;
        }
        if (!LeetCodeServices.login().isLoggedIn()) {
            MessageUtils.getInstance(project).showWarnMsg("", PropertiesUtils.getInfo("login.not"));
            return null;
        }
        String filePath = PersistentConfig.getInstance().getTempFilePath()
                + LanguageTemplateService.fileName(codeTypeEnum.getLangSlug(), question)
                + codeTypeEnum.getSuffix();
        File file = new File(filePath);
        if (!file.exists()) {
            MessageUtils.getInstance(project).showWarnMsg("", PropertiesUtils.getInfo("request.code"));
            return null;
        } else {
            if (StringUtils.isBlank(question.getTestCase())) {
                return null;
            }

            String code = FileUtils.getClearCommentFileBody(file, codeTypeEnum);
            if (StringUtils.isBlank(code)) {
                MessageUtils.getInstance(project).showWarnMsg("", PropertiesUtils.getInfo("request.empty"));
                return null;
            }


            return code;
        }
    }

    private static class SubmitCheckTask extends Task.Backgroundable {

        private final CodeExecutionCoordinator.Execution execution;
        private Question question;
        private CodeTypeEnum codeTypeEnum;
        private Project project;
        private String code;

        public SubmitCheckTask(
                CodeExecutionCoordinator.Execution execution,
                CodeTypeEnum codeTypeEnum,
                Question question,
                Project project,
                String code
        ) {
            super(project, ProductProfiles.current().pluginName() + ".submitCheckTask", true);
            this.execution = execution;
            this.codeTypeEnum = codeTypeEnum;
            this.question = question;
            this.project = project;
            this.code = code;
        }

        @Override
        public void run(@NotNull ProgressIndicator progressIndicator) {
            CodeStartResult startResult;
            try {
                startResult = codeService().submit(
                        question,
                        codeTypeEnum,
                        code,
                        execution.getRequestContext()
                );
            } catch (LcException exception) {
                handleFailure(execution, progressIndicator, project, "SubmitCode error", exception);
                return;
            }
            if (startResult.getStatusCode() == 429) {
                execution.failed();
                MessageUtils.getInstance(project).showInfoMsg("", PropertiesUtils.getInfo("request.pending"));
                return;
            }
            if (startResult.getStatusCode() != 200 || StringUtils.isBlank(startResult.getId())) {
                execution.failed();
                LogUtils.LOG.error("Submit code returned no execution id for " + question.getTitleSlug());
                MessageUtils.getInstance(project).showWarnMsg("", PropertiesUtils.getInfo("request.failed"));
                return;
            }
            execution.polling();
            MessageUtils.getInstance(project).showInfoMsg("", PropertiesUtils.getInfo("request.pending"));
            for (int i = 0; i < MAX_POLL_ATTEMPTS; i++) {
                if (cancelIfRequested(execution, progressIndicator, project)) {
                    return;
                }
                try {
                    CodeExecutionResult result = codeService().submitResult(
                            startResult.getId(),
                            execution.getRequestContext()
                    );
                    if (result.isComplete()) {
                        if (result.isRunSuccess()) {
                            if (Integer.valueOf(10).equals(result.getStatusCode())) {
                                MessageUtils.getInstance(project).showInfoMsg(
                                        "",
                                        PropertiesUtils.getInfo(
                                                "submit.success",
                                                result.getStatusRuntime(),
                                                percentile(result.getRuntimePercentile()),
                                                codeTypeEnum.getType(),
                                                result.getStatusMemory(),
                                                percentile(result.getMemoryPercentile()),
                                                codeTypeEnum.getType()
                                        )
                                );
                                question.setStatus("ac");
                                notifyQuestionStatus(question);
                            } else {
                                MessageUtils.getInstance(project).showExecutionResult(
                                        "Wrong Answer",
                                        result.getInput(),
                                        result.getExpectedOutput(),
                                        result.getCodeOutput(),
                                        result.getStandardOutput(),
                                        true
                                );
                                markNotAccepted(question);
                            }
                        } else {
                            MessageUtils.getInstance(project).showExecutionFailure(
                                    result.getStatusMessage(),
                                    resolveSubmissionError(startResult.getId(), result),
                                    result.getLastTestCase(),
                                    result.getStandardOutput(),
                                    failurePrefix(question, codeTypeEnum, code)
                            );
                            markNotAccepted(question);
                        }
                        notifyQuestionSubmitted(question);
                        execution.succeeded();
                        return;
                    }
                    if (!waitForNextPoll(progressIndicator, project, execution)) {
                        execution.cancel();
                        showCancellation(project);
                        return;
                    }
                } catch (LcException exception) {
                    handleFailure(execution, progressIndicator, project, "提交出错", exception);
                    return;
                }

            }
            if (project.isDisposed()) {
                execution.cancel();
                return;
            }
            notifyQuestionSubmitted(question);
            execution.timedOut();
            MessageUtils.getInstance(project).showInfoMsg("", PropertiesUtils.getInfo("response.timeout"));
        }

        @Override
        public void onCancel() {
            execution.cancel();
        }
    }

    static String buildErrorMsg(CodeExecutionResult result) {
        String statusMsg = result.getStatusMessage();
        if (StringUtils.isNotBlank(statusMsg)) {
            if (statusMsg.equals("Compile Error")) {
                return StringUtils.defaultIfBlank(result.getFullCompileError(), statusMsg);
            } else if (statusMsg.equals("Runtime Error")) {
                return StringUtils.defaultIfBlank(result.getFullRuntimeError(), statusMsg);
            } else {
                return statusMsg;
            }
        }
        return "Unknown error";
    }

    private static String resolveSubmissionError(String submissionId, CodeExecutionResult result) {
        String error = buildErrorMsg(result);
        String status = result.getStatusMessage();
        if (!StringUtils.equals(error, status)
                || (!"Compile Error".equals(status) && !"Runtime Error".equals(status))) {
            return error;
        }
        try {
            SubmissionDetail detail = LeetCodeServices.submission().detail(submissionId);
            if ("Compile Error".equals(status)) {
                return StringUtils.defaultIfBlank(detail.getCompileError(), error);
            }
            return StringUtils.defaultIfBlank(detail.getRuntimeError(), error);
        } catch (LcException exception) {
            LogUtils.LOG.warn("Unable to load detailed submission error for " + submissionId, exception);
            return error;
        }
    }


    private static class RunCodeCheckTask extends Task.Backgroundable {
        private final CodeExecutionCoordinator.Execution execution;
        private Project project;
        private String input;
        private CodeTypeEnum codeTypeEnum;
        private Question question;
        private String code;

        public RunCodeCheckTask(
                CodeExecutionCoordinator.Execution execution,
                Project project,
                String input,
                CodeTypeEnum codeTypeEnum,
                Question question,
                String code
        ) {
            super(project, ProductProfiles.current().pluginName() + ".runCodeCheckTask", true);
            this.execution = execution;
            this.project = project;
            this.input = input;
            this.codeTypeEnum = codeTypeEnum;
            this.question = question;
            this.code = code;
        }

        @Override
        public void run(@NotNull ProgressIndicator progressIndicator) {
            CodeStartResult startResult;
            try {
                startResult = codeService().run(
                        question,
                        codeTypeEnum,
                        code,
                        execution.getRequestContext()
                );
            } catch (LcException exception) {
                handleFailure(execution, progressIndicator, project, "RunCode error", exception);
                return;
            }
            if (startResult.getStatusCode() == 429) {
                execution.failed();
                MessageUtils.getInstance(project).showWarnMsg("", "Please wait for the result.");
                return;
            }
            if (startResult.getStatusCode() != 200 || StringUtils.isBlank(startResult.getId())) {
                execution.failed();
                LogUtils.LOG.error("Run code returned no execution id for " + question.getTitleSlug());
                MessageUtils.getInstance(project).showWarnMsg("", PropertiesUtils.getInfo("request.failed"));
                return;
            }
            execution.polling();
            MessageUtils.getInstance(project).showInfoMsg("", PropertiesUtils.getInfo("request.pending"));
            String key = startResult.getExpectedId();
            if (StringUtils.isBlank(key)) {
                key = startResult.getId();
            }
            List<String> expectedAnswers = Collections.emptyList();
            for (int i = 0; i < MAX_POLL_ATTEMPTS; i++) {
                if (cancelIfRequested(execution, progressIndicator, project)) {
                    return;
                }
                try {
                    CodeExecutionResult result = codeService().runResult(
                            key,
                            execution.getRequestContext()
                    );
                    if (result.isComplete()) {
                        if (!key.equals(startResult.getId())) {
                            key = startResult.getId();
                            expectedAnswers = result.getCodeAnswers();
                        } else {
                            if (result.isRunSuccess()) {
                                String resultInput = StringUtils.defaultIfBlank(
                                        startResult.getTestCase(),
                                        input
                                );
                                String output = StringUtils.join(result.getCodeAnswers(), "\n");
                                List<String> resultExpectedAnswers = expectedAnswers.isEmpty()
                                        ? result.getExpectedCodeAnswers()
                                        : expectedAnswers;
                                String expected = StringUtils.join(resultExpectedAnswers, "\n");
                                String outputs = StringUtils.join(result.getCodeOutputs(), "\n\t\t");
                                MessageUtils.getInstance(project).showExecutionResult(
                                        "Run finished",
                                        resultInput,
                                        expected,
                                        output,
                                        outputs,
                                        !StringUtils.equals(expected, output)
                                );
                            } else {
                                MessageUtils.getInstance(project).showExecutionFailure(
                                        result.getStatusMessage(),
                                        buildErrorMsg(result),
                                        input,
                                        StringUtils.join(result.getCodeOutputs(), "\n\t\t"),
                                        failurePrefix(question, codeTypeEnum, code)
                                );
                            }
                            execution.succeeded();
                            return;
                        }
                    }
                    if (!waitForNextPoll(progressIndicator, project, execution)) {
                        execution.cancel();
                        showCancellation(project);
                        return;
                    }
                } catch (LcException exception) {
                    handleFailure(execution, progressIndicator, project, "运行出错", exception);
                    return;
                }

            }
            execution.timedOut();
            MessageUtils.getInstance(project).showWarnMsg("", PropertiesUtils.getInfo("response.timeout"));
        }

        @Override
        public void onCancel() {
            execution.cancel();
        }
    }

    static boolean waitForNextPoll(@NotNull ProgressIndicator progressIndicator) {
        return waitForNextPoll(progressIndicator, null);
    }

    static boolean waitForNextPoll(@NotNull ProgressIndicator progressIndicator, Project project) {
        return waitForNextPoll(progressIndicator, project, null);
    }

    private static boolean waitForNextPoll(
            @NotNull ProgressIndicator progressIndicator,
            Project project,
            CodeExecutionCoordinator.Execution execution
    ) {
        long deadline = System.nanoTime() + POLL_INTERVAL_MILLIS * 1_000_000L;
        while (!isCanceled(progressIndicator, project, execution)) {
            long remainingMillis = (deadline - System.nanoTime()) / 1_000_000L;
            if (remainingMillis <= 0L) {
                return true;
            }
            try {
                Thread.sleep(Math.min(remainingMillis, CANCELLATION_CHECK_INTERVAL_MILLIS));
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    private static boolean isCanceled(@NotNull ProgressIndicator progressIndicator, Project project) {
        return isCanceled(progressIndicator, project, null);
    }

    private static boolean isCanceled(
            @NotNull ProgressIndicator progressIndicator,
            Project project,
            CodeExecutionCoordinator.Execution execution
    ) {
        return progressIndicator.isCanceled()
                || project != null && project.isDisposed()
                || execution != null && execution.isCancellationRequested();
    }

    private static boolean cancelIfRequested(
            CodeExecutionCoordinator.Execution execution,
            ProgressIndicator progressIndicator,
            Project project
    ) {
        if (!isCanceled(progressIndicator, project, execution)) {
            return false;
        }
        execution.cancel();
        showCancellation(project);
        return true;
    }

    private static void handleFailure(
            CodeExecutionCoordinator.Execution execution,
            ProgressIndicator progressIndicator,
            Project project,
            String logMessage,
            LcException exception
    ) {
        if (isCanceled(progressIndicator, project, execution)) {
            execution.cancel();
            showCancellation(project);
            return;
        }
        execution.failed();
        LogUtils.LOG.error(logMessage, exception);
        MessageUtils.getInstance(project).showWarnMsg("", PropertiesUtils.getInfo("request.failed"));
    }

    private static void showCancellation(Project project) {
        if (!project.isDisposed()) {
            MessageUtils.getInstance(project).showWarnMsg("", PropertiesUtils.getInfo("request.cancel"));
        }
    }

    private static LeetCodeCodeService codeService() {
        return LeetCodeServices.code();
    }

    private static CodeExecutionCoordinator coordinator(Project project) {
        return CodeExecutionCoordinator.getInstance(project);
    }

    private static String percentile(BigDecimal value) {
        return value == null ? "" : value.setScale(2, RoundingMode.HALF_UP).toString();
    }

    private static String indent(String value) {
        return StringUtils.isBlank(value) ? value : value.replace("\n", "\n\t\t\t");
    }

    private static String failurePrefix(
            Question question,
            CodeTypeEnum codeType,
            String code
    ) {
        String prefix = ProductServices.codeExecutionPresentationStrategy()
                .failurePrefix(question, codeType, code);
        return StringUtils.isBlank(prefix) ? "" : prefix + "\n";
    }

    private static void markNotAccepted(Question question) {
        if (!"ac".equals(question.getStatus())) {
            question.setStatus("notac");
            notifyQuestionStatus(question);
        }
    }

    private static void notifyQuestionStatus(Question question) {
        ApplicationManager.getApplication().getMessageBus()
                .syncPublisher(QuestionStatusNotifier.QUESTION_STATUS_TOPIC)
                .updateTable(question);
    }

    private static void notifyQuestionSubmitted(Question question) {
        ApplicationManager.getApplication().getMessageBus()
                .syncPublisher(QuestionSubmitNotifier.TOPIC)
                .submit(URLUtils.getLeetcodeHost(), question.getTitleSlug());
    }
}
