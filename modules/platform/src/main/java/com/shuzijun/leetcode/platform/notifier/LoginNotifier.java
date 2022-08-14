package com.shuzijun.leetcode.platform.notifier;

import com.intellij.openapi.project.Project;

/**
 * @author shuzijun
 */
public interface LoginNotifier {

    void login(Project project, String host);

    void logout(Project project, String host);
}
