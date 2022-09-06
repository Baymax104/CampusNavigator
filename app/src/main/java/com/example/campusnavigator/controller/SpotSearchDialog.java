package com.example.campusnavigator.controller;

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
import com.example.campusnavigator.utility.List;
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
    private List<String> spotNames;
    private SpotsAdapter adapter;
    private Button routeButton;
    private MaterialCardView selectSpotView;
    private OnSpotSelectListener listener;
    private String selectResult;

    public SpotSearchDialog(@NonNull Context context, OnSpotSelectListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        provider = PositionProvider.getInstance(context);
        spotNames = provider.getAllNames();
    }

    public SpotSearchDialog(@NonNull Context context, Position position, OnSpotSelectListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        this.selectResult = position.getName();
        provider = PositionProvider.getInstance(context);
        spotNames = provider.getAllNames();
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

        routeButton = findViewById(R.id.route_button);
        selectSpotView = findViewById(R.id.select_spot_view);

        adapter = new SpotsAdapter(spotNames);
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
            @Override
            public void afterTextChanged(Editable editable) {
                String content = editable.toString();
                if (content.equals("")) { // 输入为空，重新展示所有地名
                    spotNames = provider.getAllNames();
                    adapter.setData(spotNames);
                    adapter.notifyDataSetChanged();
                } else {
                    List<Position> results = provider.getPosByName(content);
                    refreshDataSource(results);
                }
            }
        });

        adapter.setItemClickListener(position -> {
            String name = spotNames.get(position);
            editText.setText(name);
        });

        selectSpotView.setOnClickListener(view -> {
            listener.onSpotSelect(1);
            dismiss();
        });

        routeButton.setOnClickListener(view -> {
            String name = editText.getText().toString();
            listener.showRoute(2, name);
            dismiss();
        });
    }

    private void refreshDataSource(List<Position> positionList) {
        spotNames.clear();
        for (int i = 0; i < positionList.getSize(); i++) {
            spotNames.add(positionList.get(i).getName());
        }
        adapter.setData(spotNames);
        adapter.notifyDataSetChanged();
    }

}
