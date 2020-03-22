//package com.shuzijun.leetcode.plugin.actions;
//
//import com.intellij.openapi.actionSystem.AnActionEvent;
//import com.intellij.openapi.application.ApplicationManager;
//import com.shuzijun.leetcode.plugin.model.Config;
//
///**
// * @author shuzijun
// */
//public abstract class AbstractAsynAction extends AbstractAction {
//
//    @Override
//    public void actionPerformed(AnActionEvent anActionEvent, Config config) {
//        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
//            @Override
//            public void run() {
//                perform(anActionEvent, config);
//            }
//        });
//    }
//
//    public abstract void perform(AnActionEvent anActionEvent, Config config);
//}
