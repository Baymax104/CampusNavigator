package com.example.campusnavigator.window;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusnavigator.R;
import com.example.campusnavigator.model.Position;
import com.example.campusnavigator.utility.adapters.SpotSelectAdapter;

import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;

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
    private ImageView selectRemoveButton;
    private RecyclerView spotRecyclerView;
    private SpotSelectAdapter adapter;

    public MultiSelectWindow(Context context, ViewGroup parent) {
        super(R.layout.layout_multi_select_window, context, parent);
        routeButton = rootView.findViewById(R.id.multi_select_button);
        selectNumber = rootView.findViewById(R.id.multi_select_number);
        spotRecyclerView = rootView.findViewById(R.id.multi_select_list);
        selectRemoveButton = rootView.findViewById(R.id.multi_select_remove);

        selectNumber.setText("0");

        adapter = new SpotSelectAdapter();
        spotRecyclerView.setAdapter(adapter);
        LinearLayoutManager manager = new LinearLayoutManager(context);
        manager.setOrientation(RecyclerView.HORIZONTAL);
        spotRecyclerView.setLayoutManager(manager);
        spotRecyclerView.setItemAnimator(new SlideInRightAnimator());
    }

    public int getWindowY() {
        int[] locationOnScreen = new int[2];
        rootView.getLocationOnScreen(locationOnScreen);
        return locationOnScreen[1];
    }

    public void setRouteButtonListener(View.OnClickListener listener) {
        routeButton.setOnClickListener(listener);
    }

    public void setSelectRemoveListener(View.OnClickListener listener) {
        selectRemoveButton.setOnClickListener(listener);
    }

    public void addPosition(Position position) {
        adapter.addItem(position);
        selectNumber.setText(String.valueOf(adapter.getItemCount()));
        spotRecyclerView.scrollToPosition(adapter.getItemCount() - 1);
    }

    public boolean removePosition() {
        if (adapter.getItemCount() == 0) {
            return false;
        }
        adapter.removeItem();
        selectNumber.setText(String.valueOf(adapter.getItemCount()));
        spotRecyclerView.scrollToPosition(adapter.getItemCount() - 1);
        return true;
    }

    public void removeAllPosition() {
        while (adapter.getItemCount() != 0) {
            adapter.removeItem();
        }
        selectNumber.setText("0");
    }
}
