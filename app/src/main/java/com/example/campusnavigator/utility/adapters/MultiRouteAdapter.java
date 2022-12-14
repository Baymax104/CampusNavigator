package com.example.campusnavigator.utility.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusnavigator.R;
import com.example.campusnavigator.utility.structures.List;

import java.util.Locale;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/10/2 14:42
 * @Version 1
 */
public class MultiRouteAdapter extends RecyclerView.Adapter<MultiRouteAdapter.ViewHolder> {
    private List<Item> data;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView spotName;
        final TextView spotInfo;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            spotName = itemView.findViewById(R.id.multi_route_spot_name);
            spotInfo = itemView.findViewById(R.id.multi_route_spot_info);
        }
    }

    public static class Item {
        private final String name;
        private final Double time;
        private final Double dist;

        public Item(String name, Double time, Double dist) {
            this.name = name;
            this.time = time;
            this.dist = dist;
        }
    }

    public MultiRouteAdapter() {
    }

    public void setData(List<Item> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_multi_route_spot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = data.get(position);
        holder.spotName.setText(item.name);
        int dist = item.dist.intValue();
        int time = item.time.intValue();
        if (dist == -1 && time == -1) {
            holder.spotInfo.setText("起点");
        } else{
            String info = String.format(
                    Locale.CHINA,
                    "移动%d米，在%d分钟后到达",
                    dist, time);
            holder.spotInfo.setText(info);
        }
    }

    @Override
    public int getItemCount() {
        return data.length();
    }
}
