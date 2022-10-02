package com.example.campusnavigator.window;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.campusnavigator.R;
import com.google.android.material.card.MaterialCardView;

/**
 * @Description 搜索框弹窗对象
 * @Author John
 * @email
 * @Date 2022/9/26 10:09
 * @Version 1
 */
public class SearchWindow extends Window {
    private TextView searchField;
    private MaterialCardView multiSelectEntry;

    public SearchWindow(@NonNull Context context, @NonNull ViewGroup parent) {
        super(R.layout.layout_search_window, context, parent);
        searchField = rootView.findViewById(R.id.search_field);
        multiSelectEntry = rootView.findViewById(R.id.multi_select_entry);
    }

    public void setSearchFieldListener(View.OnClickListener listener) {
        searchField.setOnClickListener(listener);
    }

    public void setMultiSelectEntryListener(View.OnClickListener listener) {
        multiSelectEntry.setOnClickListener(listener);
    }
}
