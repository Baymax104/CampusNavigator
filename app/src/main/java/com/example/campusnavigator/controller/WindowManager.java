package com.example.campusnavigator.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/26 10:36
 * @Version 1
 */
public abstract class WindowManager {
    protected Context context;
    protected ViewGroup parent;
    protected View rootView;
    protected int rootViewId;

    public WindowManager(int rootViewId, Context context, ViewGroup parent) {
        this.context = context;
        this.parent = parent;
        this.rootViewId = rootViewId;
        rootView = LayoutInflater.from(context).inflate(rootViewId, parent, false);
    }

    public void open() {
        if (rootView != null) {
            parent.addView(rootView);
        }
    }

    public void close() {
        if (rootView != null) {
            parent.removeView(rootView);
        }
    }
}
