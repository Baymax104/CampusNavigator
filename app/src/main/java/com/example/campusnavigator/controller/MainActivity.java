package com.example.campusnavigator.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.TextOptions;
import com.bumptech.glide.Glide;
import com.example.campusnavigator.R;
import com.example.campusnavigator.model.DialogHelper;
import com.example.campusnavigator.model.Position;
import com.example.campusnavigator.model.MapManager;
import com.example.campusnavigator.model.PositionProvider;
import com.example.campusnavigator.utility.List;
import com.example.campusnavigator.utility.Stack;


public class MainActivity extends AppCompatActivity implements LocationSource, AMapLocationListener,RouteResultCallback {
    private MapView mapView;
    private AMap map = null;
    private PositionProvider provider;
    private MapManager manager;
    private Stack<Position> spotBuffer = new Stack<>(); // 地点参数栈
    private Position myLocation;

    private Button button;
    private TextView searchView;

    private OnLocationChangedListener locationListener; // 定位改变回调接口
    private AMapLocationClient locationClient; // 定位启动和销毁类

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // provider需要context，必须延迟初始化
        provider = PositionProvider.getInstance(this);
        manager = MapManager.getInstance(this);
        // 隐私合规
        privacyCompliance();
        // 初始化
        initView();
        mapView.onCreate(savedInstanceState);
        setMap();

        map.setOnMarkerClickListener(marker -> {
            // 压入数据
            LatLng latLng = marker.getPosition();
            Position position = provider.getPosByLatLng(latLng);
            spotBuffer.push(position);
            Toast.makeText(this, "已选中", Toast.LENGTH_SHORT).show();
            return true;
        });

        button.setOnClickListener(view -> {
            if (!spotBuffer.isEmpty()) {
                 manager.getMultiDestRoute(spotBuffer, this);
            }
        });

        searchView.setOnClickListener(view -> {
            DialogHelper.showSpotSearchDialog(this);
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

        button = findViewById(R.id.confirm_button);
        searchView = findViewById(R.id.search_position);
    }

    void setMap() {
        if (map == null) {
            map = mapView.getMap();
        }
        LatLng southwest = new LatLng(39.870337,116.477103);
        LatLng northeast = new LatLng(39.880384,116.488162);

        TextOptions textOptions = new TextOptions().fontSize(37).backgroundColor(Color.TRANSPARENT);
        String[] spotName = new String[] {"奥运餐厅","东门","西门","南门","逸夫图书馆","东南门","美食园","北门","天天餐厅","篮球场","信息楼"};

        View markerView = LayoutInflater.from(this).inflate(R.layout.layout_marker_icon, mapView, false);
        MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromView(markerView));

        MyLocationStyle locationStyle = new MyLocationStyle();
        locationStyle.interval(1200);
        locationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        locationStyle.strokeColor(Color.TRANSPARENT);
        locationStyle.radiusFillColor(Color.TRANSPARENT);
        locationStyle.showMyLocation(true);

        map.setOnMapLoadedListener(() -> {
            map.showMapText(false);
            map.setMapStatusLimits(new LatLngBounds(southwest, northeast));
            map.setLocationSource(this);
            map.setMyLocationStyle(locationStyle);
            map.setMyLocationEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(false);
            for (String s : spotName) {
                // 设置marker
                Marker marker = map.addMarker(markerOptions.position(provider.getPosByName(s).get(0).getLatLng()));
                marker.setClickable(true);
                // 设置文字
                map.addText(textOptions.position(provider.getPosByName(s).get(0).getLatLng()).text(s));
            }
        });
    }

    @Override
    public void showMultiDestRoute(List<Position[]> results) {
        PolylineOptions lineOptions = new PolylineOptions()
                .width(40)
                .lineJoinType(PolylineOptions.LineJoinType.LineJoinRound)
                .color(Color.parseColor("#1e90ff"))
                .setUseTexture(true)
                .setCustomTexture(BitmapDescriptorFactory.fromResource(R.drawable.line_arrow));
        for (int i = 0; i < results.getSize(); i++) {
            Position[] p = results.get(i);
            map.addPolyline(lineOptions.add(p[0].getLatLng(), p[1].getLatLng()));
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
}