package com.example.campusnavigator.controller;

import com.example.campusnavigator.model.Position;
import com.example.campusnavigator.utility.List;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/5 10:02
 * @Version 1
 */
public interface RouteResultCallback {
    void showMultiDestRoute(List<Position[]> results);
}
