package com.example.campusnavigator.utility.helpers;

import android.content.Context;

import com.example.campusnavigator.controller.PrivacyConfirmDialog;
import com.example.campusnavigator.controller.SpotSearchDialog;
import com.example.campusnavigator.model.Position;
import com.example.campusnavigator.utility.callbacks.OnSpotSelectListener;
import com.lxj.xpopup.XPopup;

/**
 * @Description 对话框帮助类
 * @Author John
 * @email
 * @Date 2022/9/5 14:37
 * @Version 1
 */
public class DialogHelper {

    public static void showSpotSearchDialog(Context context, OnSpotSelectListener listener) {
        new XPopup.Builder(context)
                .asCustom(new SpotSearchDialog(context, listener))
                .show();
    }

    public static void showPrivacyConfirmDialog(Context context) {
        new XPopup.Builder(context)
                .isDestroyOnDismiss(true)
                .asCustom(new PrivacyConfirmDialog(context))
                .show();
    }

    public static void showSpotSearchDialog(Context context, Position position, OnSpotSelectListener listener) {
        new XPopup.Builder(context)
                .asCustom(new SpotSearchDialog(context, position, listener))
                .show();
    }
}
