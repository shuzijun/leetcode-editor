package com.shuzijun.leetcode.plugin

import com.intellij.ide.starter.driver.engine.runIdeWithDriver
import com.intellij.ide.starter.models.IdeInfo
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.plugins.PluginConfigurator
import com.intellij.ide.starter.project.LocalProjectInfo
import com.intellij.ide.starter.runner.Starter
import com.intellij.driver.client.Driver
import com.intellij.driver.client.Remote
import com.intellij.driver.client.service
import com.intellij.driver.model.OnDispatcher
import com.intellij.driver.sdk.ActionManager
import com.intellij.driver.sdk.DumbService
import com.intellij.driver.sdk.FileEditorManager
import com.intellij.driver.sdk.getToolWindow
import com.intellij.driver.sdk.invokeActionWithRetries
import com.intellij.driver.sdk.openEditor
import com.intellij.driver.sdk.openFile
import com.intellij.driver.sdk.openToolWindow
import com.intellij.driver.sdk.singleProject
import com.intellij.driver.sdk.waitForProjectOpen
import com.intellij.driver.sdk.ui.components.elements.table
import com.intellij.driver.sdk.ui.components.UiComponent.Companion.waitFound
import com.intellij.driver.sdk.ui.ui
import com.intellij.tools.ide.starter.product.idea.ultimate.IdeaUltimate
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.OutputStream
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.swing.JTable
import javax.swing.JTabbedPane
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import kotlin.time.Duration.Companion.seconds

@Remote("javax.swing.JTabbedPane")
private interface JTabbedPaneRef {
    fun getTabCount(): Int
    fun getTitleAt(index: Int): String
    fun getSelectedIndex(): Int
}

@Remote("com.intellij.ide.ui.UISettings")
private interface UiSettingsRef {
    fun getEditorTabLimit(): Int
    fun setEditorTabLimit(limit: Int)
}

@Remote("com.intellij.openapi.wm.ToolWindow")
private interface ActivatableToolWindowRef {
    fun activate(runnable: Runnable?)
}

private data class RestoredEditorEntry(
    val codeRelativePath: String,
    val codePath: String,
    val contentPath: String,
    val questionId: Int,
)

class LeetCodeEditorStartupIntegrationTest {

    @Test
    fun restoresManyLeetCodeTabsWithoutStartupNetworkCalls(@TempDir tempDir: Path) {
        listOf(10).forEach { tabCount ->
            val projectDir = tempDir.resolve("restored-tabs-$tabCount")
            val editorEntries = createRestoredLeetCodeTabsProject(projectDir, tabCount)

            LocalGraphqlServer(responseDelayMillis = 5_000L).use { graphqlServer ->
                val prepareContext = Starter.newContext(
                    testName = "leetcode-editor-prepare-tabs-$tabCount-${UUID.randomUUID()}",
                    TestCase(
                        IdeInfo.IdeaUltimate,
                        projectInfo = LocalProjectInfo(projectDir),
                    ),
                ).apply {
                    PluginConfigurator(this).installPluginFromPath(Path.of(System.getProperty("path.to.build.plugin")))
                    this.applyVMOptionsPatch {
                        addSystemProperty("user.language", "en")
                        addSystemProperty("user.country", "US")
                        addSystemProperty("sun.java2d.metal", "false")
                        addSystemProperty("sun.java2d.opengl", "false")
                        addSystemProperty("leetcode.test.base.url", graphqlServer.baseUrl)
                    }
                    writeTestConfiguration(paths.configDir, projectDir)
                }

                prepareContext.runIdeWithDriver().useDriverAndCloseIde {
                    waitForProjectOpen()
                    val uiSettings = service(UiSettingsRef::class)
                    uiSettings.setEditorTabLimit(128)
                    assertTrue(
                        uiSettings.getEditorTabLimit() == 128,
                        "The pressure test must keep all requested editor tabs open",
                    )
                    val restoredEditorManager = service(FileEditorManager::class, singleProject())
                    editorEntries.forEach { entry ->
                        openFile(entry.codeRelativePath, waitForCodeAnalysis = false)
                    }
                    waitUntil("$tabCount LeetCode tabs are opened before restart") {
                        restoredLeetCodeFileCount(restoredEditorManager) == tabCount
                    }
                    assertTrue(
                        graphqlServer.totalRequestCount() == 0,
                        "Opening local LeetCode editor tabs must not request LeetCode data",
                    )
                }

                val restartedAt = System.nanoTime()
                prepareContext.runIdeWithDriver().useDriverAndCloseIde {
                    waitForProjectOpen()
                    val restoredEditorManager = service(FileEditorManager::class, singleProject())
                    waitUntilWithDiagnostics(tabCount, "$tabCount persisted LeetCode editor tabs are restored") {
                        restoredLeetCodeFilePaths(restoredEditorManager)
                    }
                    assertConvergeEditorUi("the restored $tabCount-tab workspace uses ConvergeEditor")
                    assertTrue(
                        graphqlServer.totalRequestCount() == 0,
                        "Restoring $tabCount LeetCode tabs must not request LeetCode data during restart",
                    )
                    val restartElapsedMillis = (System.nanoTime() - restartedAt) / 1_000_000L
                    println(
                        "MULTI_TAB_RESTART_METRIC tabs=$tabCount " +
                            "restartMillis=$restartElapsedMillis " +
                            "restoredEditors=${restoredEditorManager.getAllEditors().size} " +
                            "networkRequests=${graphqlServer.totalRequestCount()}",
                    )
                }
            }
        }
    }

    @Test
    fun remainsInteractiveAtStartupWhenLeetCodeResponsesAreSlow(@TempDir tempDir: Path) {
        LocalGraphqlServer(responseDelayMillis = 5_000L).use { graphqlServer ->
            val testContext = Starter.newContext(
                testName = "leetcode-editor-slow-network-startup-${UUID.randomUUID()}",
                TestCase(
                    IdeInfo.IdeaUltimate,
                    projectInfo = LocalProjectInfo(
                        Paths.get(
                            checkNotNull(javaClass.classLoader.getResource("ui-project")) {
                                "Missing ui-project integration test resource"
                            }.toURI()
                        )
                    )
                ),
            ).apply {
                PluginConfigurator(this).installPluginFromPath(Path.of(System.getProperty("path.to.build.plugin")))
                this.applyVMOptionsPatch {
                    addSystemProperty("user.language", "en")
                    addSystemProperty("user.country", "US")
                    addSystemProperty("sun.java2d.metal", "false")
                    addSystemProperty("sun.java2d.opengl", "false")
                    addSystemProperty("leetcode.test.base.url", graphqlServer.baseUrl)
                }
                writeTestConfiguration(paths.configDir, tempDir)
            }

            testContext.runIdeWithDriver().useDriverAndCloseIde {
                waitForProjectOpen()
                assertTrue(
                    graphqlServer.totalRequestCount() == 0,
                    "Project startup must not eagerly load LeetCode data",
                )

                val startedAt = System.nanoTime()
                openToolWindow("Leetcode")
                val openElapsedMillis = (System.nanoTime() - startedAt) / 1_000_000L
                assertTrue(getToolWindow("Leetcode").isVisible())
                assertTrue(
                    openElapsedMillis < graphqlServer.responseDelayMillis,
                    "Opening the tool window must not synchronously wait for LeetCode responses",
                )
                waitUntil("the delayed user request reaches the local server") {
                    graphqlServer.totalRequestCount() > 0
                }
            }
        }
    }

    @Test
    fun startsIdeaWithLeetCodeEditorInstalled(@TempDir tempDir: Path) {
        LocalGraphqlServer().use { graphqlServer ->
            val testContext = Starter.newContext(
                testName = "leetcode-editor-startup-${UUID.randomUUID()}",
                TestCase(
                    IdeInfo.IdeaUltimate,
                    projectInfo = LocalProjectInfo(
                        Paths.get(
                            checkNotNull(javaClass.classLoader.getResource("ui-project")) {
                                "Missing ui-project integration test resource"
                            }.toURI()
                        )
                    )
                ),
            ).apply {
                PluginConfigurator(this).installPluginFromPath(Path.of(System.getProperty("path.to.build.plugin")))
                this.applyVMOptionsPatch {
                    addSystemProperty("user.language", "en")
                    addSystemProperty("user.country", "US")
                    addSystemProperty("sun.java2d.metal", "false")
                    addSystemProperty("sun.java2d.opengl", "false")
                    addSystemProperty("leetcode.test.base.url", graphqlServer.baseUrl)
                }
                writeTestConfiguration(paths.configDir, tempDir)
            }

            testContext.runIdeWithDriver().useDriverAndCloseIde {
                waitForProjectOpen()
                waitUntil("the project finishes indexing") {
                    !service(DumbService::class, singleProject()).isDumb()
                }
                openToolWindow("Leetcode")
                assertTrue(getToolWindow("Leetcode").isVisible())
                assertPluginActionsRegistered()
                val questionTable = ui.table { byType(JTable::class.java) }.waitFound(120.seconds)
                invokeActionWithRetries("leetcode.RefreshAction")
                waitUntil("the refresh request reaches the local GraphQL server") {
                    graphqlServer.requestCount("problemsetQuestionList") > 0
                }
                openToolWindow("Leetcode")

                waitUntil("the question table displays mocked questions") {
                    questionTable.rowCount() == 51 &&
                        questionTable.content().values.asSequence()
                            .flatMap { it.values.asSequence() }
                            .any { it.contains("两数之和") } &&
                        questionTable.content().values.asSequence()
                            .flatMap { it.values.asSequence() }
                            .any { it.contains("两数相加") }
                }
                println("STEP_SCREENSHOT[01-default-list-page-1]=${takeScreenshot("01-default-list-page-1")}")
                val defaultRequestCount = graphqlServer.requestCount("problemsetQuestionList")
                invokeActionWithRetries("leetcode.sort.SortByTitle")
                waitUntil("sorting sends the selected order to the question API") {
                    graphqlServer.requestCount("problemsetQuestionList") > defaultRequestCount &&
                        graphqlServer.lastRequest("problemsetQuestionList").contains("\"orderBy\":\"FRONTEND_ID\"")
                }
                waitUntil("the sorted table is ready for keyboard selection") {
                    questionTable.rowCount() == 51 &&
                        questionTable.content().values.asSequence()
                            .flatMap { it.values.asSequence() }
                            .any { it.contains("两数之和") }
                }
                exerciseDefaultListActions(graphqlServer)
                waitUntil("opening a question requests its content") {
                    if (graphqlServer.requestCount("questionData") == 0) {
                        questionTable.doubleClickCell(1, 1)
                    }
                    graphqlServer.requestCount("questionData") > 0
                }
                val generatedCode = tempDir.resolve("leetcode/editor/cn/[1]两数之和.java")
                waitUntil("the generated code is ready for startup verification") {
                    Files.isRegularFile(generatedCode)
                }
                selectOpenEditor(generatedCode, stableMillis = 3_000)
                assertConvergeEditorUi("the default ConvergeEditor UI is displayed")
                println("STEP_SCREENSHOT[02-question-opened]=${takeScreenshot("02-question-opened")}")
                val requestCountBeforePaging = graphqlServer.requestCount("problemsetQuestionList")
                invokeActionWithRetries("leetcode.NextPage")
                waitUntil("the second page is fully rendered before positioning") {
                    graphqlServer.requestCount("problemsetQuestionList") > requestCountBeforePaging &&
                        graphqlServer.lastRequest("problemsetQuestionList").contains("\"skip\":50") &&
                        questionTable.rowCount() == 51 &&
                        questionTable.content().values.asSequence()
                            .flatMap { it.values.asSequence() }
                            .any { it.contains("最大子数组和") }
                }
                println("STEP_SCREENSHOT[03-page-2-loaded]=${takeScreenshot("03-page-2-loaded")}")
                invokeActionWithRetries("leetcode.positionAction")
                waitUntil("positioning restores the first full question page instead of only the daily question") {
                    questionTable.rowCount() == 51 &&
                        questionTable.content().values.asSequence()
                            .flatMap { it.values.asSequence() }
                            .any { it.contains("两数之和") } &&
                        questionTable.content().values.asSequence()
                            .flatMap { it.values.asSequence() }
                            .none { it.contains("最大子数组和") }
                }
                println("STEP_SCREENSHOT[04-after-position]=${takeScreenshot("04-after-position")}")
                invokeActionWithRetries("leetcode.ToggleListAction")
                invokeActionWithRetries("leetcode.RefreshAction")
                waitUntil("the all-questions view displays translated question data") {
                    ui.table { byType(JTable::class.java) }.content().values.asSequence()
                        .flatMap { it.values.asSequence() }
                        .any { it.contains("翻译题目4") }
                }
                invokeActionWithRetries("leetcode.ToggleListAction")
                invokeActionWithRetries("leetcode.RefreshAction")
                waitUntil("the CodeTop view displays mocked questions") {
                    graphqlServer.requestCountForPath("/api/questions/") > 0 &&
                        ui.table { byType(JTable::class.java) }.content().values.asSequence()
                            .flatMap { it.values.asSequence() }
                            .any { it.contains("CodeTop Two Sum") }
                }
                invokeActionWithRetries("leetcode.ToggleListAction")
                invokeActionWithRetries("leetcode.RefreshAction")
                waitUntil("the default question view is restored") {
                    ui.table { byType(JTable::class.java) }.content().values.asSequence()
                        .flatMap { it.values.asSequence() }
                        .any { it.contains("两数相加") }
                }
                Unit
            }
        }
    }

    @Test
    fun startsWithEnglishContentNonConvergeConfiguration(@TempDir tempDir: Path) {
        LocalGraphqlServer().use { graphqlServer ->
            val testContext = Starter.newContext(
                testName = "leetcode-editor-non-converge-${UUID.randomUUID()}",
                TestCase(
                    IdeInfo.IdeaUltimate,
                    projectInfo = LocalProjectInfo(
                        Paths.get(
                            checkNotNull(javaClass.classLoader.getResource("ui-project")) {
                                "Missing ui-project integration test resource"
                            }.toURI()
                        )
                    )
                ),
            ).apply {
                PluginConfigurator(this).installPluginFromPath(Path.of(System.getProperty("path.to.build.plugin")))
                this.applyVMOptionsPatch {
                    addSystemProperty("user.language", "en")
                    addSystemProperty("user.country", "US")
                    addSystemProperty("sun.java2d.metal", "false")
                    addSystemProperty("sun.java2d.opengl", "false")
                    addSystemProperty("leetcode.test.base.url", graphqlServer.baseUrl)
                }
                writeTestConfiguration(
                    configDir = paths.configDir,
                    tempDir = tempDir,
                    codeType = "Kotlin",
                    convergeEditor = false,
                    englishContent = true,
                    questionEditor = "Right",
                    url = "leetcode.cn",
                    showQuestionEditorSign = false,
                )
            }

            testContext.runIdeWithDriver().useDriverAndCloseIde {
                waitForProjectOpen()
                waitUntil("the project finishes indexing") {
                    !service(DumbService::class, singleProject()).isDumb()
                }
                openToolWindow("Leetcode")
                assertPluginActionsRegistered()
                val questionTable = ui.table { byType(JTable::class.java) }.waitFound(120.seconds)
                invokeActionWithRetries("leetcode.RefreshAction")
                waitUntil("the English configuration loads the question list") {
                    graphqlServer.requestCount("problemsetQuestionList") > 0 &&
                        questionTable.rowCount() == 51 &&
                        questionTable.content().values.asSequence()
                            .flatMap { it.values.asSequence() }
                            .any { it.contains("Two Sum") }
                }
                waitUntil("the non-converge configuration loads question content") {
                    if (graphqlServer.requestCount("questionData") == 0) {
                        questionTable.doubleClickCell(1, 1)
                    }
                    graphqlServer.requestCount("questionData") > 0
                }
                waitUntil("the non-converge configuration does not create four converge tabs") {
                    val tabs = ui.x { byType(JTabbedPane::class.java) }
                    !tabs.present() || tabs.driver.cast(tabs.component, JTabbedPaneRef::class)
                        .let { tabbedPane ->
                            (0 until tabbedPane.getTabCount()).map(tabbedPane::getTitleAt) !=
                                listOf("Content", "Solution", "Submissions", "Note")
                        }
                }
                println("STEP_SCREENSHOT[05-english-non-converge]=${takeScreenshot("05-english-non-converge")}")
                Unit
            }
        }
    }

    @Test
    fun generatesCodeFromCustomTemplate(@TempDir tempDir: Path) {
        LocalGraphqlServer().use { graphqlServer ->
            val testContext = Starter.newContext(
                testName = "leetcode-editor-custom-template-${UUID.randomUUID()}",
                TestCase(
                    IdeInfo.IdeaUltimate,
                    projectInfo = LocalProjectInfo(
                        Paths.get(
                            checkNotNull(javaClass.classLoader.getResource("ui-project")) {
                                "Missing ui-project integration test resource"
                            }.toURI()
                        )
                    )
                ),
            ).apply {
                PluginConfigurator(this).installPluginFromPath(Path.of(System.getProperty("path.to.build.plugin")))
                this.applyVMOptionsPatch {
                    addSystemProperty("user.language", "en")
                    addSystemProperty("user.country", "US")
                    addSystemProperty("sun.java2d.metal", "false")
                    addSystemProperty("sun.java2d.opengl", "false")
                    addSystemProperty("leetcode.test.base.url", graphqlServer.baseUrl)
                }
                writeTestConfiguration(
                    configDir = paths.configDir,
                    tempDir = tempDir,
                    customCode = true,
                    customFileName = "\$!velocityTool.camelCaseName(\${question.titleSlug})",
                    customTemplate = """
                        ${'$'}{question.content}

                        package com.shuzijun.leetcode.editor.en;
                        public class ${'$'}!velocityTool.camelCaseName(${'$'}{question.titleSlug}) {
                            ${'$'}{question.code}
                        }
                    """.trimIndent(),
                )
            }

            testContext.runIdeWithDriver().useDriverAndCloseIde {
                waitForProjectOpen()
                waitUntil("the project finishes indexing") {
                    !service(DumbService::class, singleProject()).isDumb()
                }
                openToolWindow("Leetcode")
                val questionTable = ui.table { byType(JTable::class.java) }.waitFound(120.seconds)
                invokeActionWithRetries("leetcode.RefreshAction")
                waitUntil("the custom-template scenario displays mocked questions") {
                    questionTable.rowCount() == 51
                }
                waitUntil("opening a question requests content for the custom template") {
                    if (graphqlServer.requestCount("questionData") == 0) {
                        questionTable.doubleClickCell(1, 1)
                    }
                    graphqlServer.requestCount("questionData") > 0
                }

                val generatedFile = tempDir.resolve("leetcode/editor/cn/TwoSum.java")
                waitUntil("the custom template generates the expected Java source file") {
                    Files.isRegularFile(generatedFile)
                }
                val generatedSource = Files.readString(generatedFile)
                assertTrue(generatedSource.contains("package com.shuzijun.leetcode.editor.en;"))
                assertTrue(generatedSource.contains("public class TwoSum"))
                assertTrue(generatedSource.contains("给定一个整数数组，返回两个下标。"))
                assertTrue(generatedSource.contains("//leetcode submit region begin(Prohibit modification and deletion)"))
                assertTrue(generatedSource.contains("//leetcode submit region end(Prohibit modification and deletion)"))
                assertTrue(generatedSource.contains("class Solution"))
                assertConvergeEditorUi("the custom-template ConvergeEditor UI is displayed")
                println("STEP_SCREENSHOT[06-custom-template]=${takeScreenshot("06-custom-template")}")
                Unit
            }
        }
    }

    @Test
    fun exercisesEditorActionsWithOnlyLocalSideEffectMocks(@TempDir tempDir: Path) {
        LocalGraphqlServer().use { graphqlServer ->
            val browserCapture = tempDir.resolve("browser-url.txt")
            val testContext = Starter.newContext(
                testName = "leetcode-editor-actions-${UUID.randomUUID()}",
                TestCase(
                    IdeInfo.IdeaUltimate,
                    projectInfo = LocalProjectInfo(
                        Paths.get(
                            checkNotNull(javaClass.classLoader.getResource("ui-project")) {
                                "Missing ui-project integration test resource"
                            }.toURI()
                        )
                    )
                ),
            ).apply {
                PluginConfigurator(this).installPluginFromPath(Path.of(System.getProperty("path.to.build.plugin")))
                this.applyVMOptionsPatch {
                    addSystemProperty("user.language", "en")
                    addSystemProperty("user.country", "US")
                    addSystemProperty("sun.java2d.metal", "false")
                    addSystemProperty("sun.java2d.opengl", "false")
                    addSystemProperty("leetcode.test.base.url", graphqlServer.baseUrl)
                    addSystemProperty("leetcode.test.browser.capture.file", browserCapture.toString())
                }
                writeTestConfiguration(paths.configDir, tempDir)
            }

            testContext.runIdeWithDriver().useDriverAndCloseIde {
                waitForProjectOpen()
                waitUntil("the project finishes indexing") {
                    !service(DumbService::class, singleProject()).isDumb()
                }
                openToolWindow("Leetcode")
                val questionTable = ui.table { byType(JTable::class.java) }.waitFound(120.seconds)
                invokeActionWithRetries("leetcode.RefreshAction")
                waitUntil("the editor action scenario displays mocked questions") {
                    questionTable.rowCount() == 51
                }
                waitUntil("the code editor is opened for editor actions") {
                    if (graphqlServer.requestCount("questionData") == 0) {
                        questionTable.doubleClickCell(1, 1)
                    }
                    graphqlServer.requestCount("questionData") > 0
                }

                val generatedCode = tempDir.resolve("leetcode/editor/cn/[1]两数之和.java")
                waitUntil("the generated code is ready for run and submit") {
                    Files.isRegularFile(generatedCode)
                }
                assertConvergeEditorUi("the editor action ConvergeEditor UI is displayed")

                invokeEditorAction(generatedCode, "leetcode.editor.OpenContentAction")
                selectOpenEditor(generatedCode)
                assertConvergeEditorTabSelected("Content")

                val runRequests = graphqlServer.requestCountForPath("/problems/two-sum/interpret_solution/")
                invokeEditorAction(generatedCode, "leetcode.editor.RunCodeAction")
                waitUntil("run code posts only to the local mock and receives a local result") {
                    graphqlServer.requestCountForPath("/problems/two-sum/interpret_solution/") > runRequests &&
                        graphqlServer.requestCountForPath("/submissions/detail/run-1/check/") > 0
                }
                assertTrue(
                    graphqlServer.lastRequestForPath("/problems/two-sum/interpret_solution/").contains("\"judge_type\":\"large\""),
                )

                val submitRequests = graphqlServer.requestCountForPath("/problems/two-sum/submit/")
                invokeEditorAction(generatedCode, "leetcode.editor.SubmitAction")
                waitUntil("submit posts only to the local mock and receives a local result") {
                    graphqlServer.requestCountForPath("/problems/two-sum/submit/") > submitRequests &&
                        graphqlServer.requestCountForPath("/submissions/detail/submit-1/check/") > 0
                }
                assertTrue(
                    graphqlServer.lastRequestForPath("/problems/two-sum/submit/").contains("\"typed_code\""),
                )

                invokeEditorAction(generatedCode, "leetcode.editor.OpenInWebAction")
                waitUntil("open in web is captured instead of launching a browser") {
                    Files.isRegularFile(browserCapture)
                }
                assertTrue(Files.readString(browserCapture).endsWith("/problems/two-sum"))

                val getNoteRequests = graphqlServer.requestCount("getNote")
                invokeEditorAction(generatedCode, "leetcode.editor.PullNote")
                val noteFile = tempDir.resolve("leetcode/editor/cn/doc/note/[1]两数之和.md")
                waitUntil("pull note retrieves the note only from the local GraphQL mock") {
                    graphqlServer.requestCount("getNote") > getNoteRequests &&
                        Files.isRegularFile(noteFile) &&
                        Files.readString(noteFile) == "mock note from local server"
                }

                invokeEditorAction(generatedCode, "leetcode.editor.ShowNote")
                assertConvergeEditorTabSelected("Note")

                val updateNoteRequests = graphqlServer.requestCount("updateNote")
                invokeEditorAction(generatedCode, "leetcode.editor.PushNote")
                waitUntil("push note posts only to the local GraphQL mock") {
                    graphqlServer.requestCount("updateNote") > updateNoteRequests &&
                        graphqlServer.lastRequest("updateNote").contains("\"titleSlug\":\"two-sum\"")
                }
                assertConsoleOutputUi()
                println("STEP_SCREENSHOT[07-editor-actions-local-mocks]=${takeScreenshot("07-editor-actions-local-mocks")}")
                Unit
            }
        }
    }

    private fun Driver.invokeEditorAction(path: Path, actionId: String) {
        selectOpenEditor(path)
        invokeActionWithRetries(actionId)
    }

    private fun Driver.selectOpenEditor(path: Path, stableMillis: Long = 0L) {
        val fileEditorManager = service(FileEditorManager::class, singleProject())
        var file = fileEditorManager.getAllEditors().firstOrNull {
            Path.of(it.getFile().getPath()) == path
        }?.getFile()
        waitUntil("$path appears in the open editors") {
            file = fileEditorManager.getAllEditors().firstOrNull {
                Path.of(it.getFile().getPath()) == path
            }?.getFile()
            file != null
        }
        val openFile = checkNotNull(file) { "Expected an open editor for $path" }
        openEditor(openFile)
        var selectedSince = 0L
        waitUntil("$path is the selected editor for $stableMillis ms") {
            if (Path.of(fileEditorManager.getCurrentFile().getPath()) == path) {
                if (selectedSince == 0L) {
                    selectedSince = System.nanoTime()
                }
                (System.nanoTime() - selectedSince) / 1_000_000L >= stableMillis
            } else {
                selectedSince = 0L
                openEditor(openFile)
                false
            }
        }
    }

    private fun Driver.assertConsoleOutputUi() {
        val consoleToolWindow = getToolWindow("Leetcode Console")
        openToolWindow("Leetcode Console")
        val activatableConsoleToolWindow = cast(consoleToolWindow, ActivatableToolWindowRef::class)
        withContext(OnDispatcher.EDT) {
            activatableConsoleToolWindow.activate(null)
        }
        assertTrue(consoleToolWindow.isVisible(), "The Console tool window must be visible")
        val consolePanel = ui.x {
            componentWithChild(
                byType("com.intellij.toolWindow.InternalDecoratorImpl"),
                byType("com.shuzijun.leetcode.plugin.window.ConsolePanel"),
            )
        }.waitFound()
        consolePanel.waitAnyTextsContains("Code submitted. Please wait...")
        consolePanel.waitAnyTextsContains("Success:")
        println("STEP_SCREENSHOT[08-console-run-submit-output]=${takeScreenshot("08-console-run-submit-output")}")
    }

    private fun Driver.exerciseDefaultListActions(graphqlServer: LocalGraphqlServer) {
        listOf(
            "leetcode.FindAction",
            "leetcode.FindAction",
            "leetcode.find.Clear",
            "leetcode.sort.SortBySolution",
            "leetcode.sort.SortByAcceptance",
            "leetcode.sort.SortByDifficulty",
            "leetcode.sort.SortByFrequency",
            "leetcode.PageList",
            "leetcode.PageSize",
        ).forEach(::invokeActionWithRetries)

        val requestCountBeforePick = graphqlServer.requestCount("randomQuestion")
        invokeActionWithRetries("leetcode.PickAction")
        waitUntil("pick action requests a mocked random question") {
            graphqlServer.requestCount("randomQuestion") > requestCountBeforePick
        }
    }

    private fun Driver.assertConvergeEditorUi(description: String) {
        val convergeEditorTabs = ui.x { byType(JTabbedPane::class.java) }.waitFound(120.seconds)
        waitUntil(description) {
            convergeEditorTabs.driver.cast(convergeEditorTabs.component, JTabbedPaneRef::class)
                .let { tabs ->
                    (0 until tabs.getTabCount()).map(tabs::getTitleAt) ==
                        listOf("Content", "Solution", "Submissions", "Note")
                }
        }
    }

    private fun Driver.assertConvergeEditorTabSelected(expectedTitle: String) {
        val convergeEditorTabs = ui.x { byType(JTabbedPane::class.java) }.waitFound(120.seconds)
        waitUntil("the $expectedTitle ConvergeEditor tab is visible") {
            convergeEditorTabs.driver.cast(convergeEditorTabs.component, JTabbedPaneRef::class)
                .let { tabs ->
                    tabs.getSelectedIndex() >= 0 &&
                        tabs.getTitleAt(tabs.getSelectedIndex()) == expectedTitle
                }
        }
    }

    private fun Driver.assertPluginActionsRegistered() {
        pluginActionIds.forEach { actionId ->
            assertTrue(
                service(ActionManager::class).getAction(actionId) != null,
                "Plugin action $actionId must be registered",
            )
        }
    }

    private fun restoredLeetCodeFilePaths(fileEditorManager: FileEditorManager): Set<String> =
        fileEditorManager.getAllEditors()
            .map { it.getFile().getPath() }
            .filter { it.contains("/leetcode/editor/cn/") }
            .toSet()

    private fun restoredLeetCodeFileCount(fileEditorManager: FileEditorManager): Int =
        restoredLeetCodeFilePaths(fileEditorManager).size

    private fun createRestoredLeetCodeTabsProject(projectDir: Path, tabCount: Int): List<RestoredEditorEntry> {
        val editorEntries = (1..tabCount).map { questionId ->
            val codeRelativePath = "leetcode/editor/cn/[$questionId]题目$questionId.java"
            val contentRelativePath = "leetcode/editor/cn/doc/content/[$questionId]题目$questionId.md"
            val codeFile = projectDir.resolve(codeRelativePath)
            val contentFile = projectDir.resolve(contentRelativePath)
            Files.createDirectories(codeFile.parent)
            Files.createDirectories(contentFile.parent)
            Files.writeString(codeFile, "class Question$questionId {}\n")
            Files.writeString(contentFile, "<p>Question $questionId content</p>\n")
            RestoredEditorEntry(
                codeRelativePath = codeRelativePath,
                codePath = codeFile.toString(),
                contentPath = contentFile.toString(),
                questionId = questionId,
            )
        }
        val ideaDir = Files.createDirectories(projectDir.resolve(".idea"))
        val leetcodeIdeaDir = Files.createDirectories(ideaDir.resolve("leetcode"))
        Files.writeString(
            leetcodeIdeaDir.resolve("editor.xml"),
            """
                <project version="4">
                  <component name="LeetcodeEditor">
                    <option name="projectConfig">
                      <map>
                        ${editorEntries.joinToString("\n") { entry ->
                            """
                                <entry key="${escapeXml(entry.codePath)}">
                                  <value>
                                    <LeetcodeEditor>
                                      <option name="contentPath" value="${escapeXml(entry.contentPath)}" />
                                      <option name="frontendQuestionId" value="leetcode.cn${entry.questionId}" />
                                      <option name="host" value="leetcode.cn" />
                                      <option name="path" value="${escapeXml(entry.codePath)}" />
                                      <option name="titleSlug" value="question-${entry.questionId}" />
                                    </LeetcodeEditor>
                                  </value>
                                </entry>
                            """.trimIndent()
                        }}
                      </map>
                    </option>
                  </component>
                </project>
            """.trimIndent(),
        )
        return editorEntries
    }

    private fun writeTestConfiguration(
        configDir: Path,
        tempDir: Path,
        codeType: String = "Java",
        convergeEditor: Boolean = true,
        englishContent: Boolean = false,
        questionEditor: String = "Left",
        url: String = "leetcode.cn",
        showQuestionEditorSign: Boolean = true,
        customCode: Boolean = false,
        customFileName: String? = null,
        customTemplate: String? = null,
    ) {
        val optionsDir = Files.createDirectories(configDir.resolve("options"))
        val customCodeOptions = buildString {
            append("""<option name="customCode" value="$customCode" />""")
            customFileName?.let {
                append('\n')
                append("""<option name="customFileName" value="${escapeXml(it)}" />""")
            }
            customTemplate?.let {
                append('\n')
                append("""<option name="customTemplate" value="${escapeXml(it)}" />""")
            }
        }
        Files.writeString(
            optionsDir.resolve("leetcode-config.xml"),
            """
                <application>
                  <component name="PersistentConfig">
                    <option name="initConfig">
                      <Config>
                        <option name="codeType" value="$codeType" />
                        <option name="convergeEditor" value="$convergeEditor" />
                        $customCodeOptions
                        <option name="englishContent" value="$englishContent" />
                        <option name="filePath" value="${escapeXml(tempDir.toString())}" />
                        <option name="id" value="starter-ui-test" />
                        <option name="questionEditor" value="$questionEditor" />
                        <option name="showQuestionEditorSign" value="$showQuestionEditorSign" />
                        <option name="url" value="$url" />
                        <option name="version" value="3" />
                      </Config>
                    </option>
                  </component>
                </application>
            """.trimIndent(),
        )
    }

    private fun escapeXml(value: String) = value
        .replace("&", "&amp;")
        .replace("\"", "&quot;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\r\n", "&#10;")
        .replace("\n", "&#10;")
        .replace("\r", "&#10;")

    private fun waitUntil(description: String, condition: () -> Boolean) {
        val timeoutSeconds = System.getProperty("leetcode.test.wait.timeout.seconds", "120").toLong()
        val deadline = System.nanoTime() + timeoutSeconds * 1_000_000_000L
        while (System.nanoTime() < deadline) {
            if (condition()) {
                return
            }
            Thread.sleep(500)
        }
        assertTrue(condition(), "Timed out waiting for $description")
    }

    private fun waitUntilWithDiagnostics(expectedCount: Int, description: String, files: () -> Set<String>) {
        var restoredFiles = emptySet<String>()
        repeat(180) {
            restoredFiles = files()
            if (restoredFiles.size == expectedCount) {
                return
            }
            Thread.sleep(1_000)
        }
        assertTrue(
            restoredFiles.size == expectedCount,
            "Timed out waiting for $description; restored=${restoredFiles.size}, files=$restoredFiles",
        )
    }

    private class LocalGraphqlServer(val responseDelayMillis: Long = 0L) : AutoCloseable {
        private val requestCounts = ConcurrentHashMap<String, Int>()
        private val lastRequests = ConcurrentHashMap<String, String>()
        private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/graphql") { exchange -> handleGraphql(exchange) }
            createContext("/points/api/") { exchange ->
                recordPath(exchange)
                exchange.respondJson("""{"user_name":"starter-ui-test"}""")
            }
            createContext("/problems/two-sum/interpret_solution/") { exchange ->
                recordPath(exchange)
                exchange.respondJson("""{"interpret_id":"run-1","test_case":"[2,7,11,15]\n9"}""")
            }
            createContext("/problems/two-sum/submit/") { exchange ->
                recordPath(exchange)
                exchange.respondJson("""{"submission_id":"submit-1"}""")
            }
            createContext("/submissions/detail/run-1/check/") { exchange ->
                recordPath(exchange)
                exchange.respondJson(
                    """{"state":"SUCCESS","run_success":true,"code_answer":["[0,1]"],"expected_code_answer":["[0,1]"],"code_output":[]}""",
                )
            }
            createContext("/submissions/detail/submit-1/check/") { exchange ->
                recordPath(exchange)
                exchange.respondJson(
                    """{"state":"SUCCESS","run_success":true,"status_code":10,"status_runtime":"1 ms","runtime_percentile":50.0,"status_memory":"10 MB","memory_percentile":50.0}""",
                )
            }
            createContext("/problems/api/tags/") { exchange -> exchange.respondJson("""{"topics":[]}""") }
            createContext("/problems/api/favorites/") { exchange -> exchange.respondJson("[]") }
            createContext("/problems/api/card-info/") {
                exchange -> exchange.respondJson("""{"categories":{"0":[]}}""")
            }
            createContext("/api/tags/") { exchange -> exchange.respondJson("""[{"id":"array","name":"Array"}]""") }
            createContext("/api/companies/") { exchange -> exchange.respondJson("""[{"id":"mock-company","name":"Mock Company"}]""") }
            createContext("/api/questions/") {
                exchange ->
                    countPath(exchange.requestURI.path)
                    exchange.respondJson(
                        """
                            {"count":1,"list":[{"value":99,"time":"2026-07-26T00:00:00Z","leetcode":{"title":"CodeTop Two Sum","frontend_question_id":"1","level":"Easy","slug_title":"two-sum"}}]}
                        """.trimIndent(),
                    )
            }
            start()
        }

        val baseUrl: String = "http://127.0.0.1:${server.address.port}"

        private fun handleGraphql(exchange: HttpExchange) {
            val request = exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8)
            listOf(
                "globalData",
                "problemsetQuestionList",
                "allQuestions",
                "questionOfToday",
                "questionData",
                "randomQuestion",
                "getNote",
                "updateNote",
                "submissions",
            )
                .firstOrNull { request.contains("\"operationName\":\"$it\"") }
                ?.let {
                    requestCounts.compute(it) { _, count -> (count ?: 0) + 1 }
                    lastRequests[it] = request
                }
            if (responseDelayMillis > 0L) {
                Thread.sleep(responseDelayMillis)
            }
            val response = when {
                request.contains("\"operationName\":\"globalData\"") ->
                    """{"data":{"userStatus":{"isPremium":false,"username":"starter-ui-test","isSignedIn":true,"isVerified":true,"isPhoneVerified":true}}}"""
                request.contains("\"operationName\":\"problemsetQuestionList\"") ->
                    questionList(request)
                request.contains("\"operationName\":\"allQuestions\"") ->
                    allQuestions()
                request.contains("\"operationName\":\"questionOfToday\"") -> dailyQuestion()
                request.contains("\"operationName\":\"questionData\"") ->
                    """{"data":{"question":{"questionId":"1","questionFrontendId":"1","title":"Two Sum","titleSlug":"two-sum","content":"<p>Given an array of integers, return two indices.</p>","translatedTitle":"两数之和","translatedContent":"<p>给定一个整数数组，返回两个下标。</p>","isPaidOnly":false,"difficulty":"Easy","likes":10,"dislikes":1,"isLiked":false,"exampleTestcases":"[2,7,11,15]\n9","topicTags":[],"codeSnippets":[{"lang":"Java","langSlug":"java","code":"class Solution { public int[] twoSum(int[] nums, int target) { return new int[0]; } }"}],"hints":[],"solution":null,"status":null,"sampleTestCase":"[2,7,11,15]\n9","judgerAvailable":true,"judgeType":"large","mysqlSchemas":[],"libraryUrl":""}}}"""
                request.contains("\"operationName\":\"randomQuestion\"") ->
                    if (request.contains("problemsetRandomFilteredQuestion")) {
                        """{"data":{"randomQuestion":"two-sum"}}"""
                    } else {
                        """{"data":{"randomQuestion":{"titleSlug":"two-sum"}}}"""
                    }
                request.contains("\"operationName\":\"getNote\"") ->
                    """{"data":{"question":{"questionId":"1","note":"mock note from local server","__typename":"QuestionNode"}}}"""
                request.contains("\"operationName\":\"updateNote\"") ->
                    """{"data":{"updateNote":{"ok":true,"error":null,"question":{"questionId":"1","note":"mock note from local server","__typename":"QuestionNode"},"__typename":"UpdateNotePayload"}}}"""
                request.contains("\"operationName\":\"submissions\"") ->
                    """{"data":{"submissionList":{"lastKey":null,"hasNext":false,"submissions":[],"__typename":"SubmissionListNode"}}}"""
                else -> """{"data":{}}"""
            }
            exchange.respondJson(response)
        }

        private fun questionList(request: String): String {
            if (request.contains("\"searchKeywords\":\"\"")) {
                return """{"data":{"problemsetQuestionList":{"hasMore":false,"total":0,"questions":[]}}}"""
            }
            val startId = if (request.contains("\"skip\":50")) 51 else 1
            val questions = (startId until startId + 50).joinToString(",") { questionListItem(it) }
            return """{"data":{"problemsetQuestionList":{"hasMore":${startId == 1},"total":100,"questions":[$questions]}}}"""
        }

        private fun allQuestions(): String {
            val questions = (1..100).joinToString(",") { allQuestionItem(it) }
            return """{"data":{"allQuestions":[$questions]}}"""
        }

        private fun dailyQuestion(): String {
            return """
                {"data":{"activeDailyCodingChallengeQuestion":[{"date":"2026-07-27","userStatus":"NOT_STARTED","question":{"questionId":"daily-1","frontendQuestionId":"面试题 01.01","difficulty":"Easy","title":"Daily Question","titleCn":"每日一题","titleSlug":"daily-question","paidOnly":false,"freqBar":0,"acRate":50.0,"status":"NOT_STARTED","solutionNum":1,"topicTags":[]}}]}}
            """.trimIndent()
        }

        private fun questionListItem(id: Int): String {
            val (title, titleCn, titleSlug) = questionTitle(id)
            return """{"acRate":50.0,"difficulty":"${if (id % 3 == 0) "Hard" else if (id % 2 == 0) "Medium" else "Easy"}","freqBar":0,"frontendQuestionId":"$id","paidOnly":false,"solutionNum":1,"status":"NOT_STARTED","title":"$title","titleCn":"$titleCn","titleSlug":"$titleSlug","topicTags":[]}"""
        }

        private fun allQuestionItem(id: Int): String {
            val (title, translatedTitle, titleSlug) = questionTitle(id)
            return """{"title":"$title","titleSlug":"$titleSlug","translatedTitle":"$translatedTitle","frontendQuestionId":"$id","questionId":"$id","status":null,"level":"${if (id % 3 == 0) "Hard" else if (id % 2 == 0) "Medium" else "Easy"}","isPaidOnly":false,"category":"Algorithms"}"""
        }

        private fun questionTitle(id: Int): Triple<String, String, String> = when (id) {
            1 -> Triple("Two Sum", "两数之和", "two-sum")
            2 -> Triple("Add Two Numbers", "两数相加", "add-two-numbers")
            53 -> Triple("Maximum Subarray", "最大子数组和", "maximum-subarray")
            else -> Triple("Question $id", "翻译题目$id", "question-$id")
        }

        fun requestCount(operationName: String): Int = requestCounts[operationName] ?: 0

        fun totalRequestCount(): Int = requestCounts.values.sum()

        fun lastRequest(operationName: String): String = lastRequests[operationName].orEmpty()

        fun requestCountForPath(path: String): Int = requestCounts[path] ?: 0

        fun lastRequestForPath(path: String): String = lastRequests[path].orEmpty()

        private fun countPath(path: String) {
            requestCounts.compute(path) { _, count -> (count ?: 0) + 1 }
        }

        private fun recordPath(exchange: HttpExchange) {
            val path = exchange.requestURI.path
            countPath(path)
            lastRequests[path] = exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8)
        }

        private fun HttpExchange.respondJson(response: String) {
            val bytes = response.toByteArray(StandardCharsets.UTF_8)
            responseHeaders.add("Content-Type", "application/json; charset=UTF-8")
            sendResponseHeaders(200, bytes.size.toLong())
            responseBody.use { outputStream: OutputStream -> outputStream.write(bytes) }
        }

        override fun close() {
            server.stop(0)
        }
    }

    private companion object {
        val pluginActionIds = listOf(
            "leetcode.LoginAction",
            "leetcode.LogoutAction",
            "leetcode.RefreshAction",
            "leetcode.FindAction",
            "leetcode.ProgressAction",
            "leetcode.ConfigAction",
            "leetcode.ClearAllAction",
            "leetcode.HelpAction",
            "leetcode.ToggleListAction",
            "leetcode.OpenAction",
            "leetcode.OpenContentAction",
            "leetcode.OpenSolutionAction",
            "leetcode.OpenInWebAction",
            "leetcode.SubmitAction",
            "leetcode.SubmissionsAction",
            "leetcode.RunCodeAction",
            "leetcode.TestcaseAction",
            "leetcode.ClearOneAction",
            "leetcode.PickAction",
            "leetcode.positionAction",
            "leetcode.DonateAction",
            "leetcode.ShowNote",
            "leetcode.PullNote",
            "leetcode.PushNote",
            "leetcode.StartTimeAction",
            "leetcode.StopTimeAction",
            "leetcode.ResetTimeAction",
            "leetcode.find.Clear",
            "leetcode.editor.RunCodeAction",
            "leetcode.editor.TestcaseAction",
            "leetcode.editor.SubmitAction",
            "leetcode.editor.SubmissionsAction",
            "leetcode.editor.OpenContentAction",
            "leetcode.editor.OpenSolutionAction",
            "leetcode.editor.OpenInWebAction",
            "leetcode.editor.ShowNote",
            "leetcode.editor.PullNote",
            "leetcode.editor.PushNote",
            "leetcode.editor.StartTimeAction",
            "leetcode.editor.StopTimeAction",
            "leetcode.editor.ResetTimeAction",
            "leetcode.sort.Sort",
            "leetcode.sort.SortByTitle",
            "leetcode.sort.SortBySolution",
            "leetcode.sort.SortByAcceptance",
            "leetcode.sort.SortByDifficulty",
            "leetcode.sort.SortByFrequency",
            "leetcode.PageSize",
            "leetcode.PageList",
            "leetcode.PreviousPage",
            "leetcode.NextPage",
            "leetcode.GoPage",
            "leetcode.codetop.RefreshAction",
            "leetcode.codetop.FindAction",
            "leetcode.codetop.ShareAction",
            "leetcode.codetop.find.Clear",
            "leetcode.codetop.sort.Sort",
            "leetcode.codetop.sort.CodeTopSortByTitle",
            "leetcode.codetop.sort.CodeTopSortByTime",
            "leetcode.codetop.sort.CodeTopSortByFrequency",
            "leetcode.all.find.Clear",
            "leetcode.all.codetop.sort.Sort",
            "leetcode.all.sort.SortByTitle",
            "leetcode.all.sort.SortByDifficulty",
            "leetcode.all.sort.SortByStates",
        )
    }
}
