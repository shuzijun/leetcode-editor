package com.shuzijun.leetcode.plugin.adapter.defaults.setting;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
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
import com.intellij.util.net.HttpProxyConfigurable;
import com.intellij.util.net.ProxyConfiguration;
import com.intellij.util.net.ProxySettings;
import com.shuzijun.leetcode.plugin.listener.ColorListener;
import com.shuzijun.leetcode.plugin.listener.ConfigNotifier;
import com.shuzijun.leetcode.plugin.listener.DonateListener;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.product.ProductProfiles;
import com.shuzijun.leetcode.plugin.setting.ConfigurationChangeDetector;
import com.shuzijun.leetcode.plugin.setting.PersistentConfig;
import com.shuzijun.leetcode.plugin.utils.BrowserUtils;
import com.shuzijun.leetcode.plugin.utils.DevelopmentTools;
import com.shuzijun.leetcode.plugin.utils.LogUtils;
import com.shuzijun.leetcode.plugin.utils.MTAUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.utils.SentryUtils;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Objects;

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
    private JButton sentryTestButton;


    private Editor fileNameEditor = null;
    private Editor templateEditor = null;
    private Editor templateHelpEditor = null;
    private String customFileName = "";
    private String customTemplate = "";

    private String savedPassword;
    private String savedPasswordUsername;
    private boolean passwordLoaded;
    private boolean passwordEdited;
    private boolean updatingPasswordField;
    private int passwordLoadGeneration;
    private boolean disposed;

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
        sentryTestButton.setVisible(DevelopmentTools.isEnabled());
        sentryTestButton.addActionListener(event -> {
            sentryTestButton.setEnabled(false);
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                try {
                    SentryUtils.submitErrorReport(
                            new IllegalStateException("LeetCode Editor Sentry settings test exception"),
                            "Sentry test submitted from plugin settings"
                    );
                } finally {
                    ApplicationManager.getApplication().invokeLater(
                            () -> sentryTestButton.setEnabled(true),
                            ignored -> disposed
                    );
                }
            });
        });
        passwordField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent event) {
                passwordChanged();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent event) {
                passwordChanged();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent event) {
                passwordChanged();
            }
        });
        refreshProxyState();
        proxyCheckBox.addActionListener(event -> {
            refreshProxyState();
            try {
                HttpProxyConfigurable.editConfigurable(mainPanel);
            } finally {
                refreshProxyState();
            }
        });

        templateConfigHelp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                BrowserUtils.browse("https://github.com/shuzijun/leetcode-editor/blob/master/doc/CustomCode.md");
            }
        });

        fileNameEditor = EditorFactory.getInstance().createEditor(EditorFactory.getInstance().createDocument(""), null, FileTypeManager.getInstance().getFileTypeByExtension("vm"), false);
        EditorSettings settings = fileNameEditor.getSettings();
        ((EditorEx) fileNameEditor).setOneLineMode(true);
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
        fileNameEditor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                customFileName = event.getDocument().getText();
            }
        });


        templateEditor = EditorFactory.getInstance().createEditor(EditorFactory.getInstance().createDocument(""), null, FileTypeManager.getInstance().getFileTypeByExtension("vm"), false);
        EditorSettings templateEditorSettings = templateEditor.getSettings();
        templateEditorSettings.setAdditionalLinesCount(0);
        templateEditorSettings.setAdditionalColumnsCount(0);
        templateEditorSettings.setLineMarkerAreaShown(false);
        templateEditorSettings.setVirtualSpace(false);
        JBScrollPane jbScrollPane = new JBScrollPane(templateEditor.getComponent());
        codeTemplate.add(jbScrollPane, BorderLayout.CENTER);
        templateEditor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                customTemplate = event.getDocument().getText();
            }
        });

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

        loadPassword(config);
        refreshProxyState();
    }

    private void loadPassword(Config config) {
        int generation = ++passwordLoadGeneration;
        String username = config == null ? null : config.getLoginName();
        savedPassword = null;
        savedPasswordUsername = username;
        passwordLoaded = config == null;
        passwordEdited = false;
        setPasswordField("");
        if (config == null) {
            return;
        }
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            String password = null;
            boolean loadSucceeded = false;
            try {
                password = PersistentConfig.getInstance().getPassword(username);
                loadSucceeded = true;
            } catch (RuntimeException exception) {
                LogUtils.LOG.warn("Failed to load the saved password", exception);
            }
            String loadedPassword = password;
            boolean passwordAvailable = loadSucceeded;
            ApplicationManager.getApplication().invokeLater(() -> {
                if (disposed || generation != passwordLoadGeneration) {
                    return;
                }
                if (!passwordAvailable) {
                    return;
                }
                savedPassword = StringUtils.defaultString(loadedPassword);
                passwordLoaded = true;
                if (!passwordEdited && Objects.equals(username, userNameField.getText())) {
                    setPasswordField(savedPassword);
                }
            }, ignored -> disposed || generation != passwordLoadGeneration);
        });
    }

    private void setPasswordField(String password) {
        updatingPasswordField = true;
        try {
            passwordField.setText(password);
        } finally {
            updatingPasswordField = false;
        }
    }

    private void passwordChanged() {
        if (!updatingPasswordField) {
            passwordEdited = true;
        }
    }

    private void refreshProxyState() {
        proxyCheckBox.setSelected(!(ProxySettings.getInstance().getProxyConfiguration() instanceof ProxyConfiguration.DirectProxy));
    }

    public JPanel getContentPane() {
        return mainPanel;
    }

    public boolean isModified() {
        Config config = PersistentConfig.getInstance().getInitConfig();
        Config currentState = config == null ? new Config() : config.clone();
        process(currentState);
        String currentPassword = String.valueOf(passwordField.getPassword());
        return ConfigurationChangeDetector.hasChanged(
                currentState, config, currentPassword, passwordForComparison(config, currentPassword));
    }

    public void apply() {
        PersistentConfig persistentConfig = PersistentConfig.getInstance();
        Config config = persistentConfig.getInitConfig();
        Config currentState = config == null ? new Config() : config.clone();
        process(currentState);
        String currentPassword = String.valueOf(passwordField.getPassword());
        if (!ConfigurationChangeDetector.hasChanged(
                currentState, config, currentPassword, passwordForComparison(config, currentPassword))) {
            return;
        }

        Config oldConfig = null;
        if (config == null) {
            config = currentState;
            config.setId(MTAUtils.getI(""));
        } else {
            oldConfig = config.clone();
            process(config);
        }
        File file = new File(config.getFilePath() + File.separator + PersistentConfig.PATH + File.separator);
        if (!file.exists()) {
            file.mkdirs();
        }
        persistentConfig.setInitConfig(config);
        boolean savePassword = config == currentState || passwordLoaded || passwordEdited;
        if (savePassword) {
            savedPassword = currentPassword;
            savedPasswordUsername = config.getLoginName();
            passwordLoaded = true;
            passwordEdited = false;
        }
        Config finalOldConfig = oldConfig;
        Config finalConfig = config;
        ProgressManager.getInstance().run(new Task.Backgroundable(null, ProductProfiles.current().pluginName() + " Apply Config", false) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                if (savePassword) {
                    persistentConfig.savePassword(currentPassword, finalConfig.getLoginName());
                }
                ApplicationManager.getApplication().getMessageBus().syncPublisher(ConfigNotifier.TOPIC).change(finalOldConfig, finalConfig);
            }
        });
    }

    private String passwordForComparison(Config config, String currentPassword) {
        if (config == null) {
            return null;
        }
        if (passwordLoaded && Objects.equals(savedPasswordUsername, config.getLoginName())) {
            return savedPassword;
        }
        return passwordEdited ? null : currentPassword;
    }

    public void process(Config config) {
        if (config.getVersion() == null) {
            config.setVersion(Constant.PLUGIN_CONFIG_VERSION_4);
        }
        config.setLoginName(userNameField.getText());
        config.setFilePath(fileFolderBtn.getText());
        config.setCodeType(codeComboBox.getSelectedItem().toString());
        config.setUrl(webComboBox.getSelectedItem().toString());
        config.setUpdate(updateCheckBox.isSelected());
        config.setCustomCode(customCodeBox.isSelected());
        config.setCustomFileName(customFileName);
        config.setCustomTemplate(customTemplate);
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
        disposed = true;
        passwordLoadGeneration++;
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
