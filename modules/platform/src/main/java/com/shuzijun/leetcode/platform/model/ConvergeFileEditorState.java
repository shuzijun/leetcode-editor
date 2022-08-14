package com.shuzijun.leetcode.platform.model;

import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import org.jetbrains.annotations.NotNull;

public interface ConvergeFileEditorState {

    public static class TabFileEditorState implements FileEditorState {

        public static TabFileEditorState TabFileEditorLoadState = new TabFileEditorState(true);
        private boolean load = false;

        public TabFileEditorState(boolean load) {
            this.load = load;
        }

        public boolean isLoad() {
            return load;
        }

        @Override
        public boolean canBeMergedWith(@NotNull FileEditorState otherState, @NotNull FileEditorStateLevel level) {
            return false;
        }


    }

    public static class TabSelectFileEditorState implements FileEditorState {

        private String name;

        private String childrenState;

        public TabSelectFileEditorState(String name) {
            this.name = name;
        }

        public TabSelectFileEditorState(String name, String childrenState) {
            this.name = name;
            this.childrenState = childrenState;
        }

        public String getName() {
            return name;
        }

        public String getChildrenState() {
            return childrenState;
        }

        @Override
        public boolean canBeMergedWith(@NotNull FileEditorState otherState, @NotNull FileEditorStateLevel level) {
            return false;
        }

    }

    public static class LoginState implements FileEditorState {

        public static LoginState NoLoginNoSelect = new LoginState(false, false);
        public static LoginState NoLoginSelect = new LoginState(false, true);
        public static LoginState LoginNoSelect = new LoginState(true, false);
        public static LoginState LoginSelect = new LoginState(true, true);
        private boolean login;
        private boolean select;

        public LoginState(boolean login, boolean select) {
            this.login = login;
            this.select = select;
        }

        public static LoginState getState(boolean login, boolean select) {
            if (login) {
                if (select) {
                    return LoginSelect;
                } else {
                    return LoginNoSelect;
                }
            } else {
                if (select) {
                    return NoLoginSelect;
                } else {
                    return NoLoginNoSelect;
                }
            }
        }

        public boolean isLogin() {
            return login;
        }

        public boolean isSelect() {
            return select;
        }

        @Override
        public boolean canBeMergedWith(@NotNull FileEditorState otherState, @NotNull FileEditorStateLevel level) {
            return false;
        }

    }
}
