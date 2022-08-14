package com.shuzijun.leetcode.extension;

import com.shuzijun.leetcode.platform.model.Find;
import com.shuzijun.leetcode.platform.model.PageInfo;
import com.shuzijun.leetcode.platform.model.Sort;
import com.shuzijun.leetcode.platform.model.Tag;

import javax.swing.*;

/**
 * 导航窗口响应事件定义
 * 实现导航窗口时需要根据需要进行实现
 *
 * @author shuzijun
 */
public interface NavigatorAction<T> {

    /**
     * 更新UI
     */
    void updateUI();

    /**
     * 获取导航窗口查询面板
     *
     * @return
     */
    JPanel queryPanel();

    /**
     * 跟进标记选择事件
     *
     * @param slug
     * @return
     */
    boolean selectedRow(String slug);

    /**
     * 清除查询
     */
    void findClear();


    /**
     * 添加查询数据
     *
     * @param filterKey
     * @param b
     * @param tag
     */
    void findChange(String filterKey, boolean b, Tag tag);

    /**
     * 获取过滤数据
     *
     * @return
     */
    Find getFind();

    /**
     * 排序事件
     *
     * @param sort
     */
    void sort(Sort sort);

    /**
     * 获取页面内选择的数据
     *
     * @return
     */
    T getSelectedRowData();

    /**
     * 获取导航内分页面板
     *
     * @return
     */
    NavigatorPagePanel getPagePanel();

    /**
     * 获取分页数据
     *
     * @return
     */
    PageInfo<T> getPageInfo();

    /**
     * 加载数据
     *
     * @param slug 加载时选中的标记
     */
    void loadData(String slug);

    /**
     * 加载服务数据
     */
    void loadServiceData();

    /**
     * 重进加载数据
     */
    void resetServiceData();

    /**
     * 跟进标记定位位置
     *
     * @param slug
     * @return
     */
    boolean position(String slug);

    public static class Adapter<T> implements NavigatorAction<T> {


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
        public NavigatorPagePanel getPagePanel() {
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
