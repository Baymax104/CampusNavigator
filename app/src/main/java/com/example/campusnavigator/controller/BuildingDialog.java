package com.example.campusnavigator.controller;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.campusnavigator.R;
import com.lxj.xpopup.core.AttachPopupView;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/11/9 17:15
 * @Version 1
 */
public class BuildingDialog extends AttachPopupView {

    public BuildingDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.layout_search_building;
    }
}
