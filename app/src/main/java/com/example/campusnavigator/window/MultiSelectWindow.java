package com.example.campusnavigator.window;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.campusnavigator.R;

/**
 * @Description 多点选择弹窗对象
 * @Author John
 * @email
 * @Date 2022/9/26 10:35
 * @Version 1
 */
public class MultiSelectWindow extends Window {
    private Button routeButton;
    private TextView selectNumber;
    private RecyclerView spotListView;

    public MultiSelectWindow(Context context, ViewGroup parent) {
        super(R.layout.layout_multi_select_window, context, parent);
        routeButton = rootView.findViewById(R.id.multi_route_button);
        selectNumber = rootView.findViewById(R.id.select_number);
        spotListView = rootView.findViewById(R.id.spot_list);

        selectNumber.setText("0");
    }

    public int getWindowY() {
        int[] locationOnScreen = new int[2];
        rootView.getLocationOnScreen(locationOnScreen);
        return locationOnScreen[1];
    }

    public void setTestListener(View.OnClickListener listener) {
        routeButton.setOnClickListener(listener);
    }
}
