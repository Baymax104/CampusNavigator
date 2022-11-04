package com.example.campusnavigator.controller;

import com.example.campusnavigator.window.Window;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/11/4 14:44
 * @Version 1
 */
public enum M {
    /**
     * DEFAULT:默认<br>
     * S_SELECT:单点选择<br>
     * S_SELECT_CLICK:Marker点击选择<br>
     * S_ROUTE_OPEN:单点路径窗口打开<br>
     * S_ROUTE_CLOSE:单点路径窗口关闭<br>
     * M_SELECT:多点选择<br>
     * M_ROUTE_OPEN:多点路径窗口打开<br>
     * M_ROUTE_CLOSE:多点路径窗口关闭<br>
     */
    DEFAULT,
    S_SELECT,
    S_SELECT_CLICK,
    S_ROUTE_OPEN,
    S_ROUTE_CLOSE,
    M_SELECT,
    M_ROUTE_OPEN,
    M_ROUTE_CLOSE;

    private Window window;

    public void setWindow(Window window) {
        this.window = window;
    }

    public Window getWindow() {
        return window;
    }
}
