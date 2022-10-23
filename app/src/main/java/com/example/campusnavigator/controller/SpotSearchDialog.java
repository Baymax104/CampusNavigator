package com.example.campusnavigator.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusnavigator.R;
import com.example.campusnavigator.model.Position;
import com.example.campusnavigator.model.SpotProvider;
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
    private SpotProvider provider;
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
            SpotProvider provider,
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
        EditText editText = findViewById(R.id.dialog_search_spot_edit);
        RecyclerView spotsRecyclerView = findViewById(R.id.dialog_search_spot_list);
        Button routeButton = findViewById(R.id.dialog_search_route_button);
        MaterialCardView mapSelectButton = findViewById(R.id.dialog_search_select_spot);
        ImageView cleanButton = findViewById(R.id.dialog_search_clean_button);

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

                if (content.equals("")) { // 输入为空，重新展示所有地名
                    adapter.setData(spotNames);
                    adapter.notifyDataSetChanged();
                    cleanButton.setVisibility(INVISIBLE);
                } else {
                    List<String> results = fuzzySearch(content, spotNames);
                    adapter.setData(results);
                    adapter.notifyDataSetChanged();
                    cleanButton.setVisibility(VISIBLE);
                }
            }
        });

        // 列表item点击监听
        adapter.setItemClickListener(name -> {
            editText.setText(name);
            editText.setSelection(editText.getText().length());
        });

        // 地图选点监听
        mapSelectButton.setOnClickListener(view -> {
            if (modeContext == Mode.DEFAULT) {
                dismissWith(() -> listener.onSingleSelect());
            }
        });

        cleanButton.setOnClickListener(view -> editText.setText(""));

        // 路线点击监听
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

    // TODO 模糊搜索
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
