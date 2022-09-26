package com.example.campusnavigator.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.campusnavigator.R;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/26 9:58
 * @Version 1
 */
public class RouteWindowManager extends WindowManager {
    private LinearLayout routeContainer;
    // 上方信息窗口
    private ImageView expendButton;
    private TextView destTxt;

    // 下方方案窗口
    private View routePlanBox;
    private GridLayout planGroup;


    public RouteWindowManager(Context context, ViewGroup parent) {
        super(R.layout.layout_route_window, context, parent);
        routeContainer = rootView.findViewById(R.id.route_container);

        expendButton = routeContainer.findViewById(R.id.expend_button);
        destTxt = routeContainer.findViewById(R.id.dest_name);

        routePlanBox = LayoutInflater.from(context).inflate(R.layout.layout_route_plan_box, routeContainer, false);
        planGroup = routePlanBox.findViewById(R.id.plan_group);
        routeContainer.addView(routePlanBox);
    }

    public void setExpendButtonListener(View.OnClickListener listener) {
        expendButton.setOnClickListener(listener);
    }

    public int getWindowY() {
        int[] locationOnScreen = new int[2];
        rootView.getLocationOnScreen(locationOnScreen);
        return locationOnScreen[1];
    }

    public void setExpendButtonUp(boolean isUp) {
        if (isUp) {
            expendButton.setImageResource(R.drawable.expend_arrow_up);
        } else {
            expendButton.setImageResource(R.drawable.expend_arrow_down);
        }
    }

    public void openPlanBox() {
        if (routePlanBox != null && routeContainer.findViewById(R.id.route_plan_box) == null) {
            routeContainer.addView(routePlanBox);
        }
    }

    public void closePlanBox() {
        if (routePlanBox != null && routeContainer.findViewById(R.id.route_plan_box) != null) {
            routeContainer.removeView(routePlanBox);
        }
    }

    public void setDestName(String name) {
        if (destTxt != null) {
            destTxt.setText(name);
        }
    }

    public GridLayout getPlanGroup() {
        return planGroup;
    }
}
