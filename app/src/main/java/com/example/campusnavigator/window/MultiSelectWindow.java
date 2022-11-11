package com.example.campusnavigator.window;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusnavigator.R;
import com.example.campusnavigator.model.M;
import com.example.campusnavigator.model.Mode;
import com.example.campusnavigator.model.Position;
import com.example.campusnavigator.utility.adapters.MultiSelectAdapter;

import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;

/**
 * @Description 多点选择弹窗对象
 * @Author John
 * @email
 * @Date 2022/9/26 10:35
 * @Version 1
 */
public class MultiSelectWindow extends Window {
    private final Button routeButton;
    private final TextView selectNumber;
    private final ImageView removeButton;
    private final RecyclerView spotRecyclerView;
    private final MultiSelectAdapter adapter;

    private MultiSelectWindow(Context context, ViewGroup parent) {
        super(R.layout.window_multi_select, M.M_SELECT, context, parent);
        routeButton = rootView.findViewById(R.id.multi_select_button);
        selectNumber = rootView.findViewById(R.id.multi_select_number);
        spotRecyclerView = rootView.findViewById(R.id.multi_select_list);
        removeButton = rootView.findViewById(R.id.multi_select_remove);

        selectNumber.setText("0");

        adapter = new MultiSelectAdapter();
        spotRecyclerView.setAdapter(adapter);
        LinearLayoutManager manager = new LinearLayoutManager(context);
        manager.setOrientation(RecyclerView.HORIZONTAL);
        spotRecyclerView.setLayoutManager(manager);
        spotRecyclerView.setItemAnimator(new SlideInRightAnimator());
    }
    public static MultiSelectWindow newInstance(Context context, ViewGroup parent) {
        MultiSelectWindow window = new MultiSelectWindow(context, parent);
        M.M_SELECT.setWindow(window);
        return window;
    }

    public void setButtonListener(Mode mode, OnClickListener listener) {
        registerListener(routeButton, mode, listener);
    }

    public void setRemoveListener(Mode mode, OnClickListener listener) {
        registerListener(removeButton, mode, listener);
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
