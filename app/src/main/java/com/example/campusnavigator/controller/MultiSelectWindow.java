package com.example.campusnavigator.controller;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.campusnavigator.R;

/**
 * @Description 多点选择弹窗对象
 * @Author John
 * @email
 * @Date 2022/9/26 10:35
 * @Version 1
 */
public class MultiSelectWindow extends Window {
    private Button test;

    public MultiSelectWindow(Context context, ViewGroup parent) {
        super(R.layout.layout_multi_select_window, context, parent);
        test = rootView.findViewById(R.id.test_button);
    }

    public void setTestListener(View.OnClickListener listener) {
        test.setOnClickListener(listener);
    }
}
