package com.example.campusnavigator.utility.helpers;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.TextOptions;
import com.example.campusnavigator.R;
import com.example.campusnavigator.model.Position;
import com.example.campusnavigator.utility.structures.List;

/**
 * @Description 设置覆盖物样式及控制动画的帮助类
 * @Author John
 * @email
 * @Date 2022/9/6 18:05
 * @Version 1
 */
public class OverlayHelper {
    private AMap map;
    private List<Polyline> lineBuffer;
    private PolylineOptions lineStyle;
    private MarkerOptions markerOptions;
    private TextOptions textOptions;
    private static OverlayHelper overlayHelper;

    private OverlayHelper(AMap map, MapView mapView, Context context) {
        this.map = map;
        lineBuffer = new List<>();
        lineStyle = new PolylineOptions()
                .width(40)
                .lineJoinType(PolylineOptions.LineJoinType.LineJoinRound)
                .setUseTexture(true)
                .setCustomTexture(BitmapDescriptorFactory.fromResource(R.drawable.line));
        View markerView = LayoutInflater.from(context).inflate(R.layout.icon_marker, mapView, false);
        markerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromView(markerView));
        textOptions = new TextOptions()
                .fontSize(37)
                .backgroundColor(Color.TRANSPARENT);
    }

    public static OverlayHelper getInstance(AMap map, MapView mapView, Context context) {
        if (overlayHelper == null) {
            overlayHelper = new OverlayHelper(map, mapView, context);
        }
        return overlayHelper;
    }

    public void drawLine(Position ...destination) {
        for (Position p : destination) {
            Polyline polyline = map.addPolyline(lineStyle.add(p.getLatLng()));
            lineBuffer.add(polyline);
        }
    }
    public void drawMarker(Position position) {
        map.addMarker(markerOptions.position(position.getLatLng()));
    }

    public void drawText(Position position, String text) {
        map.addText(textOptions.position(position.getLatLng()).text(text));
    }

    public void removeLines() {
        for (Polyline line : lineBuffer) {
            line.remove();
        }
        lineBuffer.clear();
        lineStyle = new PolylineOptions()
                .width(40)
                .lineJoinType(PolylineOptions.LineJoinType.LineJoinRound)
                .setUseTexture(true)
                .setCustomTexture(BitmapDescriptorFactory.fromResource(R.drawable.line));
    }
}