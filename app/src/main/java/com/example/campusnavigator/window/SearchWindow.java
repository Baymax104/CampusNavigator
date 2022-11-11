package com.example.campusnavigator.window;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.campusnavigator.R;
import com.example.campusnavigator.model.M;
import com.example.campusnavigator.model.Mode;
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
        super(R.layout.window_search, M.DEFAULT, context, parent);
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

    public void setSearchListener(Mode mode, OnClickListener listener) {
        registerListener(searchField, mode, listener);
    }

    public void setEntryListener(Mode mode, OnClickListener listener) {
        registerListener(multiSelectEntry, mode, listener);
    }

    public void setBuildingListener(Mode mode, OnClickListener listener) {
        registerListener(buildingEntry, mode, listener);
    }
}
