package com.example.campusnavigator.model;

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

    public boolean isRouteOpen() {
        return mode == M.S_ROUTE || mode == M.M_ROUTE;
    }
}
