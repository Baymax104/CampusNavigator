package com.example.campusnavigator.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.TextOptions;
import com.example.campusnavigator.R;
import com.example.campusnavigator.model.Position;
import com.example.campusnavigator.model.MapManager;
import com.example.campusnavigator.model.PositionProvider;
import com.example.campusnavigator.utility.List;
import com.example.campusnavigator.utility.Queue;
import com.example.campusnavigator.utility.Stack;
import com.lxj.xpopup.XPopup;


public class MainActivity extends AppCompatActivity{
    private MapView mapView;
    private AMap map = null;
    private Button button;
    private PositionProvider provider;
    private MapManager manager;
    private Stack<Position> spotBuffer = new Stack<>(); // 地点参数栈
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // provider需要context，必须延迟初始化
        provider = PositionProvider.getInstance(this, "map_data.json");
        manager = MapManager.getInstance(this, "map_data.json");
        // 隐私合规
        privacyCompliance();
        // 初始化
        initView();
        mapView.onCreate(savedInstanceState);
        setMap();

        map.setOnMarkerClickListener(marker -> {
            LatLng latLng = marker.getPosition();
            Position position = provider.getPosByLatLng(latLng);
            spotBuffer.push(position);
            Toast.makeText(this, "已选中", Toast.LENGTH_SHORT).show();
            return true;
        });

        button.setOnClickListener(view -> {
            if (!spotBuffer.isEmpty()) {
                 showMultiShortPath();
            }
        });
    }

    private void privacyCompliance() {
        MapsInitializer.updatePrivacyShow(MainActivity.this,true,true);
        new XPopup.Builder(this)
                .isDestroyOnDismiss(true)
                .asCustom(new PrivacyDialog(this))
                .show();
    }

    private void initView() {
        AMapOptions options = new AMapOptions();
        options.tiltGesturesEnabled(false);
        Position defaultPosition = new Position(39.8751, 116.48134);
        options.camera(new CameraPosition(defaultPosition.getLatLng(), 18, 0, 0));
        mapView = new MapView(this, options);
        FrameLayout layout = findViewById(R.id.map_view_container);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(mapView, params);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        button = findViewById(R.id.confirm_button);
    }

    void setMap() {
        if (map == null) {
            map = mapView.getMap();
        }
        map.setOnMapLoadedListener(() -> {
            map.showMapText(false);
            LatLng southwest = new LatLng(39.870737,116.477072);
            LatLng northeast = new LatLng(39.87985,116.489752);
            map.setMapStatusLimits(new LatLngBounds(southwest, northeast));
            TextOptions textOptions = new TextOptions().fontSize(37).backgroundColor(Color.TRANSPARENT);
            String[] spotName = new String[] {"奥运餐厅","东门","西门","南门","逸夫图书馆","东南门","美食园","北门","天天餐厅","篮球场","信息楼"};
            for (String s : spotName) {
                map.addText(textOptions.position(provider.getPosByName(s).getLatLng()).text(s));
                map.addMarker(new MarkerOptions().position(provider.getPosByName(s).getLatLng()));
            }
        });
    }

    private void showSingleShortPath() {
        if (spotBuffer.getSize() == 2) {
            Position to = spotBuffer.top();
            spotBuffer.pop();
            Position from = spotBuffer.top();
            spotBuffer.pop();
            if (from.getId() == to.getId()) {
                Toast.makeText(this, "选择重复地点，请重新选择", Toast.LENGTH_SHORT).show();
            } else {
                List<Position[]> results = manager.getSingleShortPath(from, to);
                PolylineOptions lineOptions = new PolylineOptions().color(Color.parseColor("#1e90ff"));
                for (int i = 0; i < results.getSize(); i++) {
                    Position[] p = results.get(i);
                    map.addPolyline(lineOptions.add(p[0].getLatLng(), p[1].getLatLng()));
                }
            }
        }
    }

    private void showMultiShortPath() {
        List<Position[]> result = manager.getMultiShortPath(spotBuffer);
        PolylineOptions lineOptions = new PolylineOptions()
                .width(40)
                .lineJoinType(PolylineOptions.LineJoinType.LineJoinRound)
                .color(Color.parseColor("#1e90ff"))
                .setUseTexture(true)
                .setCustomTexture(BitmapDescriptorFactory.fromResource(R.drawable.line_arrow));
        for (int i = 0; i < result.getSize(); i++) {
            Position[] p = result.get(i);
            map.addPolyline(lineOptions.add(p[0].getLatLng(), p[1].getLatLng()));
        }
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