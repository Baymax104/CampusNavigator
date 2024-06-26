package com.example.campusnavigator.window;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.example.campusnavigator.R;
import com.example.campusnavigator.model.M;
import com.example.campusnavigator.model.Map;
import com.example.campusnavigator.model.Mode;
import com.example.campusnavigator.model.Position;

import java.util.Locale;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/10/31 10:59
 * @Version 1
 */
public class SelectClickWindow extends Window {

    private final TextView nameTxt;
    private final TextView infoTxt;
    private final Button button;
    private Position selected;

    public interface ButtonClickListener {
        void onClick(Position selected);
    }

    private SelectClickWindow(Context context, ViewGroup parent) {
        super(R.layout.window_select_click, M.S_SELECT_CLICK, context, parent);
        nameTxt = rootView.findViewById(R.id.select_click_name);
        infoTxt = rootView.findViewById(R.id.select_click_info);
        button = rootView.findViewById(R.id.select_click_button);
    }

    public static SelectClickWindow newInstance(Context context, ViewGroup parent) {
        SelectClickWindow window = new SelectClickWindow(context, parent);
        M.S_SELECT_CLICK.setWindow(window);
        return window;
    }

    public void setButtonListener(Mode mode, ButtonClickListener listener) {
        button.setOnClickListener(v -> {
            if (mode.is(this.mode)) {
                listener.onClick(selected);
            }
        });
    }

    public void setMarkerInfo(@NonNull Position position, @NonNull Position myPosition) {
        this.selected = position;

        nameTxt.setText(position.getName());

        LatLng p1 = position.getLatLng();
        LatLng p2 = myPosition.getLatLng();
        double d = AMapUtils.calculateLineDistance(p1, p2);
        double t = d / Map.SPEED_WALK;

        int dist = (int) d;
        int time = (int) t;
        String info = String.format(Locale.CHINA, "距离当前位置%d米，预计步行%d分钟", dist, time);

        infoTxt.setText(info);
    }
}
