package com.example.campusnavigator.window;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.example.campusnavigator.model.M;
import com.example.campusnavigator.model.Mode;

/**
 * @Description 弹窗对象父类
 * @Author John
 * @email
 * @Date 2022/9/26 10:36
 * @Version 1
 */
public abstract class Window {
    protected final Context context;
    protected final ViewGroup parent;
    protected final View rootView;
    protected final M mode;

    public interface OnClickListener {
        void onClick(View view);
    }

    public Window(int rootViewId, M mode, Context context, ViewGroup parent) {
        this.context = context;
        this.parent = parent;
        this.mode = mode;
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

    public int getWindowY() {
        int[] locationOnScreen = new int[2];
        rootView.getLocationOnScreen(locationOnScreen);
        return locationOnScreen[1];
    }

    protected void registerListener(View view, Mode mode, OnClickListener listener) {
        view.setOnClickListener(v -> {
            if (mode.is(this.mode)) {
                listener.onClick(v);
            }
        });
    }

    public static void transition(@NonNull Window close, @NonNull Window open) {
        close.close();
        open.open();
    }
}
