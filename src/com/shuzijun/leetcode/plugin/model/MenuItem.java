package com.shuzijun.leetcode.plugin.model;

import java.awt.event.MouseEvent;

/**
 * @author shuzijun
 */
public class MenuItem {

    private String title;

    private Object object;

    private Runnable runnable;

    private MouseEvent e;

    public MenuItem(String title, Runnable runnable) {
        this.title = title;
        this.object = object;
        this.runnable = runnable;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    public MouseEvent getE() {
        return e;
    }

    public void setE(MouseEvent e) {
        this.e = e;
    }

    @Override
    public String toString() {
        return title;
    }
}
