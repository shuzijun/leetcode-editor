package com.shuzijun.leetcode.plugin.setting;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.shuzijun.leetcode.plugin.model.CodeTypeEnum;
import com.shuzijun.leetcode.plugin.model.Config;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * @author shuzijun
 */
public class SettingUI extends JDialog {

    public JPanel mainPanel = new JBPanel();


    private JTextField userNameField = new JBTextField(10);
    private JPasswordField passwordField = new JBPasswordField();
    private TextFieldWithBrowseButton fileFolderBtn = new TextFieldWithBrowseButton();

    private JComboBox webComboBox = new JComboBox();
    private JComboBox codeComboBox = new JComboBox();

    public SettingUI() {
        setContentPane(mainPanel);
    }


    public void createUI() {
        mainPanel.setLayout(new GridLayout(10, 0));

        JPanel webMainPane = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JPanel webPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        webPanel.add(new JLabel("URL:"));
        webComboBox.addItem("leetcode-cn.com");
        webComboBox.addItem("leetcode.com");
        webComboBox.setSelectedIndex(0);
        webPanel.add(webComboBox);

        JPanel codePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        codePanel.add(new JLabel("code type:"));
        for (CodeTypeEnum c : CodeTypeEnum.values()) {
            codeComboBox.addItem(c.getType());
        }
        codeComboBox.setSelectedIndex(0);
        codePanel.add(codeComboBox);

        webMainPane.add(webPanel);
        webMainPane.add(codePanel);

        JPanel loginMainPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel userNamePanel = new JPanel();
        userNamePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        userNamePanel.add(new JLabel("loginName:"));
        userNamePanel.add(userNameField);

        JPanel passwordPanel = new JPanel();
        passwordPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        passwordPanel.add(new JLabel("password:"));
        passwordField.setColumns(10);
        passwordPanel.add(passwordField);

        loginMainPane.add(userNamePanel);
        loginMainPane.add(passwordPanel);

        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filePanel.add(new JLabel("temp file path:"));
        fileFolderBtn.setTextFieldPreferredWidth(45);
        fileFolderBtn.setText(System.getProperty("java.io.tmpdir"));
        fileFolderBtn.addBrowseFolderListener(new TextBrowseFolderListener(FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()) {
        });
        filePanel.add(fileFolderBtn);


        mainPanel.add(webMainPane);
        mainPanel.add(loginMainPane);
        mainPanel.add(filePanel);


        Config config = PersistentConfig.getInstance().getInitConfig();
        if (config != null) {
            userNameField.setText(config.getLoginName());
            passwordField.setText(config.getPassword());
            if (StringUtils.isNotBlank(config.getFilePath())) {
                fileFolderBtn.setText(config.getFilePath());
            }
            if (StringUtils.isNotBlank(config.getCodeType())) {
                codeComboBox.setSelectedItem(config.getCodeType());
            }
            if (StringUtils.isNotBlank(config.getUrl())) {
                webComboBox.setSelectedItem(config.getUrl());
            }
        }

    }

    public boolean isModified() {
        boolean modified = true;
        return modified;
    }

    public void apply() {
        Config config = new Config();
        config.setLoginName(userNameField.getText());
        config.setPassword(passwordField.getText());
        config.setFilePath(fileFolderBtn.getText());
        config.setCodeType(codeComboBox.getSelectedItem().toString());
        config.setUrl(webComboBox.getSelectedItem().toString());
        File file = new File(config.getFilePath() + File.separator + PersistentConfig.PATH + File.separator);
        if (!file.exists()) {
            file.mkdirs();
        }
        PersistentConfig.getInstance().setInitConfig(config);


    }

    public void reset() {

    }

    @Override
    public JPanel getContentPane() {
        return mainPanel;
    }

}
