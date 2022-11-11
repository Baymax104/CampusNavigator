package com.example.campusnavigator.utility.helpers;

import android.content.Context;
import android.view.View;

import com.example.campusnavigator.model.Mode;
import com.example.campusnavigator.controller.PrivacyConfirmDialog;
import com.example.campusnavigator.controller.SpotSearchDialog;
import com.example.campusnavigator.controller.BuildingDialog;
import com.example.campusnavigator.model.Position;
import com.example.campusnavigator.model.SpotProvider;
import com.example.campusnavigator.utility.interfaces.SingleSelectListener;
import com.lxj.xpopup.XPopup;

/**
 * @Description 对话框帮助类
 * @Author John
 * @email
 * @Date 2022/9/5 14:37
 * @Version 1
 */
public class DialogHelper {

    private DialogHelper() {
    }

    public static void showSpotSearchDialog(Context context,
                                            Mode mode,
                                            SpotProvider provider,
                                            SingleSelectListener listener,
                                            Position... selectedSpot) {
        new XPopup.Builder(context)
                .asCustom(new SpotSearchDialog(context, mode, provider, listener, selectedSpot))
                .show();
    }

    public static void showPrivacyConfirmDialog(Context context) {
        new XPopup.Builder(context)
                .isDestroyOnDismiss(true)
                .asCustom(new PrivacyConfirmDialog(context))
                .show();
    }

    public static void showBuildingDialog(Context context, View attachView) {
        new XPopup.Builder(context)
                .atView(attachView)
                .hasShadowBg(false)
                .isTouchThrough(true)
                .asCustom(new BuildingDialog(context))
                .show();
    }
}
