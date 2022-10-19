package com.example.campusnavigator.controller;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/26 17:34
 * @Version 1
 */
public enum Mode {
    DEFAULT, // 初始状态
    SINGLE_SELECT, // 单点地图选点
    SINGLE_ROUTE_OPEN, // 单点显示路径，结果弹窗打开
    SINGLE_ROUTE_CLOSE, // 单点显示路径，结果弹窗关闭
    MULTI_SELECT, // 多点地图选点
    MULTI_ROUTE_OPEN, // 多点显示路径，结果弹窗打开
    MULTI_ROUTE_CLOSE // 多点显示路径，结果弹窗关闭
}
