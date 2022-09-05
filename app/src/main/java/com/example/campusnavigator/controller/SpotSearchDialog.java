package com.example.campusnavigator.controller;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.campusnavigator.R;
import com.example.campusnavigator.model.Position;
import com.example.campusnavigator.model.PositionProvider;
import com.example.campusnavigator.utility.List;
import com.lxj.xpopup.core.BottomPopupView;
import com.lxj.xpopup.util.XPopupUtils;

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

    public SpotSearchDialog(@NonNull Context context) {
        super(context);
        this.context = context;
        provider = PositionProvider.getInstance(context);
        spotNames = provider.getAllName();
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

        adapter = new SpotsAdapter(spotNames);
        spotsRecyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        spotsRecyclerView.setLayoutManager(layoutManager);

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
                    spotNames = provider.getAllName();
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
