package com.example.campusnavigator.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusnavigator.R;
import com.example.campusnavigator.model.Position;
import com.example.campusnavigator.model.PositionProvider;
import com.example.campusnavigator.utility.adapters.SpotSearchAdapter;
import com.example.campusnavigator.utility.interfaces.SingleSelectListener;
import com.example.campusnavigator.utility.structures.List;
import com.google.android.material.card.MaterialCardView;
import com.lxj.xpopup.core.BottomPopupView;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/5 14:38
 * @Version 1
 */
public class SpotSearchDialog extends BottomPopupView {
    private Context context;
    private PositionProvider provider;
    private SpotSearchAdapter adapter;
    private SingleSelectListener listener;
    private String selectResult;
    private Mode modeContext;

    public SpotSearchDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public SpotSearchDialog(
            @NonNull Context context,
            Mode mode,
            PositionProvider provider,
            SingleSelectListener listener,
            Position... selectedSpot) {
        super(context);
        this.context = context;
        this.listener = listener;
        this.provider = provider;
        this.modeContext = mode;
        if (selectedSpot != null && selectedSpot.length > 0) {
            this.selectResult = selectedSpot[0].getName();
        }
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.dialog_spot_search;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        EditText editText = findViewById(R.id.spot_edit);
        RecyclerView spotsRecyclerView = findViewById(R.id.list_spot);

        Button routeButton = findViewById(R.id.single_route_button);
        MaterialCardView mapSelectCard = findViewById(R.id.select_spot_card);

        List<String> spotNames = provider.getAllNames();
        adapter = new SpotSearchAdapter(spotNames);
        spotsRecyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        spotsRecyclerView.setLayoutManager(layoutManager);

        if (selectResult != null) {
            editText.setText(selectResult);
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void afterTextChanged(Editable editable) {
                String content = editable.toString();
                // TODO 添加清除按钮，指针自动调整到最后
                if (content.equals("")) { // 输入为空，重新展示所有地名
                    adapter.setData(spotNames);
                    adapter.notifyDataSetChanged();
                } else {
                    List<String> results = fuzzySearch(content, spotNames);
                    adapter.setData(results);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        adapter.setItemClickListener(position -> {
            String name = spotNames.get(position);
            editText.setText(name);
        });

        mapSelectCard.setOnClickListener(view -> {
            if (modeContext == Mode.DEFAULT) {
                dismissWith(() -> listener.onSingleSelect());
            }
        });

        routeButton.setOnClickListener(view -> dismissWith(() -> {
            String name = editText.getText().toString();
            Position spot = provider.getPosByName(name);
            if (spot != null) {
                listener.onDestReceiveSuccess(spot);
            } else {
                listener.onDestReceiveError(new Exception("找不到该地点"));
            }
        }));
    }

    private List<String> fuzzySearch(String content, List<String> spotNames) {
        List<String> results = new List<>();
        for (String name : spotNames) {
            if (name.equals(content)) {
                results.push(name);
            }
        }
        return results;
    }

}
