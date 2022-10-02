package com.example.campusnavigator.utility.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusnavigator.R;
import com.example.campusnavigator.utility.structures.List;
import com.example.campusnavigator.utility.callbacks.OnItemClickedListener;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/5 15:25
 * @Version 1
 */
public class SpotSearchAdapter extends RecyclerView.Adapter<SpotSearchAdapter.ViewHolder> {
    private List<String> data;
    private OnItemClickedListener itemClickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView spotName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            spotName = itemView.findViewById(R.id.search_spot_name);
        }
    }

    public SpotSearchAdapter(List<String> data) {
        this.data = data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    public void setItemClickListener(OnItemClickedListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_search_spot_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = data.get(position);
        holder.spotName.setText(name);
        holder.itemView.setOnClickListener(view -> itemClickListener.onItemClicked(position));
    }

    @Override
    public int getItemCount() {
        return data.length();
    }
}
