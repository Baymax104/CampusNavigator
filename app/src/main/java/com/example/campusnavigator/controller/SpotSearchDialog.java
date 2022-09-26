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
import com.example.campusnavigator.utility.List;
import com.example.campusnavigator.utility.callbacks.OnSpotSelectListener;
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
    private SpotSearchAdapter adapter;
    private OnSpotSelectListener listener;
    private String mapSelectResult;
    private Mode mapMode = Mode.DEFAULT;
    private String selectSpotName;

    public SpotSearchDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

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
        this.mapSelectResult = position.getName();
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

        Button routeButton = findViewById(R.id.single_route_button);
        MaterialCardView mapSelectCard = findViewById(R.id.select_spot_card);

        adapter = new SpotSearchAdapter(spotNames);
        spotsRecyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        spotsRecyclerView.setLayoutManager(layoutManager);

        if (mapSelectResult != null) {
            editText.setText(mapSelectResult);
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

        mapSelectCard.setOnClickListener(view -> {
            mapMode = Mode.SINGLE_SELECT;
            dismiss();
        });

        routeButton.setOnClickListener(view -> {
            String name = editText.getText().toString();
            mapMode = Mode.SINGLE_ROUTE_OPEN;
            selectSpotName = name;
            dismiss();
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshDataSource(List<Position> positionList) {
        spotNames.clear();
        for (Position p : positionList) {
            spotNames.add(p.getName());
        }
        adapter.setData(spotNames);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void doAfterDismiss() {
        listener.onMapStateChange(mapMode);
        if (mapMode == Mode.SINGLE_ROUTE_OPEN && selectSpotName != null) {
            try {
                Position selectSpot = provider.getPosByName(selectSpotName).get(0);
                if (selectSpot == null) {
                    throw new Exception("找不到该地点");
                }
                listener.onDestReceiveSuccess(selectSpotName);
            } catch (Exception e) {
                listener.onDestReceiveError(e);
            } finally {
                super.doAfterDismiss();
            }
        }
        super.doAfterDismiss();
    }
}
