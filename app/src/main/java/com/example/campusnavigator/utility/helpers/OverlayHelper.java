package com.example.campusnavigator.utility.helpers;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.TextOptions;
import com.amap.api.maps.model.animation.ScaleAnimation;
import com.example.campusnavigator.R;
import com.example.campusnavigator.model.Position;
import com.example.campusnavigator.utility.structures.HashMap;
import com.example.campusnavigator.utility.structures.List;

/**
 * @Description 设置覆盖物样式及控制动画的帮助类
 * @Author John
 * @email
 * @Date 2022/9/6 18:05
 * @Version 1
 */
public class OverlayHelper {
    private static AMap map;

    private static PolylineOptions lineStyle;
    private static MarkerOptions markerOptions;
    private static TextOptions textOptions;
    private static ScaleAnimation openAnimation;
    private static ScaleAnimation closeAnimation;

    // Buffer存储当前活动的Overlay
    private static List<Polyline> lineBuffer;
    private static List<Marker> markerBuffer;

    // map记录每个marker在buffer的个数，通过markerId绑定
    private static HashMap<String, Integer> markerMap;

    private OverlayHelper() {
    }

    public static void bind(@NonNull AMap amap, @NonNull MapView mapView, @NonNull Context context) {
        map = amap;
        lineBuffer = new List<>();
        markerBuffer = new List<>();
        markerMap = new HashMap<>();

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

        openAnimation = new ScaleAnimation(0.5f, 1.4f, 0.5f, 1.4f);

        closeAnimation = new ScaleAnimation(1.4f, 1f, 1.4f, 1f);
        closeAnimation.setDuration(0);
    }

    public static void drawLine(@NonNull Position ...destination) {
        for (Position p : destination) {
            Polyline polyline = map.addPolyline(lineStyle.add(p.getLatLng()));
            lineBuffer.push(polyline);
        }
    }
    public static void drawMarker(@NonNull Position position) {
        Marker marker = map.addMarker(markerOptions.position(position.getLatLng()));

        markerMap.put(marker.getId(), 0);
        // position与marker绑定
        position.setMarkerId(marker.getId());
    }

    public static void drawText(@NonNull Position position, @NonNull String text) {
        map.addText(textOptions.position(position.getLatLng()).text(text));
    }

    public static void onMarkerClicked(@NonNull Marker marker) {
        marker.setAnimation(openAnimation);
        marker.startAnimation();
        markerBuffer.push(marker);
        int count = markerMap.get(marker.getId());
        markerMap.put(marker.getId(), count + 1);
    }

    public static void onSpotRemoved(@NonNull Position spot) {
        String markerId = spot.getMarkerId();
        Marker marker = markerBuffer.top();

        // 根据buffer内当前marker的个数判断状态
        int count = markerMap.get(markerId);
        if (count == 1) { // 若当前marker为buffer内最后一个
            marker.setAnimation(closeAnimation);
            marker.startAnimation();
        }
        markerBuffer.pop();
        markerMap.put(markerId, count - 1);
    }

    public static void initAllMarkers() {
        for (Marker marker : markerBuffer) {
            marker.setAnimation(closeAnimation);
            marker.startAnimation();
        }
        markerBuffer.clear();
    }

    public static void removeAllLines() {
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