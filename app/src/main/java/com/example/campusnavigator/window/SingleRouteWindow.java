package com.example.campusnavigator.window;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.amap.api.maps.AMap;
import com.example.campusnavigator.R;
import com.example.campusnavigator.model.M;
import com.example.campusnavigator.model.Map;
import com.example.campusnavigator.model.Mode;
import com.example.campusnavigator.model.Position;
import com.example.campusnavigator.model.Route;
import com.example.campusnavigator.utility.helpers.OverlayHelper;
import com.example.campusnavigator.utility.interfaces.RouteWindow;
import com.example.campusnavigator.utility.structures.List;

import java.util.Locale;

import info.hoang8f.android.segmented.SegmentedGroup;

/**
 * @Description 单点路径结果弹窗对象
 * @Author John
 * @email
 * @Date 2022/9/26 9:58
 * @Version 1
 */
public class SingleRouteWindow extends Window implements RouteWindow {
    private final LinearLayout routeContainer;

    // 上方信息窗口
    private final ImageView expendButton; // 窗口展开/关闭按钮
    private final TextView destTxt; // 目的地名TextView
    private final SegmentedGroup waySegment; // 出行方式Segment

    // 下方方案窗口
    private final View routePlanBox; // 方案外层布局
    private final GridLayout planGroup; // 方案容器
    private int selected; // 当前选中的方案索引
    private View selectedPlanView; // 当前选中的方案布局
    private boolean boxOpened; // 控制box打开与关闭

    // 缓存当前结果
    private Position destPosition;
    private List<List<Position>> routes;

    public interface WayChangeListener {
        void onWayChange(Position dest, RadioGroup group, int checkedId);
    }


    private SingleRouteWindow(Context context, ViewGroup parent) {
        super(R.layout.window_single_route, context, parent);
        routeContainer = rootView.findViewById(R.id.route_container);
        // 获取布局对象
        expendButton = routeContainer.findViewById(R.id.expend_button);
        destTxt = routeContainer.findViewById(R.id.route_info_dest_name);
        waySegment = routeContainer.findViewById(R.id.segment_group);
        waySegment.check(R.id.segment_footway);

        routePlanBox = LayoutInflater.from(context).inflate(R.layout.box_single_route_plan, routeContainer, false);
        planGroup = routePlanBox.findViewById(R.id.route_plan_group);
        routeContainer.addView(routePlanBox);
        boxOpened = true;

        selected = -1;
        selectedPlanView = planGroup.getChildAt(0);
        selectedPlanView.setBackgroundResource(R.drawable.shape_plan_item_bg_selected);
    }

    public static SingleRouteWindow newInstance(Context context, ViewGroup parent) {
        SingleRouteWindow window = new SingleRouteWindow(context, parent);
        M.S_ROUTE.setWindow(window);
        return window;
    }

    public void setWayChangeListener(WayChangeListener listener) {
        waySegment.setOnCheckedChangeListener((group, checkedId) -> {
            listener.onWayChange(destPosition, group, checkedId);
        });
    }

    private void startExpendListener() {
        expendButton.setOnClickListener(v -> {
            if (boxOpened) { // 处于打开状态，关闭planBox
                closeBox();
            } else { // 处于关闭状态，打开planBox
                openBox();
            }
        });
    }

    private void startPlanListener(Position myPosition) {
        int count = planGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = planGroup.getChildAt(i);
            final int selected = i;
            child.setOnClickListener(v -> displayPlan(selected, myPosition));
        }
    }

    public void set(List<Route> results, Position dest, Position myPosition) {
        List<List<Position>> routes = Route.extractRoute(results);
        List<Double> times = Route.extractTime(results);
        List<Double> distances = Route.extractDist(results);

        // 设置信息
        selected = -1;
        this.routes = routes;
        setDestPosition(dest);
        setRouteInfo(times, distances);
        displayPlan(0, myPosition);
        // 开启窗口监听
        startPlanListener(myPosition);
        startExpendListener();
    }

    private void setDestPosition(Position destPosition) {
        this.destPosition = destPosition;
        if (destTxt != null) {
            destTxt.setText(destPosition.getName());
        }
    }

    private void setRouteInfo(@NonNull List<Double> times, @NonNull List<Double> distances) {
        if (times.length() == 0 || distances.length() == 0) {
            return;
        }
        for (int i = 0; i < planGroup.getChildCount(); i++) {
            View child = planGroup.getChildAt(i);
            TextView timeTxt = child.findViewById(R.id.route_plan_time_info);
            TextView distanceTxt = child.findViewById(R.id.route_plan_distance_info);
            int time = times.get(i).intValue();
            int distance = distances.get(i).intValue();
            timeTxt.setText(String.format(Locale.CHINA, "%d分钟", time));
            distanceTxt.setText(String.format(Locale.CHINA, "%d米", distance));
        }
    }

    private void displayPlan(int selected, @NonNull Position myLocation) {
        if (this.selected != selected) { // 若新选中的方案与当前选中不相同，则更新
            this.selected = selected;
            // 设置选中按钮
            setSelectedPlan(selected);

            // 绘制路线
            List<Position> route = routes.get(selected);
            showRoutes(route, myLocation);
        }
    }

    private void setSelectedPlan(int i) {
        if (i >= 0 && i < planGroup.getChildCount()) {
            selectedPlanView.setBackgroundResource(R.drawable.shape_plan_item_bg_normal);
            selectedPlanView = planGroup.getChildAt(i);
            selectedPlanView.setBackgroundResource(R.drawable.shape_plan_item_bg_selected);
        }
    }

    private void showRoutes(@NonNull List<Position> route,
                           @NonNull Position myLocation) {
        OverlayHelper.removeAllLines();
        for (Position p : route) {
            OverlayHelper.drawLine(p);
        }
        OverlayHelper.drawLine(myLocation);
    }

    @Override
    public void openBox() {
        if (routePlanBox != null && routeContainer.findViewById(R.id.route_plan_box) == null) {
            routeContainer.addView(routePlanBox);
            expendButton.setImageResource(R.drawable.expend_arrow_down);
            boxOpened = true;
        }
    }

    @Override
    public void closeBox() {
        if (routePlanBox != null && routeContainer.findViewById(R.id.route_plan_box) != null) {
            routeContainer.removeView(routePlanBox);
            expendButton.setImageResource(R.drawable.expend_arrow_up);
            boxOpened = false;
        }
    }

    public void initChecked() {
        waySegment.check(R.id.segment_footway);
    }

}
