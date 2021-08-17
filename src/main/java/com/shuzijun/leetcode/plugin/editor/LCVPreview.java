package com.shuzijun.leetcode.plugin.editor;

import com.google.common.net.UrlEscapers;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsListener;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.impl.EditorColorsSchemeImpl;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.Url;
import com.intellij.util.Urls;
import com.intellij.util.io.URLUtil;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.UIUtil;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.utils.FileUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.ide.BuiltInServerManager;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

/**
 * @author shuzijun
 */
public class LCVPreview extends UserDataHolderBase implements FileEditor {

    private final Project myProject;
    private final VirtualFile myFile;
    private final Document myDocument;

    private final JPanel myHtmlPanelWrapper;
    private final LCVPanel myPanel;

    private final Url servicePath = BuiltInServerManager.getInstance().addAuthToken(Urls.parseEncoded("http://localhost:" + BuiltInServerManager.getInstance().getPort() + PreviewStaticServer.PREFIX));
    private final String templateHtmlFile = "template/default.html";
    private final boolean isPresentableUrl;

    public LCVPreview(@NotNull Project project, @NotNull VirtualFile file) {
        myProject = project;
        myFile = file;
        myDocument = FileDocumentManager.getInstance().getDocument(myFile);
        myHtmlPanelWrapper = new JPanel(new BorderLayout());
        isPresentableUrl = project.getPresentableUrl() != null;
        String url = UrlEscapers.urlFragmentEscaper().escape(URLUtil.FILE_PROTOCOL + URLUtil.SCHEME_SEPARATOR + FileUtils.separator() + myFile.getPath());
        LCVPanel tempPanel = null;
        try {
            tempPanel = new LCVPanel(url, project);
            tempPanel.loadHTML(createHtml(isPresentableUrl), url);
            myHtmlPanelWrapper.add(tempPanel.getComponent(), BorderLayout.CENTER);
        } catch (Exception e) {
            myHtmlPanelWrapper.add(new JBLabel(e.getMessage()), BorderLayout.CENTER);
        }
        myPanel = tempPanel;
        myHtmlPanelWrapper.repaint();
        MessageBusConnection settingsConnection = ApplicationManager.getApplication().getMessageBus().connect(this);
        settingsConnection.subscribe(EditorColorsManager.TOPIC, new EditorColorsListener() {
            @Override
            public void globalSchemeChange(@Nullable EditorColorsScheme scheme) {
                myPanel.updateStyle(getStyle(false));
            }
        });
    }

    @Override
    public @NotNull JComponent getComponent() {
        return myHtmlPanelWrapper;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return myPanel != null ? myPanel.getComponent() : null;
    }

    @Override
    public @NotNull String getName() {
        return PluginConstant.LEETCODE_EDITOR_VIEW;
    }

    @Override
    public void setState(@NotNull FileEditorState state) {

    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Override
    public @Nullable FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    public void dispose() {
        if (myPanel != null) {
            Disposer.dispose(myPanel);
        }
    }

    private String createHtml(boolean isPresentableUrl) {
        InputStream inputStream = null;

        try {

            inputStream = PreviewStaticServer.class.getResourceAsStream("/" + templateHtmlFile);

            String template = new String(FileUtilRt.loadBytes(inputStream));
            return template.replace("{{service}}", servicePath.getScheme() + URLUtil.SCHEME_SEPARATOR + servicePath.getAuthority() + servicePath.getPath())
                    .replace("{{serverToken}}", StringUtils.isNotBlank(servicePath.getParameters()) ? servicePath.getParameters().substring(1) : "")
                    .replace("{{fileValue}}", FileDocumentManager.getInstance().getDocument(myFile).getText())
                    .replace("{{Lang}}", PropertiesUtils.getInfo("Lang"))
                    .replace("{{darcula}}", UIUtil.isUnderDarcula() + "")
                    .replace("{{ideStyle}}", getStyle(true))
                    ;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    @Override
    public @Nullable VirtualFile getFile() {
        return myFile;
    }


    private String getStyle(boolean isTag) {
        try {
            EditorColorsSchemeImpl editorColorsScheme = (EditorColorsSchemeImpl) EditorColorsManager.getInstance().getGlobalScheme();
            Color defaultBackground = editorColorsScheme.getDefaultBackground();

            Color scrollbarThumbColor = EditorColors.SCROLLBAR_THUMB_COLOR.getDefaultColor();
            if (editorColorsScheme.getColor(EditorColors.SCROLLBAR_THUMB_COLOR) != null) {
                scrollbarThumbColor = editorColorsScheme.getColor(EditorColors.SCROLLBAR_THUMB_COLOR);
            }
            TextAttributes textAttributes = editorColorsScheme.getDirectlyDefinedAttributes().get("TEXT");
            Color text = null;
            if (textAttributes != null) {
                text = textAttributes.getForegroundColor();
            }
            String fontFamily = "font-family:\""+editorColorsScheme.getEditorFontName()+"\",\"Helvetica Neue\",\"Luxi Sans\",\"DejaVu Sans\"," +
                    "\"Hiragino Sans GB\",\"Microsoft Yahei\",sans-serif,\"Apple Color Emoji\",\"Segoe UI Emoji\",\"Noto Color Emoji\",\"Segoe UI Symbol\"," +
                    "\"Android Emoji\",\"EmojiSymbols\";";
            StringBuilder sb = new StringBuilder(isTag ? "<style id=\"ideaStyle\">" : "");
            sb.append(UIUtil.isUnderDarcula() ? ".vditor--dark" : ".vditor").append("{--panel-background-color:").append(toHexColor(defaultBackground))
                    .append(";--textarea-background-color:").append(toHexColor(defaultBackground)).append(";");
            sb.append("--toolbar-background-color:").append(toHexColor(JBColor.background())).append(";");
            sb.append("}");
            sb.append("::-webkit-scrollbar-track {background-color:").append(toHexColor(defaultBackground)).append(";}");
            sb.append("::-webkit-scrollbar-thumb {background-color:").append(toHexColor(scrollbarThumbColor)).append(";}");
            sb.append(".vditor-reset {font-size:").append(editorColorsScheme.getEditorFontSize()).append(";");
            sb.append(fontFamily);
            if (text != null) {
                sb.append("color:").append(toHexColor(text)).append(";");
            }
            sb.append("}");
            sb.append(isTag ? "</style>" : "");
            return sb.toString();
        } catch (Exception e) {
            return "";
        }

    }

    private String toHexColor(Color color) {
        DecimalFormat df = new DecimalFormat("0.00");
        return String.format("rgba(%s,%s,%s,%s)", color.getRed(), color.getGreen(), color.getBlue(), df.format(color.getAlpha() / (float) 255));
    }

}
