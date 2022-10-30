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
 * @Date 2022/9/26 18:56
 * @Version 1
 */
public class MultiSelectAdapter extends RecyclerView.Adapter<MultiSelectAdapter.ViewHolder> {
    private final List<Position> data = new List<>(); // 初始总为空

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView spotName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            spotName = itemView.findViewById(R.id.multi_select_spot_name);
        }
    }

    public MultiSelectAdapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_multi_select_spot_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Position position1 = data.get(position);
        holder.spotName.setText(position1.getName());
    }

    @Override
    public int getItemCount() {
        return data.length();
    }

    public void removeItem() {
        int position = data.length() - 1;
        if (position >= 0) {
            data.pop();
            notifyItemRemoved(position);
        }
    }

    public void addItem(Position position) {
        data.push(position);
        notifyItemInserted(data.length() - 1);
    }
}
