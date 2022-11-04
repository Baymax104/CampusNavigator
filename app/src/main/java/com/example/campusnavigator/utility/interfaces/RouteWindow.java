package com.example.campusnavigator.utility.interfaces;

import android.view.MotionEvent;

import com.amap.api.maps.AMap;
import com.example.campusnavigator.controller.Mode;

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
    void autoGestureControl(MotionEvent latLng, AMap map, Mode mode);
}
