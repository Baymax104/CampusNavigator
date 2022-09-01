package com.example.campusnavigator.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.LinearLayout;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.TextOptions;
import com.example.campusnavigator.R;
import com.example.campusnavigator.model.Locations;
import com.example.campusnavigator.model.Position;
import com.example.campusnavigator.model.MapManager;
import com.example.campusnavigator.model.PositionProvider;
import com.example.campusnavigator.utility.List;


public class MainActivity extends AppCompatActivity{
    private MapView mapView;
    private AMap map = null;
    private PositionProvider provider;
    private MapManager manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // provider需要context，必须延迟初始化
        provider = PositionProvider.getInstance(this, "map_data.json");
        manager = MapManager.getInstance(this, "map_data.json");
        privacyCompliance();
        initView();
        mapView.onCreate(savedInstanceState);
        setMap();

        map.setOnMarkerClickListener(marker -> {
            LatLng latLng = marker.getPosition();
            Position position = provider.getPosByLatLng(latLng);
            List<Position[]> results = manager.BFS(position);
            for (int i = 0; i < results.getSize(); i++) {
                Position[] pos = results.get(i);
                map.addPolyline(new PolylineOptions().add(pos[0].getLatLng(), pos[1].getLatLng()));
            }
            return true;
        });
    }

    private void privacyCompliance() {
        MapsInitializer.updatePrivacyShow(MainActivity.this,true,true);
        SpannableStringBuilder spannable = new SpannableStringBuilder("\"亲，感谢您对XXX一直以来的信任！我们依据最新的监管要求更新了XXX《隐私权政策》，特向您说明如下\n1.为向您提供交易相关基本功能，我们会收集、使用必要的信息；\n2.基于您的明示授权，我们可能会获取您的位置（为您提供附近的商品、店铺及优惠资讯等）等信息，您有权拒绝或取消授权；\n3.我们会采取业界先进的安全措施保护您的信息安全；\n4.未经您同意，我们不会从第三方处获取、共享或向提供您的信息；\n");
        spannable.setSpan(new ForegroundColorSpan(Color.BLUE), 35, 42, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        new AlertDialog.Builder(this)
                .setTitle("温馨提示")
                .setMessage(spannable)
                .setPositiveButton("同意", (dialogInterface, i) -> MapsInitializer.updatePrivacyAgree(MainActivity.this,true))
                .setNegativeButton("不同意", (dialogInterface, i) -> {
                    MapsInitializer.updatePrivacyAgree(MainActivity.this,false);
                    finish();
                })
                .show();
    }

    private void initView() {
        AMapOptions options = new AMapOptions();
        options.tiltGesturesEnabled(false);
        Position defaultPosition = new Position(Locations.DEFAULT_LAT, Locations.DEFAULT_LNG);
        options.camera(new CameraPosition(defaultPosition.getLatLng(), 18, 0, 0));
        mapView = new MapView(this, options);
        LinearLayout layout = findViewById(R.id.activity_main);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        layout.addView(mapView, params);
    }

    void setMap() {
        if (map == null) {
            map = mapView.getMap();
        }
        map.setOnMapLoadedListener(() -> {
            map.showMapText(false);
            LatLng southwest = new LatLng(39.871214,116.47701);
            LatLng northeast = new LatLng(39.879621,116.489407);
            map.setMapStatusLimits(new LatLngBounds(southwest, northeast));
            map.addText(new TextOptions().position(provider.getPosByName("奥运餐厅").getLatLng()).text("奥运餐厅").fontSize(37).backgroundColor(Color.TRANSPARENT));
            map.addText(new TextOptions().position(provider.getPosByName("东门").getLatLng()).text("东门").fontSize(37).backgroundColor(Color.TRANSPARENT));
            map.addText(new TextOptions().position(provider.getPosByName("西门").getLatLng()).text("西门").fontSize(37).backgroundColor(Color.TRANSPARENT));
            map.addText(new TextOptions().position(provider.getPosByName("南门").getLatLng()).text("南门").fontSize(37).backgroundColor(Color.TRANSPARENT));
            map.addText(new TextOptions().position(provider.getPosByName("逸夫图书馆").getLatLng()).text("逸夫图书馆").fontSize(37).backgroundColor(Color.TRANSPARENT));
            map.addText(new TextOptions().position(provider.getPosByName("东南门").getLatLng()).text("东南门").fontSize(37).backgroundColor(Color.TRANSPARENT));
            map.addText(new TextOptions().position(provider.getPosByName("美食园").getLatLng()).text("美食园").fontSize(37).backgroundColor(Color.TRANSPARENT));
            map.addText(new TextOptions().position(provider.getPosByName("北门").getLatLng()).text("北门").fontSize(37).backgroundColor(Color.TRANSPARENT));
            map.addText(new TextOptions().position(provider.getPosByName("天天餐厅").getLatLng()).text("天天餐厅").fontSize(37).backgroundColor(Color.TRANSPARENT));
            map.addText(new TextOptions().position(provider.getPosByName("篮球场").getLatLng()).text("篮球场").fontSize(37).backgroundColor(Color.TRANSPARENT));
            map.addText(new TextOptions().position(provider.getPosByName("信息楼").getLatLng()).text("信息楼").fontSize(37).backgroundColor(Color.TRANSPARENT));

            map.addMarker(new MarkerOptions().position(provider.getPosByName("奥运餐厅").getLatLng()));
            map.addMarker(new MarkerOptions().position(provider.getPosByName("东门").getLatLng()));
            map.addMarker(new MarkerOptions().position(provider.getPosByName("西门").getLatLng()));
            map.addMarker(new MarkerOptions().position(provider.getPosByName("南门").getLatLng()));
            map.addMarker(new MarkerOptions().position(provider.getPosByName("逸夫图书馆").getLatLng()));
            map.addMarker(new MarkerOptions().position(provider.getPosByName("东南门").getLatLng()));
            map.addMarker(new MarkerOptions().position(provider.getPosByName("美食园").getLatLng()));
            map.addMarker(new MarkerOptions().position(provider.getPosByName("北门").getLatLng()));
            map.addMarker(new MarkerOptions().position(provider.getPosByName("信息楼").getLatLng()));
            map.addMarker(new MarkerOptions().position(provider.getPosByName("篮球场").getLatLng()));
            map.addMarker(new MarkerOptions().position(provider.getPosByName("天天餐厅").getLatLng()));

//            for (int i = 0; i < PositionProvider.getSize(); i++) {
//                for (int j = i + 1; j < PositionProvider.getSize(); j++) {
//                    if (manager.getWeight(i, j) == 1) {
//                        map.addPolyline(new PolylineOptions().add(provider.getPosById(i).getLatLng(),provider.getPosById(j).getLatLng()));
//                    }
//                }
//            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }
}