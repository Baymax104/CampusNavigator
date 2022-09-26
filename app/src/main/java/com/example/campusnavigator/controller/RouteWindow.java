package com.example.campusnavigator.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.campusnavigator.R;
import com.example.campusnavigator.model.Position;
import com.example.campusnavigator.utility.List;
import com.example.campusnavigator.utility.OverlayManager;
import com.example.campusnavigator.utility.Tuple;

import java.util.Locale;

/**
 * @Description 单点路径结果弹窗对象
 * @Author John
 * @email
 * @Date 2022/9/26 9:58
 * @Version 1
 */
public class RouteWindow extends Window {
    private LinearLayout routeContainer;

    // 上方信息窗口
    private ImageView expendButton;
    private TextView destTxt;

    // 下方方案窗口
    private View routePlanBox;
    private GridLayout planGroup;
    private View selectedPlanView;


    public RouteWindow(Context context, ViewGroup parent) {
        super(R.layout.layout_route_window, context, parent);
        routeContainer = rootView.findViewById(R.id.route_container);
        // 获取布局对象
        expendButton = routeContainer.findViewById(R.id.expend_button);
        destTxt = routeContainer.findViewById(R.id.dest_name);

        routePlanBox = LayoutInflater.from(context).inflate(R.layout.layout_route_plan_box, routeContainer, false);
        planGroup = routePlanBox.findViewById(R.id.plan_group);
        routeContainer.addView(routePlanBox);

        // 设置默认选中方案View样式
        selectedPlanView = planGroup.getChildAt(0);
        selectedPlanView.setBackgroundResource(R.drawable.shape_plan_item_bg_selected);
    }

    public int getWindowY() {
        int[] locationOnScreen = new int[2];
        rootView.getLocationOnScreen(locationOnScreen);
        return locationOnScreen[1];
    }

    public void setExpendButtonListener(View.OnClickListener listener) {
        expendButton.setOnClickListener(listener);
    }

    public void setExpendButtonUp(boolean isUp) {
        if (isUp) {
            expendButton.setImageResource(R.drawable.expend_arrow_up);
        } else {
            expendButton.setImageResource(R.drawable.expend_arrow_down);
        }
    }

    public void setDestName(String name) {
        if (destTxt != null) {
            destTxt.setText(name);
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

    public int getPlanCount() {
        return planGroup.getChildCount();
    }

    public void setPlanListener(int i, View.OnClickListener listener) {
        View child = planGroup.getChildAt(i);
        child.setOnClickListener(listener);
    }

    public void setPlansInfo(@NonNull List<Double> allTimes, @NonNull List<Double> allDistances) {
        if (allTimes.length() == 0 || allDistances.length() == 0) {
            return;
        }
        for (int i = 0; i < planGroup.getChildCount(); i++) {
            View child = planGroup.getChildAt(i);
            TextView timeTxt = child.findViewById(R.id.plan_time);
            TextView distanceTxt = child.findViewById(R.id.plan_distance);
            int time = allTimes.get(i).intValue();
            int distance = allDistances.get(i).intValue();
            timeTxt.setText(String.format(Locale.CHINA, "%d分钟", time));
            distanceTxt.setText(String.format(Locale.CHINA, "%d米", distance));
        }
    }

    public void displayPlan(@NonNull List<List<Tuple<Position, Position>>> plans,
                            int selected,
                            @NonNull Tuple<Position, Position> attachToMe,
                            @NonNull OverlayManager operator) {
        // 设置选中按钮
        setSelectedPlan(selected);

        // 绘制路线
        List<Tuple<Position, Position>> route = plans.get(selected);
        showRoutes(route, attachToMe, operator);
    }

    private void setSelectedPlan(int i) {
        int last = planGroup.indexOfChild(selectedPlanView);
        if (i >= 0 && i < planGroup.getChildCount() && last != i) {
            selectedPlanView.setBackgroundResource(R.drawable.shape_plan_item_bg_normal);
            selectedPlanView = planGroup.getChildAt(i);
            selectedPlanView.setBackgroundResource(R.drawable.shape_plan_item_bg_selected);
        }
    }

    private void showRoutes(@NonNull List<Tuple<Position, Position>> route,
                           @NonNull Tuple<Position, Position> attachToMe,
                           @NonNull OverlayManager operator) {
        operator.removeLines();
        for (int i = 0; i < route.length(); i++) {
            Tuple<Position, Position> p = route.get(i);
            if (i == 0) {
                operator.drawLine(p.first, p.second);
            } else {
                operator.drawLine(p.second);
            }
        }
        operator.drawLine(attachToMe.first, attachToMe.second);
    }
}
