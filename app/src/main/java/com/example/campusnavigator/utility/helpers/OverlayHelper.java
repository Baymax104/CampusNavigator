package com.example.campusnavigator.utility.helpers;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;

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
    private static ScaleAnimation startAnimation;
    private static ScaleAnimation endAnimation;

    private static List<Polyline> lineBuffer;
    private static List<Marker> markerBuffer;

    private OverlayHelper() {
    }

    public static void bind(AMap amap, MapView mapView, Context context) {
        map = amap;
        lineBuffer = new List<>();
        markerBuffer = new List<>();

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

        startAnimation = new ScaleAnimation(0.5f, 1.4f, 0.5f, 1.4f);
        startAnimation.setDuration(200);

        endAnimation = new ScaleAnimation(1.4f, 1f, 1.4f, 1f);
        endAnimation.setDuration(0);
    }

    public static void drawLine(Position ...destination) {
        for (Position p : destination) {
            Polyline polyline = map.addPolyline(lineStyle.add(p.getLatLng()));
            lineBuffer.push(polyline);
        }
    }
    public static void drawMarker(Position position) {
        Marker marker = map.addMarker(markerOptions.position(position.getLatLng()));
        marker.setAnimation(startAnimation);
        position.setMarkerId(marker.getId());
    }

    public static void drawText(Position position, String text) {
        map.addText(textOptions.position(position.getLatLng()).text(text));
    }

    public static void onMarkerStart(Marker marker) {
        marker.setAnimation(endAnimation);
        markerBuffer.push(marker);
    }

    public static void initAllMarkers() {
        for (Marker marker : markerBuffer) {
            marker.startAnimation();
            marker.setAnimation(startAnimation);
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