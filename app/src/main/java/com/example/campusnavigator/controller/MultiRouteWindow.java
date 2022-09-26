package com.example.campusnavigator.controller;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.campusnavigator.R;
import com.example.campusnavigator.model.Position;
import com.example.campusnavigator.utility.List;
import com.example.campusnavigator.utility.OverlayManager;
import com.example.campusnavigator.utility.Tuple;

import java.util.Locale;

/**
 * @Description 多点路径结果弹窗对象
 * @Author John
 * @email
 * @Date 2022/9/26 10:57
 * @Version 1
 */
public class MultiRouteWindow extends Window {
    private TextView timeInfo;
    private TextView distanceInfo;

    public MultiRouteWindow(Context context, ViewGroup parent) {
        super(R.layout.layout_multi_route_window, context, parent);
        timeInfo = rootView.findViewById(R.id.time_info);
        distanceInfo = rootView.findViewById(R.id.distance_info);
    }

    public int getWindowY() {
        int[] locationOnScreen = new int[2];
        rootView.getLocationOnScreen(locationOnScreen);
        return locationOnScreen[1];
    }

    public void setRouteInfo(Double time, Double dist) {
        int t = time.intValue();
        int d = dist.intValue();
        if (timeInfo != null && distanceInfo != null) {
            timeInfo.setText(String.format(Locale.CHINA, "%d分钟", t));
            distanceInfo.setText(String.format(Locale.CHINA, "%d米", d));
        }
    }

    public void displayRoute(@NonNull List<Tuple<Position, Position>> route, OverlayManager operator) {
        operator.removeLines();
        for (int i = 0; i < route.length(); i++) {
            Tuple<Position, Position> p = route.get(i);
            if (i == 0) {
                operator.drawLine(p.first, p.second);
            } else {
                operator.drawLine(p.second);
            }
        }
    }
}
