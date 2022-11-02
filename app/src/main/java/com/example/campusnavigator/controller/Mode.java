package com.example.campusnavigator.controller;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/26 17:34
 * @Version 1
 */
public class Mode {
    public enum M {
        DEFAULT, // 初始状态
        S_SELECT, // 单点地图选点
        S_SELECT_CLICK, // Marker点击单点选点
        S_ROUTE_OPEN, // 单点显示路径，结果弹窗打开
        S_ROUTE_CLOSE, // 单点显示路径，结果弹窗关闭
        M_SELECT, // 多点地图选点
        M_ROUTE_OPEN, // 多点显示路径，结果弹窗打开
        M_ROUTE_CLOSE // 多点显示路径，结果弹窗关闭
    }
    private M mode = M.DEFAULT;

    public void change(M mode) {
        this.mode = mode;
        log();
    }

    public M mode() {
        return mode;
    }

    public void log() {
        Log.i("CamNav-Mode", "Current Mode=" + mode);
    }

    public boolean is(@NonNull Mode.M m) {
        return m == this.mode;
    }

    public boolean isSingleRoute() {
        return mode == M.S_ROUTE_OPEN || mode == M.S_ROUTE_CLOSE;
    }

    public boolean isMultiRoute() {
        return mode == M.M_ROUTE_OPEN || mode == M.M_ROUTE_CLOSE;
    }

    public void setDefault() {
        mode = M.DEFAULT;
        log();
    }

}
