package com.example.campusnavigator.window;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.maps.AMap;
import com.example.campusnavigator.R;
import com.example.campusnavigator.model.M;
import com.example.campusnavigator.model.Mode;
import com.example.campusnavigator.model.Position;
import com.example.campusnavigator.model.Route;
import com.example.campusnavigator.utility.adapters.MultiSpotAdapter;
import com.example.campusnavigator.utility.helpers.OverlayHelper;
import com.example.campusnavigator.utility.interfaces.RouteWindow;
import com.example.campusnavigator.utility.structures.List;
import com.example.campusnavigator.utility.structures.Stack;

import java.util.Locale;

/**
 * @Description 多点路径结果弹窗对象
 * @Author John
 * @email
 * @Date 2022/9/26 10:57
 * @Version 1
 */
public class MultiRouteWindow extends Window implements RouteWindow {
    private final LinearLayout multiRouteContainer;

    // 上方信息窗口
    private final TextView timeInfo;
    private final TextView distanceInfo;
    private final ImageView expendButton;

    // 下方地点列表
    private final View multiSpotBox;
    private final MultiSpotAdapter adapter;

    private MultiRouteWindow(Context context, ViewGroup parent) {
        super(R.layout.window_multi_route, context, parent);
        multiRouteContainer = rootView.findViewById(R.id.multi_route_container);

        timeInfo = multiRouteContainer.findViewById(R.id.multi_route_time_info);
        distanceInfo = multiRouteContainer.findViewById(R.id.multi_route_distance_info);
        expendButton = multiRouteContainer.findViewById(R.id.expend_button);

        multiSpotBox = LayoutInflater.from(context).inflate(R.layout.box_multi_route_spot, parent, false);

        RecyclerView multiSpotList = multiSpotBox.findViewById(R.id.multi_route_spot_list);
        adapter = new MultiSpotAdapter();
        multiSpotList.setAdapter(adapter);
        LinearLayoutManager manager = new LinearLayoutManager(context);
        multiSpotList.setLayoutManager(manager);
    }

    public static MultiRouteWindow newInstance(Context context, ViewGroup parent) {
        MultiRouteWindow window = new MultiRouteWindow(context, parent);
        M.M_ROUTE_OPEN.setWindow(window);
        M.M_ROUTE_CLOSE.setWindow(window);
        return window;
    }

    public void startExpendListener(Mode mode) {
        expendButton.setOnClickListener(v -> {
            if (mode.is(M.M_ROUTE_OPEN)) {
                closeBox();
                mode.changeTo(M.M_ROUTE_CLOSE);
            } else if (mode.is(M.M_ROUTE_CLOSE)) {
                openBox();
                mode.changeTo(M.M_ROUTE_OPEN);
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
            mode.changeTo(M.M_ROUTE_CLOSE);

        } else if (touchY >= windowY) { // 触摸点位于弹窗内侧
            // 轨迹位于外侧时，由于起始点必定不在外侧，所以保持false状态
            map.getUiSettings().setAllGesturesEnabled(false);
        }
    }

    public void set(List<Route> results, Stack<Position> destBuffer) {
        // 提取结果
        List<Position> route = Route.combineRoute(results);
        List<Double> times = Route.extractTime(results);
        List<Double> distances = Route.extractDist(results);

        // 设置文字信息
        setInfo(destBuffer, times, distances);
        // 展示路线
        displayRoute(route);

    }

    private void setInfo(@NonNull Stack<Position> destBuffer, List<Double> times, List<Double> dists) {
        List<Position> dests = destBuffer.toList(true);
        List.reverse(times);
        List.reverse(dists);
        setRouteInfo(times, dists);
        setSpotInfo(dests, times, dists);
    }

    private void setRouteInfo(List<Double> times, List<Double> dists) {
        Double td = 0.0;
        Double dd = 0.0;
        for (Double t : times) {
            td += t;
        }
        for (Double d : dists) {
            dd += d;
        }
        int t = td.intValue();
        int d = dd.intValue();
        if (timeInfo != null && distanceInfo != null) {
            timeInfo.setText(String.format(Locale.CHINA, "预计步行%d分钟", t));
            distanceInfo.setText(String.format(Locale.CHINA, "总距离：%d米", d));
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setSpotInfo(List<Position> dests, List<Double> times, List<Double> dists) {
        List<MultiSpotAdapter.Item> data = new List<>();
        for (int i = 0; i < dests.length(); i++) {
            MultiSpotAdapter.Item item = new MultiSpotAdapter.Item(
                    dests.get(i).getName(),
                    (i == 0) ? 0.0 : times.get(i - 1),
                    (i == 0) ? 0.0 : dists.get(i - 1)
            );
            data.push(item);
        }
        adapter.setData(data);
        adapter.notifyDataSetChanged();
    }

    private void displayRoute(@NonNull List<Position> route) {
        OverlayHelper.removeAllLines();
        for (Position p : route) {
            OverlayHelper.drawLine(p);
        }
    }

    @Override
    public void openBox() {
        if (multiSpotBox != null && multiRouteContainer.findViewById(R.id.multi_route_spot_box) == null) {
            multiRouteContainer.addView(multiSpotBox);
        }
        expendButton.setImageResource(R.drawable.expend_arrow_down);
    }

    @Override
    public void closeBox() {
        if (multiSpotBox != null && multiRouteContainer.findViewById(R.id.multi_route_spot_box) != null) {
            multiRouteContainer.removeView(multiSpotBox);
        }
        expendButton.setImageResource(R.drawable.expend_arrow_up);
    }

}
