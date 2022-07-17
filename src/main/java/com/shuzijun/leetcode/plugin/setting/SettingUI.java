package com.shuzijun.leetcode.plugin.setting;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.net.HttpConfigurable;
import com.shuzijun.leetcode.plugin.listener.ColorListener;
import com.shuzijun.leetcode.plugin.listener.ConfigNotifier;
import com.shuzijun.leetcode.plugin.listener.DonateListener;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.model.PluginConstant;
import com.shuzijun.leetcode.plugin.utils.MTAUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * @author shuzijun
 */
public class SettingUI {
    private JPanel mainPanel;
    private JComboBox<String> questionEditorBox;
    private JComboBox<String> webComboBox;
    private JComboBox<String> codeComboBox;
    private JBTextField userNameField;
    private JBPasswordField passwordField;
    private JLabel easyLabel;
    private JLabel mediumLabel;
    private JLabel hardLabel;
    private TextFieldWithBrowseButton fileFolderBtn;
    private JCheckBox customCodeBox;
    private JCheckBox updateCheckBox;
    private JCheckBox proxyCheckBox;
    private JCheckBox englishContentBox;

    private JLabel templateConfigHelp;
    private JPanel codeFileName;
    private JPanel codeTemplate;
    private JPanel templateConstant;
    private JCheckBox cookieCheckBox;
    private JCheckBox multilineCheckBox;
    private JCheckBox htmlContentCheckBox;
    private JCheckBox showTopicsCheckBox;
    private JCheckBox showToolIconCheckBox;
    private JCheckBox convergeEditorCheckBox;
    private JCheckBox showEditorSignCheckBox;


    private Editor fileNameEditor = null;
    private Editor templateEditor = null;
    private Editor templateHelpEditor = null;


    public SettingUI() {
        initUI();
    }

    public void initUI() {

        webComboBox.addItem(URLUtils.leetcodecn);
        webComboBox.addItem(URLUtils.leetcode);

        for (CodeTypeEnum c : CodeTypeEnum.values()) {
            codeComboBox.addItem(c.getType());
        }
        easyLabel.addMouseListener(new ColorListener(mainPanel, easyLabel));
        mediumLabel.addMouseListener(new ColorListener(mainPanel, mediumLabel));
        hardLabel.addMouseListener(new ColorListener(mainPanel, hardLabel));

        fileFolderBtn.addBrowseFolderListener(new TextBrowseFolderListener(FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()) {
        });

        customCodeBox.addActionListener(new DonateListener(customCodeBox));
        proxyCheckBox.setSelected(HttpConfigurable.getInstance().USE_HTTP_PROXY || HttpConfigurable.getInstance().USE_PROXY_PAC);
        proxyCheckBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (HttpConfigurable.editConfigurable(mainPanel)) {
                    proxyCheckBox.setSelected(HttpConfigurable.getInstance().USE_HTTP_PROXY || HttpConfigurable.getInstance().USE_PROXY_PAC);
                }
            }
        });

        templateConfigHelp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                BrowserUtil.browse("https://github.com/shuzijun/leetcode-editor/blob/master/doc/CustomCode.md");
            }
        });

        fileNameEditor = EditorFactory.getInstance().createEditor(EditorFactory.getInstance().createDocument(""), null, FileTypeManager.getInstance().getFileTypeByExtension("vm"), false);
        EditorSettings settings = fileNameEditor.getSettings();
        ((EditorImpl) fileNameEditor).setOneLineMode(true);
        //额外的行
        settings.setAdditionalLinesCount(0);
        //额外的列
        settings.setAdditionalColumnsCount(0);
        settings.setCaretRowShown(false);
        //折叠大纲
        settings.setFoldingOutlineShown(false);
        //缩进
        settings.setIndentGuidesShown(false);
        //线性标记区域
        settings.setLineMarkerAreaShown(false);
        //行号
        settings.setLineNumbersShown(false);
        //虚拟空间
        settings.setVirtualSpace(false);
        //允许单逻辑行折叠
        settings.setAllowSingleLogicalLineFolding(false);
        //滚动
        settings.setAnimatedScrolling(true);
        //底部附加
        settings.setAdditionalPageAtBottom(false);
        //代码自动折叠
        settings.setAutoCodeFoldingEnabled(false);
        codeFileName.add(fileNameEditor.getComponent(), BorderLayout.CENTER);


        templateEditor = EditorFactory.getInstance().createEditor(EditorFactory.getInstance().createDocument(""), null, FileTypeManager.getInstance().getFileTypeByExtension("vm"), false);
        EditorSettings templateEditorSettings = templateEditor.getSettings();
        templateEditorSettings.setAdditionalLinesCount(0);
        templateEditorSettings.setAdditionalColumnsCount(0);
        templateEditorSettings.setLineMarkerAreaShown(false);
        templateEditorSettings.setVirtualSpace(false);
        JBScrollPane jbScrollPane = new JBScrollPane(templateEditor.getComponent());
        codeTemplate.add(jbScrollPane, BorderLayout.CENTER);

        templateHelpEditor = EditorFactory.getInstance().createEditor(EditorFactory.getInstance().createDocument(PropertiesUtils.getInfo("template.variable", "{", "}")), null, FileTypeManager.getInstance().getFileTypeByExtension("vm"), true);
        EditorSettings templateHelpEditorSettings = templateHelpEditor.getSettings();
        templateHelpEditorSettings.setAdditionalLinesCount(0);
        templateHelpEditorSettings.setAdditionalColumnsCount(0);
        templateHelpEditorSettings.setLineMarkerAreaShown(false);
        templateHelpEditorSettings.setLineNumbersShown(false);
        templateHelpEditorSettings.setVirtualSpace(false);
        templateConstant.add(templateHelpEditor.getComponent(), BorderLayout.CENTER);

        questionEditorBox.addItem("Disable");
        questionEditorBox.addItem("Left");
        questionEditorBox.addItem("Right");

        loadSetting();
    }

    private void loadSetting() {
        webComboBox.setSelectedIndex(0);
        codeComboBox.setSelectedIndex(0);
        fileFolderBtn.setText(System.getProperty("java.io.tmpdir"));

        Config config = PersistentConfig.getInstance().getInitConfig();
        if (config != null) {
            userNameField.setText(config.getLoginName());
            passwordField.setText(PersistentConfig.getInstance().getPassword(config.getLoginName()));
            if (StringUtils.isNotBlank(config.getFilePath())) {
                fileFolderBtn.setText(config.getFilePath());
            }
            if (StringUtils.isNotBlank(config.getCodeType())) {
                codeComboBox.setSelectedItem(config.getCodeType());
            }
            if (StringUtils.isNotBlank(config.getUrl())) {
                webComboBox.setSelectedItem(config.getUrl());
            }
            updateCheckBox.setSelected(config.getUpdate());
            customCodeBox.setSelected(config.getCustomCode());
            ApplicationManager.getApplication().runWriteAction(() -> {
                fileNameEditor.getDocument().setText(config.getCustomFileName());
                templateEditor.getDocument().setText(config.getCustomTemplate());
            });
            englishContentBox.setSelected(config.getEnglishContent());

            Color[] colors = config.getFormatLevelColour();
            easyLabel.setForeground(colors[0]);
            mediumLabel.setForeground(colors[1]);
            hardLabel.setForeground(colors[2]);

            cookieCheckBox.setSelected(config.isCookie());
            if (config.getQuestionEditor().equals("true")) {
                questionEditorBox.setSelectedItem("Left");
            } else if (config.getQuestionEditor().equals("false")) {
                questionEditorBox.setSelectedItem("Disable");
            } else {
                questionEditorBox.setSelectedItem(config.getQuestionEditor());
            }

            multilineCheckBox.setSelected(config.getMultilineComment());
            htmlContentCheckBox.setSelected(config.getHtmlContent());
            showTopicsCheckBox.setSelected(config.getShowTopics());
            showToolIconCheckBox.setSelected(config.getShowToolIcon());
            convergeEditorCheckBox.setSelected(config.getConvergeEditor());
            showEditorSignCheckBox.setSelected(config.isShowQuestionEditorSign());
        } else {
            Color[] colors = new Config().getFormatLevelColour();
            easyLabel.setForeground(colors[0]);
            mediumLabel.setForeground(colors[1]);
            hardLabel.setForeground(colors[2]);
            ApplicationManager.getApplication().runWriteAction(() -> {
                fileNameEditor.getDocument().setText(Constant.CUSTOM_FILE_NAME);
                templateEditor.getDocument().setText(Constant.CUSTOM_TEMPLATE);
            });
            questionEditorBox.setSelectedItem("Left");
        }


    }

    public JPanel getContentPane() {
        return mainPanel;
    }

    public boolean isModified() {
        Config config = PersistentConfig.getInstance().getInitConfig();
        if (config == null) {
            return true;
        } else {
            Config currentState = new Config();
            process(currentState);
            if (currentState.isModified(config)) {
                if (passwordField.getText() != null && passwordField.getText().equals(PersistentConfig.getInstance().getPassword(config.getLoginName()))) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        }
    }

    public void apply() {
        Config config = PersistentConfig.getInstance().getInitConfig();
        Config oldConfig = null;
        if (config == null) {
            config = new Config();
            config.setId(MTAUtils.getI(""));
        } else {
            oldConfig = config.clone();
        }
        process(config);
        File file = new File(config.getFilePath() + File.separator + PersistentConfig.PATH + File.separator);
        if (!file.exists()) {
            file.mkdirs();
        }
        PersistentConfig.getInstance().setInitConfig(config);
        PersistentConfig.getInstance().savePassword(passwordField.getText(), config.getLoginName());
        Config finalOldConfig = oldConfig;
        Config finalConfig = config;
        ProgressManager.getInstance().run(new Task.Backgroundable(null, PluginConstant.PLUGIN_NAME + " Apply Config", false) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                ApplicationManager.getApplication().getMessageBus().syncPublisher(ConfigNotifier.TOPIC).change(finalOldConfig, finalConfig);
            }
        });
    }

    public void process(Config config) {
        if (config.getVersion() == null) {
            config.setVersion(Constant.PLUGIN_CONFIG_VERSION_3);
        }
        config.setLoginName(userNameField.getText());
        config.setFilePath(fileFolderBtn.getText());
        config.setCodeType(codeComboBox.getSelectedItem().toString());
        config.setUrl(webComboBox.getSelectedItem().toString());
        config.setUpdate(updateCheckBox.isSelected());
        config.setCustomCode(customCodeBox.isSelected());
        config.setCustomFileName(fileNameEditor.getDocument().getText());
        config.setCustomTemplate(templateEditor.getDocument().getText());
        config.setFormatLevelColour(easyLabel.getForeground(), mediumLabel.getForeground(), hardLabel.getForeground());
        config.setEnglishContent(englishContentBox.isSelected());
        config.setCookie(cookieCheckBox.isSelected());
        config.setQuestionEditor(questionEditorBox.getSelectedItem().toString());
        config.setMultilineComment(multilineCheckBox.isSelected());
        config.setHtmlContent(htmlContentCheckBox.isSelected());
        config.setShowTopics(showTopicsCheckBox.isSelected());
        config.setShowToolIcon(showToolIconCheckBox.isSelected());
        config.setConvergeEditor(convergeEditorCheckBox.isSelected());
        config.setShowQuestionEditorSign(showEditorSignCheckBox.isSelected());
    }


    public void reset() {
        loadSetting();
    }

    public void disposeUIResources() {
        if (this.fileNameEditor != null) {
            EditorFactory.getInstance().releaseEditor(this.fileNameEditor);
            this.fileNameEditor = null;
        }
        if (this.templateEditor != null) {
            EditorFactory.getInstance().releaseEditor(this.templateEditor);
            this.templateEditor = null;
        }
        if (this.templateHelpEditor != null) {
            EditorFactory.getInstance().releaseEditor(this.templateHelpEditor);
            this.templateHelpEditor = null;
        }
    }
}
