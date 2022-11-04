package com.example.campusnavigator.controller;

import android.util.Log;

import androidx.annotation.NonNull;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/26 17:34
 * @Version 1
 */
public class Mode {
    private M mode;

    public Mode() {
        mode = M.DEFAULT;
        log();
    }

    public void changeTo(M mode) {
        this.mode = mode;
        log();
    }

    public M getState() {
        return mode;
    }

    public void log() {
        Log.i("CamNav-Mode", "Current Mode=" + mode);
    }

    public boolean is(@NonNull M m) {
        return mode == m;
    }

    public boolean isSingleRoute() {
        return mode == M.S_ROUTE_OPEN || mode == M.S_ROUTE_CLOSE;
    }

    public boolean isMultiRoute() {
        return mode == M.M_ROUTE_OPEN || mode == M.M_ROUTE_CLOSE;
    }

    public boolean isRouteOpen() {
        return mode == M.S_ROUTE_OPEN || mode == M.M_ROUTE_OPEN;
    }
}
