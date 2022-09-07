package com.example.campusnavigator.controller;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/5 20:27
 * @Version 1
 */
public interface OnSpotSelectListener {
    /**
     1:单点地图选点
     2:单点显示路径
     */
    void setMapState(int modeCode);
    void showRoute(String name);
}
