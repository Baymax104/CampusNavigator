package com.example.campusnavigator.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusnavigator.R;
import com.example.campusnavigator.model.M;
import com.example.campusnavigator.model.Mode;
import com.example.campusnavigator.model.Position;
import com.example.campusnavigator.model.SpotProvider;
import com.example.campusnavigator.utility.adapters.SearchAdapter;
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
    private final Context context;
    private SpotProvider provider;
    private SearchAdapter adapter;
    private EditText editText;
    private SingleSelectListener listener;
    private Mode modeContext;

    public SpotSearchDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public SpotSearchDialog(@NonNull Context context, @NonNull Mode mode, @NonNull SpotProvider provider, SingleSelectListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        this.provider = provider;
        this.modeContext = mode;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.dialog_spot_search;
    }

    public void setSelected(Position position) {
        editText.setText((position == null) ? "" : position.getName());
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        editText = findViewById(R.id.dialog_search_spot_edit);
        RecyclerView spotsRecyclerView = findViewById(R.id.dialog_search_spot_list);
        Button routeButton = findViewById(R.id.dialog_search_route_button);
        MaterialCardView mapSelectButton = findViewById(R.id.dialog_search_select_spot);
        ImageView cleanButton = findViewById(R.id.dialog_search_clean_button);

        List<String> spotNames = provider.allNames();
        adapter = new SearchAdapter(spotNames);
        spotsRecyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        spotsRecyclerView.setLayoutManager(layoutManager);


        // editText内容变化监听
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
                    List<Position> result = provider.fuzzyQuery(content);
                    List<String> names = SpotProvider.extractName(result);
                    adapter.setData(names);
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
            if (modeContext.is(M.DEFAULT)) {
                dismissWith(() -> listener.onSingleSelect());
            } else if (modeContext.is(M.S_SELECT)) {
                // 若当前处于地图选点状态，则直接关闭，继续选点
                dismiss();
            }
        });

        // 清除按钮监听
        cleanButton.setOnClickListener(view -> editText.setText(""));

        // 路线点击监听
        routeButton.setOnClickListener(view -> {
            String content = editText.getText().toString();

            if ("".equals(content)) {
                Toast.makeText(context, "输入不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            Position spot = provider.getPosition(content);
            if (spot == null) {
                Toast.makeText(context, "找不到该地点", Toast.LENGTH_SHORT).show();
                return;
            }

            dismissWith(() -> listener.onDestReceive(spot));
        });
    }

}
