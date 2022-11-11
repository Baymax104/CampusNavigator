package com.example.campusnavigator.utility.interfaces;

import android.view.MotionEvent;
import android.widget.ImageView;

import com.amap.api.maps.AMap;
import com.example.campusnavigator.model.Mode;
import com.example.campusnavigator.window.Window;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/11/2 23:24
 * @Version 1
 */
public interface RouteWindow {
    void openBox();
    void closeBox();

    default void autoGestureControl(MotionEvent latLng, AMap map, Window window) {
        float touchY = latLng.getRawY();
        int windowY = window.getWindowY();
        // 触摸起始点位于弹窗外侧，关闭弹窗
        if (latLng.getAction() == MotionEvent.ACTION_DOWN && touchY < windowY) {
            this.closeBox();
            map.getUiSettings().setAllGesturesEnabled(true);

        } else if (touchY >= windowY) { // 触摸点位于弹窗内侧
            // 轨迹位于外侧时，由于起始点必定不在外侧，所以保持false状态
            map.getUiSettings().setAllGesturesEnabled(false);
        }
    }
}
