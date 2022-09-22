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
     0:初始状态
     1:单点地图选点
     2:单点显示路径，路径结果弹窗打开
     3:单点显示路径，路径结果弹窗关闭
     4:多点地图选点
     5:多点显示路径
     */
    void onMapStateChange(int modeCode);
    void onDestReceiveSuccess(String name);
    void onDestReceiveError(Exception e);
}
