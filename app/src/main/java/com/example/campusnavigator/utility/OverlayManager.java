package com.example.campusnavigator.utility;

import android.graphics.Color;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.example.campusnavigator.R;
import com.example.campusnavigator.model.Position;

/**
 * @Description 设置覆盖物样式及控制动画的管理类
 * @Author John
 * @email
 * @Date 2022/9/6 18:05
 * @Version 1
 */
public class OverlayManager {
    private AMap map;
    private List<Polyline> lineBuffer;
    private PolylineOptions lineStyle;
    private static OverlayManager overlayManager;

    private OverlayManager(AMap map) {
        this.map = map;
        lineBuffer = new List<>();
        lineStyle = new PolylineOptions()
                .width(40)
                .lineJoinType(PolylineOptions.LineJoinType.LineJoinRound)
                .setUseTexture(true)
                .setCustomTexture(BitmapDescriptorFactory.fromResource(R.drawable.line_arrow));
    }

    public static OverlayManager getInstance(AMap map) {
        if (overlayManager == null) {
            overlayManager = new OverlayManager(map);
        }
        return overlayManager;
    }

    public void drawLine(Position from, Position to) {
        Polyline polyline = map.addPolyline(lineStyle.add(from.getLatLng(), to.getLatLng()));
        lineBuffer.add(polyline);
    }

    public void removeLines() {

    }

}
