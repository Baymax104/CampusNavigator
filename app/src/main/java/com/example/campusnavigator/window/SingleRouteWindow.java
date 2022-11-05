package com.example.campusnavigator.window;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.amap.api.maps.AMap;
import com.example.campusnavigator.R;
import com.example.campusnavigator.controller.M;
import com.example.campusnavigator.controller.Mode;
import com.example.campusnavigator.model.Position;
import com.example.campusnavigator.utility.helpers.OverlayHelper;
import com.example.campusnavigator.utility.interfaces.RouteWindow;
import com.example.campusnavigator.utility.structures.List;

import java.util.Locale;

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

    // 下方方案窗口
    private final View routePlanBox; // 方案外层布局
    private final GridLayout planGroup; // 方案容器
    private int selected; // 当前选中的方案索引
    private View selectedPlanView; // 当前选中的方案布局

    public interface PlanSelectedListener {
        void onSelect(int selected);
    }


    private SingleRouteWindow(Context context, ViewGroup parent) {
        super(R.layout.layout_single_route_window, context, parent);
        routeContainer = rootView.findViewById(R.id.route_container);
        // 获取布局对象
        expendButton = routeContainer.findViewById(R.id.expend_button);
        destTxt = routeContainer.findViewById(R.id.route_info_dest_name);

        routePlanBox = LayoutInflater.from(context).inflate(R.layout.layout_single_route_plan_box, routeContainer, false);
        planGroup = routePlanBox.findViewById(R.id.route_plan_group);
        routeContainer.addView(routePlanBox);

        selected = -1;
        selectedPlanView = planGroup.getChildAt(0);
        selectedPlanView.setBackgroundResource(R.drawable.shape_plan_item_bg_selected);
    }

    public static SingleRouteWindow newInstance(Context context, ViewGroup parent) {
        SingleRouteWindow window = new SingleRouteWindow(context, parent);
        M.S_ROUTE_OPEN.setWindow(window);
        M.S_ROUTE_CLOSE.setWindow(window);
        return window;
    }

    public void bindExpendMode(Mode mode) {
        expendButton.setOnClickListener(v -> {
            if (mode.is(M.S_ROUTE_OPEN)) { // 处于打开状态，关闭planBox
                closeBox();
                mode.changeTo(M.S_ROUTE_CLOSE);
            } else if (mode.is(M.S_ROUTE_CLOSE)) { // 处于关闭状态，打开planBox
                openBox();
                mode.changeTo(M.S_ROUTE_OPEN);
            }
        });
    }

    @Override
    public void autoGestureControl(@NonNull MotionEvent latLng, AMap map, Mode mode) {
        float touchY = latLng.getRawY();
        int windowY = getWindowY();
        // 触摸起始点位于弹窗外侧，关闭弹窗
        if (latLng.getAction() == MotionEvent.ACTION_DOWN && touchY < windowY) {
            closeBox();
            map.getUiSettings().setAllGesturesEnabled(true);
            mode.changeTo(M.S_ROUTE_CLOSE);

        } else if (touchY >= windowY) { // 触摸点位于弹窗内侧
            // 轨迹位于外侧时，由于起始点必定不在外侧，所以保持false状态
            map.getUiSettings().setAllGesturesEnabled(false);
        }
    }

    public void setDestName(String name) {
        if (destTxt != null) {
            destTxt.setText(name);
        }
    }

    @Override
    public void openBox() {
        if (routePlanBox != null && routeContainer.findViewById(R.id.route_plan_box) == null) {
            routeContainer.addView(routePlanBox);
        }
        expendButton.setImageResource(R.drawable.expend_arrow_down);
    }

    @Override
    public void closeBox() {
        if (routePlanBox != null && routeContainer.findViewById(R.id.route_plan_box) != null) {
            routeContainer.removeView(routePlanBox);
        }
        expendButton.setImageResource(R.drawable.expend_arrow_up);
    }

    public int getPlanCount() {
        return planGroup.getChildCount();
    }

    public void refreshSelected() {
        this.selected = -1;
    }

    public void setPlanListener(int i, PlanSelectedListener listener) {
        View child = planGroup.getChildAt(i);
        child.setOnClickListener(v -> listener.onSelect(i));
    }

    public void setRouteInfo(@NonNull List<Double> times, @NonNull List<Double> distances) {
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

    public void displayPlan(@NonNull List<List<Position>> plans,
                            int selected,
                            @NonNull Position myLocation) {
        if (this.selected != selected) { // 若新选中的方案与当前选中不相同，则更新
            this.selected = selected;
            // 设置选中按钮
            setSelectedPlan(selected);

            // 绘制路线
            List<Position> route = plans.get(selected);
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
}
