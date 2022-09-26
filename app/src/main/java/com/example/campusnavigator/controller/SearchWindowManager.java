package com.example.campusnavigator.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.campusnavigator.R;
import com.google.android.material.card.MaterialCardView;

/**
 * @Description 搜索框布局对象
 * @Author John
 * @email
 * @Date 2022/9/26 10:09
 * @Version 1
 */
public class SearchWindowManager {
    private Context context;
    private ViewGroup parent;
    private View rootView;
    private TextView searchField;
    private MaterialCardView multiSelectCard;

    public SearchWindowManager(@NonNull Context context, @NonNull ViewGroup parent) {
        if (this.parent == null && this.context == null) {
            this.context = context;
            this.parent = parent;
            rootView = LayoutInflater.from(context).inflate(R.layout.layout_search_window, parent, false);
            searchField = rootView.findViewById(R.id.search_field);
            multiSelectCard = rootView.findViewById(R.id.multi_select_card);
        }
    }

    public void addToParent() {
        parent.addView(rootView);
    }

    public void removeFromParent() {
        parent.removeView(rootView);
    }

    public void setSearchFieldListener(View.OnClickListener listener) {
        searchField.setOnClickListener(listener);
    }

    public void setMultiSelectCardListener(View.OnClickListener listener) {
        multiSelectCard.setOnClickListener(listener);
    }
}
