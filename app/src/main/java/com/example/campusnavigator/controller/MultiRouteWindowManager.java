package com.example.campusnavigator.controller;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.campusnavigator.R;

import java.util.Locale;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/26 10:57
 * @Version 1
 */
public class MultiRouteWindowManager extends WindowManager {
    private TextView timeInfo;
    private TextView distanceInfo;

    public MultiRouteWindowManager(Context context, ViewGroup parent) {
        super(R.layout.layout_multi_route_window, context, parent);
        timeInfo = rootView.findViewById(R.id.time_info);
        distanceInfo = rootView.findViewById(R.id.distance_info);
    }

    public void setDistAndTimeInfo(Double time, Double dist) {
        int t = time.intValue();
        int d = dist.intValue();
        if (timeInfo != null && distanceInfo != null) {
            timeInfo.setText(String.format(Locale.CHINA, "%d分钟", t));
            distanceInfo.setText(String.format(Locale.CHINA, "%d米", d));
        }
    }
}
