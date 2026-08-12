package com.shuzijun.leetcode.plugin.editor;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.jcef.JCEFHtmlPanel;
import com.intellij.ui.jcef.JBCefBrowserBase;
import com.intellij.ui.jcef.JBCefJSQuery;
import com.intellij.util.Url;
import com.intellij.util.Urls;
import com.intellij.util.io.HttpRequests;
import com.intellij.util.io.URLUtil;
import com.shuzijun.leetcode.plugin.product.ProductProfiles;
import com.shuzijun.leetcode.plugin.utils.BrowserUtils;
import com.shuzijun.leetcode.plugin.utils.DevelopmentTools;
import com.shuzijun.leetcode.plugin.utils.FileUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.ui.ContentStatePanel;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.apache.commons.lang3.StringUtils;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefContextMenuParams;
import org.cef.callback.CefMenuModel;
import org.cef.handler.*;
import org.cef.misc.BoolRef;
import org.cef.network.CefRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.ide.BuiltInServerManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author shuzijun
 */
public class LCVPanel extends JCEFHtmlPanel {

    private static final Logger LOG = Logger.getInstance(LCVPanel.class);
    private static final String TEMPLATE = loadTemplate();

    private final Url servicePath = BuiltInServerManager.getInstance().addAuthToken(
            Urls.parseEncoded(
                    "http://localhost:"
                            + BuiltInServerManager.getInstance().getPort()
                            + PreviewStaticServer.prefix()
            )
    );
    private CefRequestHandler requestHandler;
    private CefLifeSpanHandler lifeSpanHandler;
    private CefLoadHandlerAdapter loadHandler;
    private CefContextMenuHandlerAdapter contextMenuHandler;
    private JBCefJSQuery readyQuery;
    private javax.swing.Timer readableTimeout;

    private final String url;
    private final String text;
    private final Project project;
    private final QuestionPreviewRenderMode renderMode;
    private final QuestionPreviewPerformanceTracker.Trace performanceTrace;
    private final List<String> iframe = new ArrayList<>();
    private final AtomicBoolean initialLoad = new AtomicBoolean(true);
    private final AtomicBoolean loadFailed = new AtomicBoolean();
    private final AtomicBoolean disposed = new AtomicBoolean();
    private final ContentStatePanel component = new ContentStatePanel();
    private static final List<String> headers = Arrays.asList(HttpHeaderNames.CONTENT_SECURITY_POLICY.toString(), HttpHeaderNames.CONTENT_ENCODING.toString()
            , HttpHeaderNames.CONTENT_LENGTH.toString());
    private static final int OPEN_DEVTOOLS_COMMAND_ID = 0x6C01;

    public LCVPanel(@Nullable String url, Project project, String text, boolean old) {
        this(url, project, text, null, QuestionPreviewRenderMode.MARKDOWN, old);
    }

    public LCVPanel(@Nullable String url, Project project, String text,
                    @Nullable QuestionPreviewPerformanceTracker.Trace performanceTrace, boolean old) {
        this(url, project, text, performanceTrace, QuestionPreviewRenderMode.MARKDOWN, old);
    }

    public LCVPanel(@Nullable String url, Project project, String text,
                    @Nullable QuestionPreviewPerformanceTracker.Trace performanceTrace,
                    QuestionPreviewRenderMode renderMode, boolean old) {
        super(null);
        this.url = url;
        this.project = project;
        this.text = text;
        this.performanceTrace = performanceTrace;
        this.renderMode = renderMode;
        init();
    }

    public LCVPanel(@Nullable String url, Project project, String text) {
        this(url, project, text, null, QuestionPreviewRenderMode.MARKDOWN);
    }

    public LCVPanel(@Nullable String url, Project project, String text,
                    @Nullable QuestionPreviewPerformanceTracker.Trace performanceTrace) {
        this(url, project, text, performanceTrace, QuestionPreviewRenderMode.MARKDOWN);
    }

    public LCVPanel(@Nullable String url, Project project, String text,
                    @Nullable QuestionPreviewPerformanceTracker.Trace performanceTrace,
                    QuestionPreviewRenderMode renderMode) {
        super(null, null);
        this.url = url;
        this.project = project;
        this.text = text;
        this.performanceTrace = performanceTrace;
        this.renderMode = renderMode;
        init();
    }

    private void init() {
        component.showLoadingOver(LCVPanel.super.getComponent(), PropertiesUtils.getInfo("ui.loading"));
        if (performanceTrace != null) {
            performanceTrace.mark(QuestionPreviewPerformanceTracker.Milestone.BROWSER_CREATED);
        }
        readyQuery = JBCefJSQuery.create((JBCefBrowserBase) this);
        readyQuery.addHandler(event -> {
            handlePreviewEvent(event);
            return new JBCefJSQuery.Response("ok");
        });
        getJBCefClient().addRequestHandler(requestHandler = new CefRequestHandlerAdapter() {
            @Override
            public boolean onBeforeBrowse(CefBrowser browser, CefFrame frame, CefRequest request, boolean user_gesture, boolean is_redirect) {
                String requestUrl = request.getURL();
                if (requestUrl.startsWith(url)) {
                    return false;
                } else if (!user_gesture) {
                    iframe.add(requestUrl);
                    return false;
                } else {
                    openUrl(URLDecoder.decode(requestUrl, StandardCharsets.UTF_8));
                    return true;
                }
            }

            @Override
            public CefResourceRequestHandler getResourceRequestHandler(CefBrowser browser, CefFrame frame, CefRequest request, boolean isNavigation, boolean isDownload, String requestInitiator, BoolRef disableDefaultHandling) {
                String requestUrl = request.getURL();
                if (!iframe.contains(requestUrl)) {
                    return null;
                }

                return new CefResourceRequestHandlerAdapter() {

                    @Override
                    public CefResourceHandler getResourceHandler(CefBrowser browser, CefFrame frame, CefRequest request) {
                        try {
                            return HttpRequests.request(request.getURL())
                                    .throwStatusCodeException(false)
                                    .connect(new HttpRequests.RequestProcessor<CefResourceHandler>() {
                                        @Override
                                        public CefResourceHandler process(HttpRequests.Request request) throws IOException {
                                            HttpURLConnection urlConnection = (HttpURLConnection) request.getConnection();
                                            Map<String, String> header = new HashMap<>();
                                            urlConnection.getHeaderFields().forEach((key, values) -> {
                                                if (key != null && values != null && !headers.contains(key.toLowerCase())) {
                                                    header.put(key, StringUtils.join(values.toArray(), ";"));
                                                }
                                            });
                                            return new ProxyLoadHtmlResourceHandler(request.readString(), header, urlConnection.getResponseCode());
                                        }
                                    });
                        } catch (IOException io) {

                            return null;
                        }
                    }
                };
            }
        }, getCefBrowser());
        getJBCefClient().addLifeSpanHandler(lifeSpanHandler = new CefLifeSpanHandlerAdapter() {
            @Override
            public boolean onBeforePopup(CefBrowser browser, CefFrame frame, String target_url, String target_frame_name) {
                if (!target_url.startsWith(url)) {
                    openUrl(URLDecoder.decode(target_url, StandardCharsets.UTF_8));
                }
                return true;
            }
        }, getCefBrowser());
        getJBCefClient().addLoadHandler(loadHandler = new CefLoadHandlerAdapter() {
            @Override
            public void onLoadingStateChange(
                    CefBrowser browser,
                    boolean isLoading,
                    boolean canGoBack,
                    boolean canGoForward
            ) {
                if (!disposed.get() && !isLoading && !loadFailed.get() && performanceTrace != null) {
                    performanceTrace.mark(QuestionPreviewPerformanceTracker.Milestone.MAIN_FRAME_LOADED);
                }
            }

            @Override
            public void onLoadError(CefBrowser browser, CefFrame frame, CefLoadHandler.ErrorCode errorCode,
                                    String errorText, String failedUrl) {
                if (disposed.get() || !frame.isMain()) {
                    return;
                }
                loadFailed.set(true);
                initialLoad.set(false);
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (!disposed.get()) {
                        component.showError(
                                PropertiesUtils.getInfo("response.question"),
                                PropertiesUtils.getInfo("ui.retry"),
                                LCVPanel.this::reloadText
                        );
                    }
                });
            }
        }, getCefBrowser());
        installDevelopmentContextMenu();
        loadHTML(createHtml(text), url);
        startReadableTimeout();
    }

    private void installDevelopmentContextMenu() {
        if (!DevelopmentTools.isEnabled()) {
            return;
        }
        contextMenuHandler = new CefContextMenuHandlerAdapter() {
            @Override
            public void onBeforeContextMenu(CefBrowser browser, CefFrame frame,
                                            CefContextMenuParams parameters, CefMenuModel model) {
                model.addSeparator();
                model.addItem(OPEN_DEVTOOLS_COMMAND_ID, "打开网页调试");
            }

            @Override
            public boolean onContextMenuCommand(CefBrowser browser, CefFrame frame,
                                                CefContextMenuParams parameters, int commandId, int eventFlags) {
                if (commandId != OPEN_DEVTOOLS_COMMAND_ID) {
                    return false;
                }
                browser.openDevTools();
                return true;
            }
        };
        getJBCefClient().addContextMenuHandler(contextMenuHandler, getCefBrowser());
    }

    private void handlePreviewEvent(String event) {
        if (disposed.get()) {
            return;
        }
        if ("readable".equals(event)) {
            if (performanceTrace != null) {
                QuestionPreviewPerformanceTracker.getInstance(project).readable(performanceTrace);
            }
            showReadableContent();
        } else if ("stable".equals(event)) {
            if (performanceTrace != null) {
                QuestionPreviewPerformanceTracker.getInstance(project).visualStable(performanceTrace);
            }
        } else if ("error".equals(event)) {
            showRenderError();
        }
    }

    private void showRenderError() {
        loadFailed.set(true);
        initialLoad.set(false);
        ApplicationManager.getApplication().invokeLater(() -> {
            if (!disposed.get()) {
                stopReadableTimeout();
                component.showError(
                        PropertiesUtils.getInfo("response.question"),
                        PropertiesUtils.getInfo("ui.retry"),
                        this::reloadText
                );
            }
        });
    }

    private void showReadableContent() {
        if (!initialLoad.compareAndSet(true, false)) {
            return;
        }
        ApplicationManager.getApplication().invokeLater(() -> {
            if (disposed.get() || loadFailed.get()) {
                return;
            }
            stopReadableTimeout();
            component.showContent(LCVPanel.super.getComponent());
            component.requestFocusInWindow();
        });
    }

    private void startReadableTimeout() {
        stopReadableTimeout();
        readableTimeout = new javax.swing.Timer(5_000, event -> {
            if (disposed.get() || loadFailed.get() || !initialLoad.get()) {
                return;
            }
            if (performanceTrace != null) {
                QuestionPreviewPerformanceTracker.getInstance(project).timeout(performanceTrace);
            }
        });
        readableTimeout.setRepeats(false);
        readableTimeout.start();
    }

    private void stopReadableTimeout() {
        if (readableTimeout != null) {
            readableTimeout.stop();
            readableTimeout = null;
        }
    }

    public void reloadText() {
        if (disposed.get()) {
            return;
        }
        getCefBrowser().stopLoad();
        loadFailed.set(false);
        initialLoad.set(true);
        component.showLoadingOver(LCVPanel.super.getComponent(), PropertiesUtils.getInfo("ui.loading"));
        loadHTML(createHtml(text), url);
        startReadableTimeout();
    }

    @Override
    public void dispose() {
        if (!disposed.compareAndSet(false, true)) {
            return;
        }
        getCefBrowser().stopLoad();
        stopReadableTimeout();
        if (readyQuery != null) {
            readyQuery.dispose();
            readyQuery = null;
        }
        if (requestHandler != null) {
            getJBCefClient().removeRequestHandler(requestHandler, getCefBrowser());
            requestHandler = null;
        }
        if (lifeSpanHandler != null) {
            getJBCefClient().removeLifeSpanHandler(lifeSpanHandler, getCefBrowser());
            lifeSpanHandler = null;
        }
        if (loadHandler != null) {
            getJBCefClient().removeLoadHandler(loadHandler, getCefBrowser());
            loadHandler = null;
        }
        if (contextMenuHandler != null) {
            getJBCefClient().removeContextMenuHandler(contextMenuHandler, getCefBrowser());
            contextMenuHandler = null;
        }
        iframe.clear();
        super.dispose();
    }

    @Override
    public @NotNull JComponent getComponent() {
        return component;
    }

    private void openUrl(String url) {
        if (url.startsWith(URLUtil.FILE_PROTOCOL)) {
            File file = new File(url.substring((URLUtil.FILE_PROTOCOL + URLUtil.SCHEME_SEPARATOR + FileUtils.separator()).length()));
            if (!file.exists()) {
                Notifications.Bus.notify(new Notification(ProductProfiles.current().notificationGroup(), "Cannot Open File", file.getPath() + " not exist", NotificationType.INFORMATION), project);
            } else if (file.isDirectory()) {
                Notifications.Bus.notify(new Notification(ProductProfiles.current().notificationGroup(), "Cannot Open Directory", file.getPath() + " is a directory", NotificationType.INFORMATION), project);
            } else {
                ApplicationManager.getApplication().invokeLater(() -> {
                    VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
                    FileEditorManager.getInstance(project).openFile(vf, false);
                });
            }
        } else {
            BrowserUtils.browse(url);
        }
    }

    private String createHtml(String text) {
        return TEMPLATE.replace("{{service}}", servicePath.getScheme() + URLUtil.SCHEME_SEPARATOR + servicePath.getAuthority() + servicePath.getPath())
                    .replace("{{serverToken}}", org.apache.commons.lang3.StringUtils.isNotBlank(servicePath.getParameters()) ? servicePath.getParameters().substring(1) : "")
                    .replace("{{Lang}}", PropertiesUtils.getInfo("Lang"))
                    .replace("{{darcula}}", isDarkTheme() + "")
                    .replace("{{ideStyle}}", getStyle(true))
                    .replace("{{previewReady}}", readyQuery.inject("'readable'"))
                    .replace("{{previewStable}}", readyQuery.inject("'stable'"))
                    .replace("{{previewError}}", readyQuery.inject("'error'"))
                    .replace("{{renderMode}}", renderMode.name().toLowerCase(Locale.ROOT))
                    .replace("{{fileValue}}", escapeTextarea(text))
                    ;
    }

    static String escapeTextarea(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static String loadTemplate() {
        try (InputStream inputStream = PreviewStaticServer.class.getResourceAsStream("/template/default.html")) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing question preview template");
            }
            return new String(FileUtilRt.loadBytes(inputStream), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getStyle(boolean isTag) {
        try {
            EditorColorsScheme editorColorsScheme = EditorColorsManager.getInstance().getGlobalScheme();
            Color defaultBackground = editorColorsScheme.getDefaultBackground();

            boolean brightBackground = isBright(defaultBackground);
            Color scrollbarThumbColor = brightBackground
                    ? new Color(0, 0, 0, 82)
                    : new Color(255, 255, 255, 82);
            Color scrollbarThumbHoverColor = brightBackground
                    ? new Color(0, 0, 0, 112)
                    : new Color(255, 255, 255, 112);
            TextAttributes textAttributes = editorColorsScheme.getAttributes(TextAttributesKey.find("TEXT"));
            Color text = null;
            if (textAttributes != null) {
                text = textAttributes.getForegroundColor();
            }
            String fontFamily = "font-family:\"" + editorColorsScheme.getEditorFontName() + "\",\"Helvetica Neue\",\"Luxi Sans\",\"DejaVu Sans\"," +
                    "\"Hiragino Sans GB\",\"Microsoft Yahei\",sans-serif,\"Apple Color Emoji\",\"Segoe UI Emoji\",\"Noto Color Emoji\",\"Segoe UI Symbol\"," +
                    "\"Android Emoji\",\"EmojiSymbols\";";
            StringBuilder sb = new StringBuilder(isTag ? "<style id=\"ideaStyle\">" : "");
            sb.append(brightBackground ? ".vditor" : ".vditor--dark").append("{--panel-background-color:").append(toHexColor(defaultBackground))
                    .append(";--textarea-background-color:").append(toHexColor(defaultBackground)).append(";");
            sb.append("--toolbar-background-color:").append(toHexColor(JBColor.background())).append(";");
            sb.append("}");
            sb.append("::-webkit-scrollbar-track {background-color:").append(toHexColor(defaultBackground)).append(";}");
            sb.append("::-webkit-scrollbar-thumb {background-color:").append(toHexColor(scrollbarThumbColor)).append(";}");
            sb.append("::-webkit-scrollbar-thumb:hover {background-color:").append(toHexColor(scrollbarThumbHoverColor)).append(";}");
            sb.append(".vditor-reset {font-size:").append(editorColorsScheme.getEditorFontSize()).append("px;");
            sb.append(fontFamily);
            if (text != null) {
                sb.append("color:").append(toHexColor(text)).append(";");
            }
            sb.append("}");
            sb.append(" body{background-color: ").append(toHexColor(defaultBackground)).append(";}");
            sb.append(isTag ? "</style>" : "");
            return sb.toString();
        } catch (Exception e) {
            return "";
        }

    }

    private String toHexColor(Color color) {
        DecimalFormat df = new DecimalFormat("0.00");
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(dfs);
        return String.format("rgba(%s,%s,%s,%s)", color.getRed(), color.getGreen(), color.getBlue(), df.format(color.getAlpha() / (float) 255));
    }

    private boolean isDarkTheme() {
        return !isBright(EditorColorsManager.getInstance().getGlobalScheme().getDefaultBackground());
    }

    static boolean isBright(Color color) {
        return color.getRed() * 299 + color.getGreen() * 587 + color.getBlue() * 114 >= 128_000;
    }

    public void updateStyle() {
        String style = getStyle(false);
        getCefBrowser().executeJavaScript(
                "updateStyle('" + style + "'," + isDarkTheme() + ");", getCefBrowser().getURL(), 0);
    }
}
