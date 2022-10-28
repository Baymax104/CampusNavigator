package com.example.campusnavigator.window;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @Description 弹窗对象父类
 * @Author John
 * @email
 * @Date 2022/9/26 10:36
 * @Version 1
 */
public abstract class Window {
    protected Context context;
    protected ViewGroup parent;
    protected View rootView;
    protected boolean isOpen = false;

    public Window(int rootViewId, Context context, ViewGroup parent) {
        this.context = context;
        this.parent = parent;
        rootView = LayoutInflater.from(context).inflate(rootViewId, parent, false);
    }

    public void open() {
        if (rootView != null) {
            parent.addView(rootView);
            isOpen = true;
        }
    }

    public void close() {
        if (rootView != null) {
            parent.removeView(rootView);
            isOpen = false;
        }
    }

    public boolean isOpen() {
        return isOpen;
    }

    public static void transition(Window close, Window open) {
        close.close();
        open.open();
    }
}
