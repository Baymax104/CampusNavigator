package com.example.campusnavigator.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.MyLocationStyle;
import com.example.campusnavigator.R;
import com.example.campusnavigator.model.DialogHelper;
import com.example.campusnavigator.model.Position;
import com.example.campusnavigator.model.MapManager;
import com.example.campusnavigator.model.PositionProvider;
import com.example.campusnavigator.utility.List;
import com.example.campusnavigator.utility.OverlayManager;
import com.example.campusnavigator.utility.Stack;


public class MainActivity extends AppCompatActivity implements LocationSource, AMapLocationListener,RouteResultCallback,OnSpotSelectListener {
    private MapView mapView;
    private AMap map = null;
    private PositionProvider provider;
    private MapManager manager;
    private OverlayManager overlayManager;
    private Stack<Position> spotBuffer = new Stack<>(); // 地点参数栈
    private Position myLocation;
    private int modeCode = 0;

    private Button button;
    private TextView searchView;
    private CoordinatorLayout container;
    private View searchBox;
    private View routeBox;

    private OnLocationChangedListener locationListener; // 定位改变回调接口
    private AMapLocationClient locationClient; // 定位启动和销毁类


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 隐私合规
        privacyCompliance();
        // 初始化
        initView();
        mapView.onCreate(savedInstanceState);
        if (map == null) {
            map = mapView.getMap();
        }
        // provider需要context，必须延迟初始化
        provider = PositionProvider.getInstance(this);
        manager = MapManager.getInstance(this);
        overlayManager = OverlayManager.getInstance(map, mapView, this);
        // 设置地图属性
        setMap();

        map.setOnMarkerClickListener(marker -> {
            Toast.makeText(this, "已选中", Toast.LENGTH_SHORT).show();
            LatLng latLng = marker.getPosition();
            Position position = provider.getPosByLatLng(latLng);
            if (modeCode == 1) {
                DialogHelper.showSpotSearchDialog(this, position, this);
            }
            return true;
        });

        button.setOnClickListener(view -> {
//            if (!spotBuffer.isEmpty()) {
//                 manager.getMultiDestRoute(spotBuffer, this);
//            }
        });

        searchView.setOnClickListener(view -> {
            DialogHelper.showSpotSearchDialog(this, this);
        });
    }

    private void privacyCompliance() {
        MapsInitializer.updatePrivacyShow(MainActivity.this,true,true);
        MapsInitializer.updatePrivacyAgree(this, true);
//        DialogHelper.showPrivacyConfirmDialog(this);
    }

    private void initView() {
        AMapOptions options = new AMapOptions();
        LatLng defaultPosition = new LatLng(39.8751, 116.48134);
        options.camera(CameraPosition.fromLatLngZoom(defaultPosition, 18));
        mapView = new MapView(this, options);
        FrameLayout layout = findViewById(R.id.map_view_container);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(mapView, params);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        container = findViewById(R.id.view_container);
        searchBox = LayoutInflater.from(this).inflate(R.layout.layout_search_box, container, false);
        routeBox = LayoutInflater.from(this).inflate(R.layout.layout_route_box, container, false);
        container.addView(searchBox);

        button = findViewById(R.id.confirm_button);
        searchView = findViewById(R.id.search_position);
    }

    void setMap() {
        LatLng southwest = new LatLng(39.870337,116.477103);
        LatLng northeast = new LatLng(39.880384,116.488162);

        MyLocationStyle locationStyle = new MyLocationStyle()
                .interval(1200)
                .myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
                .strokeColor(Color.TRANSPARENT)
                .radiusFillColor(Color.TRANSPARENT)
                .showMyLocation(true);

        List<String> spotNames = provider.getAllNames();
        map.showMapText(false);
        map.setMapStatusLimits(new LatLngBounds(southwest, northeast));
        map.setLocationSource(this);
        map.setMyLocationStyle(locationStyle);
        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(false);
        for (String s : spotNames) {
            // 设置marker
            overlayManager.drawMarker(provider.getPosByName(s).get(0));
            // 设置文字
            overlayManager.drawText(provider.getPosByName(s).get(0), s);
        }
    }

    @Override
    public void showMultiDestRoute(List<Position[]> results) {
        for (int i = 0; i < results.length(); i++) {
            Position[] p = results.get(i);
            if (i == 0) {
                overlayManager.drawLine(p[0], p[1]);
            } else {
                overlayManager.drawLine(p[1]);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if(null != locationClient){
            locationClient.onDestroy();
        }
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
        // TODO 覆盖物无法恢复的bug
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        locationListener = onLocationChangedListener;
        if (locationClient == null) {
            try {
                locationClient = new AMapLocationClient(this);
                AMapLocationClientOption locationOption = new AMapLocationClientOption();
                //设置定位回调监听
                locationClient.setLocationListener(this);
                //设置为高精度定位模式
                locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                //设置定位参数
                locationClient.setLocationOption(locationOption);
                locationClient.startLocation();//启动定位
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deactivate() {
        locationListener = null;
        if (locationClient != null) {
            locationClient.stopLocation();
            locationClient.onDestroy();
        }
        locationClient = null;
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (locationListener != null && aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                locationListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
                double lat = aMapLocation.getLatitude();
                double lng = aMapLocation.getLongitude();
                if (myLocation == null) {
                    myLocation = new Position();
                }
                myLocation.setLat(lat);
                myLocation.setLng(lng);
            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode()+ ": " + aMapLocation.getErrorInfo();
                Log.e("MapLocation",errText);
            }
        }
    }

    @Override
    public void onSpotSelect(int modeCode) {
        this.modeCode = modeCode;
        Toast.makeText(this, "请选择你想去的地点", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showRoute(int modeCode, String name) {
        this.modeCode = modeCode;
        overlayManager.removeLines(); // 清除线段
        Position destPosition = provider.getPosByName(name).get(0);
        if (myLocation != null) {
            Position attachPosition = manager.attachToMap(myLocation);
            if (attachPosition != null) {
                spotBuffer.push(attachPosition);
                spotBuffer.push(destPosition);
                manager.getMultiDestRoute(spotBuffer, this);
                overlayManager.drawLine(attachPosition, myLocation);
            }
        }
        // 更换布局
        if (container != null && searchBox != null) {
            container.removeView(searchBox);
            container.addView(routeBox);
        }
    }

    @Override
    public void onBackPressed() {
        if (modeCode == 2) {
            container.removeView(routeBox);
            container.addView(searchBox);
            overlayManager.removeLines();
            modeCode = 0;
        } else {
            super.onBackPressed();
        }
    }
}