package com.example.campusnavigator.window;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusnavigator.R;
import com.example.campusnavigator.model.Position;
import com.example.campusnavigator.utility.adapters.MultiSpotAdapter;
import com.example.campusnavigator.utility.helpers.OverlayHelper;
import com.example.campusnavigator.utility.structures.List;
import com.example.campusnavigator.utility.structures.Tuple;

import java.util.Locale;

/**
 * @Description 多点路径结果弹窗对象
 * @Author John
 * @email
 * @Date 2022/9/26 10:57
 * @Version 1
 */
public class MultiRouteWindow extends Window {
    private LinearLayout multiRouteContainer;

    // 上方信息窗口
    private TextView timeInfo;
    private TextView distanceInfo;
    private ImageView expendButton;

    // 下方地点列表
    private View multiSpotBox;
    private RecyclerView multiSpotList;
    private MultiSpotAdapter adapter;

    public MultiRouteWindow(Context context, ViewGroup parent) {
        super(R.layout.layout_multi_route_window, context, parent);
        multiRouteContainer = rootView.findViewById(R.id.multi_route_container);

        timeInfo = multiRouteContainer.findViewById(R.id.multi_route_time_info);
        distanceInfo = multiRouteContainer.findViewById(R.id.multi_route_distance_info);
        expendButton = multiRouteContainer.findViewById(R.id.expend_button);

        multiSpotBox = LayoutInflater.from(context).inflate(R.layout.layout_multi_spot_box, parent, false);
        multiSpotList = multiSpotBox.findViewById(R.id.multi_route_spot_list);
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

    public void setRouteInfo(Double time, Double dist) {
        int t = time.intValue();
        int d = dist.intValue();
        if (timeInfo != null && distanceInfo != null) {
            timeInfo.setText(String.format(Locale.CHINA, "%d分钟", t));
            distanceInfo.setText(String.format(Locale.CHINA, "%d米", d));
        }
    }

    public void openSpotBox() {
        if (multiSpotBox != null && multiRouteContainer.findViewById(R.id.multi_route_spot_box) == null) {
            multiRouteContainer.addView(multiSpotBox);
        }
    }

    public void closeSpotBox() {
        if (multiSpotBox != null && multiRouteContainer.findViewById(R.id.multi_route_spot_box) != null) {
            multiRouteContainer.removeView(multiSpotBox);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSpotData(List<Position> data) {
        if (adapter == null) {
            adapter = new MultiSpotAdapter(data);
            multiSpotList.setAdapter(adapter);
            LinearLayoutManager manager = new LinearLayoutManager(context);
            multiSpotList.setLayoutManager(manager);
        } else {
            adapter.setData(data);
            adapter.notifyDataSetChanged();
        }
    }


    public void displayRoute(@NonNull List<Tuple<Position, Position>> route, OverlayHelper operator) {
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
