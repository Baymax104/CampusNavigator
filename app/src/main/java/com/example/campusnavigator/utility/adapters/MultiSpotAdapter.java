package com.example.campusnavigator.utility.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusnavigator.R;
import com.example.campusnavigator.model.Position;
import com.example.campusnavigator.utility.structures.List;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/10/2 14:42
 * @Version 1
 */
public class MultiSpotAdapter extends RecyclerView.Adapter<MultiSpotAdapter.ViewHolder> {
    private List<Position> data;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView spotName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            spotName = itemView.findViewById(R.id.multi_spot_name);
        }
    }

    public MultiSpotAdapter(List<Position> data) {
        this.data = data;
    }

    public void setData(List<Position> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_multi_spot_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Position pos = data.get(position);
        String name = pos.getName();
        holder.spotName.setText(name);
    }

    @Override
    public int getItemCount() {
        return data.length();
    }
}
