package com.example.campusnavigator.window;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.campusnavigator.R;
import com.example.campusnavigator.controller.M;
import com.google.android.material.card.MaterialCardView;

/**
 * @Description 搜索框弹窗对象
 * @Author John
 * @email
 * @Date 2022/9/26 10:09
 * @Version 1
 */
public class SearchWindow extends Window {
    private final TextView searchField;
    private final MaterialCardView multiSelectEntry;
    private final MaterialCardView buildingEntry;


    private SearchWindow(@NonNull Context context, @NonNull ViewGroup parent) {
        super(R.layout.layout_search_window, context, parent);
        searchField = rootView.findViewById(R.id.search_field);
        multiSelectEntry = rootView.findViewById(R.id.search_multi_select_entry);
        buildingEntry = rootView.findViewById(R.id.search_building_entry);
    }

    @NonNull
    public static SearchWindow newInstance(Context context, ViewGroup parent) {
        SearchWindow window = new SearchWindow(context, parent);
        M.DEFAULT.setWindow(window);
        return window;
    }

    public void setSearchListener(View.OnClickListener listener) {
        searchField.setOnClickListener(listener);
    }

    public void setEntryListener(View.OnClickListener listener) {
        multiSelectEntry.setOnClickListener(listener);
    }

    public void setSpotTypeListener(View.OnClickListener listener) {
        buildingEntry.setOnClickListener(listener);
    }
}
