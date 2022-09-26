package com.example.campusnavigator.utility.callbacks;

import com.example.campusnavigator.controller.Mode;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/5 20:27
 * @Version 1
 */
public interface OnSpotSelectListener {
    void onMapStateChange(Mode mode);
    void onDestReceiveSuccess(String name);
    void onDestReceiveError(Exception e);
}