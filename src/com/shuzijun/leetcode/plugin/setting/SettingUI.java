package com.shuzijun.leetcode.plugin.setting;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Config;
import com.shuzijun.leetcode.plugin.model.Constant;
import com.shuzijun.leetcode.plugin.utils.MTAUtils;
import com.shuzijun.leetcode.plugin.utils.PropertiesUtils;
import com.shuzijun.leetcode.plugin.utils.URLUtils;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * @author shuzijun
 */
public class SettingUI extends JDialog  {

    public JPanel mainPanel = new JBPanel();

    private JTextField userNameField = new JBTextField(10);
    private JPasswordField passwordField = new JBPasswordField();
    private TextFieldWithBrowseButton fileFolderBtn = new TextFieldWithBrowseButton();

    private JComboBox webComboBox = new JComboBox();
    private JComboBox codeComboBox = new JComboBox();
    private JCheckBox customCodeBox = new JCheckBox("Custom code template");
    private JCheckBox updateCheckBox = new JCheckBox("Check plugin update");
    private JCheckBox proxyCheckBox = new JCheckBox("proxy(HTTP Proxy)");

    private Editor fileNameEditor = null;
    private Editor templateEditor = null;
    private Editor templateHelpEditor =null;

    public SettingUI() {
        setContentPane(mainPanel);
    }


    public void createUI() {

        GridBagLayout giGridBagLayout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        mainPanel.setLayout(giGridBagLayout);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);

        addComponent(new JLabel("URL:"), constraints, 0, 0, 0, 0);

        webComboBox.addItem(URLUtils.leetcodecn);
        webComboBox.addItem(URLUtils.leetcode);
        webComboBox.setSelectedIndex(0);
        addComponent(webComboBox, constraints, 1, 0, 2, 0);

        addComponent(new JLabel("Code Type:"), constraints, 3, 0, 3, 0);

        for (CodeTypeEnum c : CodeTypeEnum.values()) {
            codeComboBox.addItem(c.getType());
        }
        codeComboBox.setSelectedIndex(0);
        addComponent(codeComboBox, constraints, 4, 0, 5, 0);

        updateCheckBox.setSelected(true);
        addComponent(updateCheckBox, constraints, 6, 0, 6, 0);


        addComponent(new JLabel("LoginName:"), constraints, 0, 1, 0, 1);

        addComponent(userNameField, constraints, 1, 1, 2, 1);

        addComponent(new JLabel("Password:"), constraints, 3, 1, 3, 1);

        addComponent(passwordField, constraints, 4, 1, 5, 1);

        proxyCheckBox.setSelected(false);
        addComponent(proxyCheckBox, constraints, 6, 1, 7, 1);

        addComponent(new JLabel("TempFilePath:"), constraints, 0, 2, 0, 2);

        //fileFolderBtn.setTextFieldPreferredWidth(45);
        fileFolderBtn.setText(System.getProperty("java.io.tmpdir"));
        fileFolderBtn.addBrowseFolderListener(new TextBrowseFolderListener(FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()) {
        });

        addComponent(fileFolderBtn, constraints, 1, 2, 5, 2);

        customCodeBox.setSelected(false);
        addComponent(customCodeBox, constraints, 6, 2, 7, 2);


        JLabel templateConfigHelp = new JLabel("CustomConfig(help)");
        templateConfigHelp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                BrowserUtil.browse("https://github.com/shuzijun/leetcode-editor/blob/master/CustomCode.md");
            }
        });
        templateConfigHelp.setForeground(new Color(88, 157, 246));
        addComponent(templateConfigHelp, constraints, 0, 3, 0, 3);
        addComponent(new JSeparator(), constraints, 1, 3, 8, 3);

        addComponent(new JLabel("CodeFileName:"), constraints, 0, 4, 0, 4);

        fileNameEditor = EditorFactory.getInstance().createEditor(EditorFactory.getInstance().createDocument(""), null, FileTypeManager.getInstance().getFileTypeByExtension("vm"), false);
        EditorSettings settings = fileNameEditor.getSettings();
        ((EditorImpl) fileNameEditor).setOneLineMode(true);

        settings.setAdditionalLinesCount(0); //额外的行
        settings.setAdditionalColumnsCount(0); //额外的列
        settings.setCaretRowShown(false); //
        settings.setFoldingOutlineShown(false); //折叠大纲
        settings.setIndentGuidesShown(false); //缩进
        settings.setLineMarkerAreaShown(false); //线性标记区域
        settings.setLineNumbersShown(false); //行号
        settings.setVirtualSpace(false); //虚拟空间
        settings.setAllowSingleLogicalLineFolding(false);//允许单逻辑行折叠
        settings.setAnimatedScrolling(false); //滚动
        settings.setAdditionalPageAtBottom(false); //底部附加
        settings.setAutoCodeFoldingEnabled(false); //代码自动折叠

        addComponent(fileNameEditor.getComponent(), constraints, 1, 4, 8, 4);

        constraints.anchor = GridBagConstraints.NORTHWEST;
        addComponent(new JLabel("CodeTemplate:"), constraints, 0, 5, 0, 5);

        templateEditor = EditorFactory.getInstance().createEditor(EditorFactory.getInstance().createDocument(""), null, FileTypeManager.getInstance().getFileTypeByExtension("vm"), false);
        EditorSettings templateEditorSettings = templateEditor.getSettings();
        templateEditorSettings.setAdditionalLinesCount(0); //额外的行
        templateEditorSettings.setAdditionalColumnsCount(0); //额外的列
        templateEditorSettings.setLineMarkerAreaShown(false); //线性标记区域
        templateEditorSettings.setVirtualSpace(false); //虚拟空间
        JBScrollPane jbScrollPane = new JBScrollPane(templateEditor.getComponent());
        jbScrollPane.setMaximumSize(new Dimension(150, 50));
        addComponent(jbScrollPane, constraints, 1, 5, 8, 5);


        addComponent(new JLabel("TemplateConstant:"), constraints, 0, 6, 0, 6);

        templateHelpEditor = EditorFactory.getInstance().createEditor(EditorFactory.getInstance().createDocument(PropertiesUtils.getInfo("template.variable", "{", "}")), null, FileTypeManager.getInstance().getFileTypeByExtension("vm"), true);

        EditorSettings templateHelpEditorSettings = templateHelpEditor.getSettings();
        templateHelpEditorSettings.setAdditionalLinesCount(0); //额外的行
        templateHelpEditorSettings.setAdditionalColumnsCount(0); //额外的列
        templateHelpEditorSettings.setLineMarkerAreaShown(false); //线性标记区域
        templateHelpEditorSettings.setLineNumbersShown(false); //行号
        templateHelpEditorSettings.setVirtualSpace(false); //虚拟空间

        addComponent(templateHelpEditor.getComponent(), constraints, 1, 6, 8, 6);

        Config config = PersistentConfig.getInstance().getInitConfig();
        if (config != null) {
            userNameField.setText(config.getLoginName());
            passwordField.setText(PersistentConfig.getInstance().getPassword());
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
            proxyCheckBox.setSelected(config.getProxy());
            customCodeBox.setSelected(config.getCustomCode());
            ApplicationManager.getApplication().runWriteAction(() -> {
                fileNameEditor.getDocument().setText(config.getCustomFileName());
                templateEditor.getDocument().setText(config.getCustomTemplate());
            });
        } else {
            ApplicationManager.getApplication().runWriteAction(() -> {
                fileNameEditor.getDocument().setText(Constant.CUSTOM_FILE_NAME);
                templateEditor.getDocument().setText(Constant.CUSTOM_TEMPLATE);
            });
        }
    }

    private void addComponent(Component component, GridBagConstraints constraints, int x, int y, int ex, int ey) {
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = ex - x + 1;
        constraints.gridheight = ey - y + 1;
        constraints.weightx = (ex - x + 1) * 0.1;
        constraints.weighty = (ey - y + 1) * 0.1;
        mainPanel.add(component, constraints);
    }

    public boolean isModified() {
        boolean modified = true;
        return modified;
    }

    public void apply() {
        Config config = PersistentConfig.getInstance().getInitConfig();
        if (config == null) {
            config = new Config();
            config.setId(MTAUtils.getI(""));
        }
        config.setVersion(Constant.PLUGIN_CONFIG_VERSION_2);
        config.setLoginName(userNameField.getText());
        config.setPassword("");
        config.setFilePath(fileFolderBtn.getText());
        config.setCodeType(codeComboBox.getSelectedItem().toString());
        config.setUrl(webComboBox.getSelectedItem().toString());
        config.setUpdate(updateCheckBox.isSelected());
        config.setProxy(proxyCheckBox.isSelected());
        config.setCustomCode(customCodeBox.isSelected());
        config.setCustomFileName(fileNameEditor.getDocument().getText());
        config.setCustomTemplate(templateEditor.getDocument().getText());
        File file = new File(config.getFilePath() + File.separator + PersistentConfig.PATH + File.separator);
        if (!file.exists()) {
            file.mkdirs();
        }
        PersistentConfig.getInstance().setInitConfig(config);
        PersistentConfig.getInstance().savePassword(passwordField.getText());
    }

    public void reset() {

    }

    @Override
    public JPanel getContentPane() {
        return mainPanel;
    }

    public void disposeUIResources(){
        if (this.fileNameEditor != null) {
            EditorFactory.getInstance().releaseEditor(this.fileNameEditor);
            this.fileNameEditor = null;
        }
        if (this.templateEditor != null) {
            EditorFactory.getInstance().releaseEditor(this.templateEditor);
            this.templateEditor = null;
        }
        if (this.templateHelpEditor!=null){
            EditorFactory.getInstance().releaseEditor(this.templateHelpEditor);
            this.templateHelpEditor = null;
        }
    }


}
