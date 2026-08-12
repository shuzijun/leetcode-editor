package com.shuzijun.leetcode.plugin.manager;

import com.shuzijun.lc.model.CodeMetaData;
import com.shuzijun.lc.model.CodeSnippet;
import com.shuzijun.lc.model.QuestionView;
import com.shuzijun.lc.model.Session;
import com.shuzijun.lc.model.Solution;
import com.shuzijun.lc.model.Submission;
import com.shuzijun.lc.model.User;
import com.shuzijun.leetcode.plugin.model.*;
import com.shuzijun.leetcode.plugin.window.NavigatorTableData;

import javax.swing.*;

/**
 * @author shuzijun
 */
public interface NavigatorAction<T> {

    void updateUI();

    JPanel queryPanel();

    boolean selectedRow(String slug);

    void findClear();

    void findChange(String filterKey, boolean b, Tag tag);

    Find getFind();

    void sort(Sort sort);

    T getSelectedRowData();

    NavigatorTableData.PagePanel getPagePanel();

    PageInfo<T> getPageInfo();

    void loadData(String slug);

    void loadServiceData();

    void resetServiceData();

    boolean position(String slug);

    default void refreshData() {
        loadServiceData();
    }

    default void loading() {
    }

    default void loaded() {
    }

    default void loadFailed(Runnable retry) {
    }

    public static class Adapter<T> implements NavigatorAction {


        @Override
        public JPanel queryPanel() {
            return null;
        }

        @Override
        public boolean selectedRow(String slug) {
            return false;
        }

        @Override
        public void findClear() {

        }

        @Override
        public void updateUI() {

        }

        @Override
        public void findChange(String filterKey, boolean b, Tag tag) {

        }

        @Override
        public Find getFind() {
            return null;
        }

        @Override
        public void sort(Sort sort) {

        }

        @Override
        public T getSelectedRowData() {
            return null;
        }

        @Override
        public NavigatorTableData.PagePanel getPagePanel() {
            return null;
        }

        @Override
        public PageInfo<T> getPageInfo() {
            return null;
        }

        @Override
        public void loadData(String slug) {

        }

        @Override
        public void loadServiceData() {

        }

        @Override
        public void resetServiceData() {

        }

        @Override
        public boolean position(String slug) {
            return false;
        }
    }
}
