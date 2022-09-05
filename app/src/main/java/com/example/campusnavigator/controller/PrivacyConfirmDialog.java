package com.example.campusnavigator.controller;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.amap.api.maps.MapsInitializer;
import com.example.campusnavigator.R;
import com.lxj.xpopup.core.CenterPopupView;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/2 20:59
 * @Version 1
 */
public class PrivacyConfirmDialog extends CenterPopupView {
    private Context context;

    public PrivacyConfirmDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.dialog_privacy_confirm;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        TextView confirm = findViewById(R.id.privacy_confirm);
        TextView cancel = findViewById(R.id.privacy_cancel);
        confirm.setOnClickListener(view -> {
            MapsInitializer.updatePrivacyAgree(context, true);
            dismiss();
        });
        cancel.setOnClickListener(view -> {
            MapsInitializer.updatePrivacyAgree(context, false);
            dismiss();
        });
    }
}
