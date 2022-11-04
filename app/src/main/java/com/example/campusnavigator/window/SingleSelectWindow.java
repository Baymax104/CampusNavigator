package com.example.campusnavigator.window;

import android.content.Context;
import android.view.ViewGroup;

import com.example.campusnavigator.R;
import com.example.campusnavigator.controller.M;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/10/21 15:53
 * @Version 1
 */
public class SingleSelectWindow extends Window {

    private SingleSelectWindow(Context context, ViewGroup parent) {
        super(R.layout.layout_single_select_window, context, parent);
    }

    public static SingleSelectWindow newInstance(Context context, ViewGroup parent) {
        SingleSelectWindow window = new SingleSelectWindow(context, parent);
        M.S_SELECT.setWindow(window);
        return window;
    }
}
