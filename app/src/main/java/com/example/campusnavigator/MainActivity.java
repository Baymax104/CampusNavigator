package com.example.campusnavigator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import com.example.campusnavigator.domain.Locations;


public class MainActivity extends AppCompatActivity{
    private MapView mapView;
    private AMap map = null;
    private LatLng defaultPosition = Locations.getLatLng("DEFAULT_LOC");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        privacyCompliance();
        initView();

        mapView.onCreate(savedInstanceState);
        setMap();

    }

    private void privacyCompliance() {
        MapsInitializer.updatePrivacyShow(MainActivity.this,true,true);
        SpannableStringBuilder spannable = new SpannableStringBuilder("\"亲，感谢您对XXX一直以来的信任！我们依据最新的监管要求更新了XXX《隐私权政策》，特向您说明如下\n1.为向您提供交易相关基本功能，我们会收集、使用必要的信息；\n2.基于您的明示授权，我们可能会获取您的位置（为您提供附近的商品、店铺及优惠资讯等）等信息，您有权拒绝或取消授权；\n3.我们会采取业界先进的安全措施保护您的信息安全；\n4.未经您同意，我们不会从第三方处获取、共享或向提供您的信息；\n");
        spannable.setSpan(new ForegroundColorSpan(Color.BLUE), 35, 42, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        new AlertDialog.Builder(this)
                .setTitle("温馨提示")
                .setMessage(spannable)
                .setPositiveButton("同意", (dialogInterface, i) -> MapsInitializer.updatePrivacyAgree(MainActivity.this,true))
                .setNegativeButton("不同意", (dialogInterface, i) -> MapsInitializer.updatePrivacyAgree(MainActivity.this,false))
                .show();
    }

    private void initView() {
        AMapOptions options = new AMapOptions();
        options.tiltGesturesEnabled(false);
        options.camera(new CameraPosition(defaultPosition, 18, 0, 0));
        mapView = new MapView(this, options);
        LinearLayout layout = findViewById(R.id.activity_main);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        layout.addView(mapView, params);
    }

    void setMap() {
        if (map == null) {
            map = mapView.getMap();
        }
        map.setOnMapLoadedListener(() -> map.showMapText(false));
        LatLng southwest = new LatLng(39.871214,116.47701);
        LatLng northeast = new LatLng(39.879621,116.489407);
        map.setMapStatusLimits(new LatLngBounds(southwest, northeast));
        map.addMarker(new MarkerOptions().position(Locations.getLatLng("AO_YUN_CAN_TING")).title("奥运餐厅"));
        map.addMarker(new MarkerOptions().position(Locations.getLatLng("DONG_MEN")).title("东门"));
        map.addMarker(new MarkerOptions().position(Locations.getLatLng("XI_MEN")).title("西门"));
        map.addMarker(new MarkerOptions().position(Locations.getLatLng("NAN_MEN")).title("南门"));
        map.addMarker(new MarkerOptions().position(Locations.getLatLng("LIBRARY")).title("逸夫图书馆"));
        map.addMarker(new MarkerOptions().position(Locations.getLatLng("DONG_NAN_MEN")).title("东南门"));
        map.addMarker(new MarkerOptions().position(Locations.getLatLng("MEI_SHI_YUAN")).title("美食园"));
        map.addMarker(new MarkerOptions().position(Locations.getLatLng("BEI_MEN")).title("北门"));
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