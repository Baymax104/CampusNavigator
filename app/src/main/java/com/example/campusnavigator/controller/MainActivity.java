package com.example.campusnavigator.controller;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

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
import com.example.campusnavigator.model.M;
import com.example.campusnavigator.model.Map;
import com.example.campusnavigator.model.MapManager;
import com.example.campusnavigator.model.Mode;
import com.example.campusnavigator.model.Position;
import com.example.campusnavigator.model.Route;
import com.example.campusnavigator.model.SpotProvider;
import com.example.campusnavigator.utility.helpers.DialogHelper;
import com.example.campusnavigator.utility.helpers.OverlayHelper;
import com.example.campusnavigator.utility.interfaces.RouteResultReceiver;
import com.example.campusnavigator.utility.interfaces.RouteWindow;
import com.example.campusnavigator.utility.interfaces.SingleSelectListener;
import com.example.campusnavigator.utility.structures.List;
import com.example.campusnavigator.window.MultiRouteWindow;
import com.example.campusnavigator.window.MultiSelectWindow;
import com.example.campusnavigator.window.SearchWindow;
import com.example.campusnavigator.window.SelectClickWindow;
import com.example.campusnavigator.window.SingleRouteWindow;
import com.example.campusnavigator.window.SingleSelectWindow;
import com.example.campusnavigator.window.Window;


public class MainActivity extends AppCompatActivity implements LocationSource, AMapLocationListener, RouteResultReceiver, SingleSelectListener {
    private MapView mapView;
    private AMap map = null;

    // 主要数据管理对象
    private SpotProvider provider;
    private MapManager manager;
    private Position myLocation;
    private final Mode mode = new Mode();

    // 窗口对象
    private SearchWindow searchWindow;
    private SpotSearchDialog searchDialog;
    private MultiSelectWindow multiSelectWindow;
    private MultiRouteWindow multiRouteWindow;
    private SingleRouteWindow singleRouteWindow;
    private SingleSelectWindow singleSelectWindow;
    private SelectClickWindow selectClickWindow;

    private OnLocationChangedListener locationListener; // 定位改变回调接口
    private AMapLocationClient locationClient; // 定位启动和销毁类

    // 单点路径计算结果
    private final List<Route> routeResults = new List<>();


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

        // Map绑定Context
        Map.bind(this);
        provider = SpotProvider.getInstance();
        manager = MapManager.getInstance();
        OverlayHelper.bind(map, mapView, this);

        // 初始化窗口
        initWindow();

        // 设置地图属性
        setMap();

        // Marker点击监听
        map.setOnMarkerClickListener(marker -> {
            String markerId = marker.getId();
            Position spot = provider.getPosByMarkerId(markerId);
            if (spot == null) {
                return false;
            }
            if (mode.is(M.DEFAULT)) {
                selectClickWindow.setMarkerInfo(spot, myLocation);
                mode.changeTo(M.S_SELECT_CLICK);

            } else if (mode.is(M.S_SELECT_CLICK)) {
                selectClickWindow.setMarkerInfo(spot, myLocation);

            } else if (mode.is(M.S_SELECT)) {
                searchDialog.show();
                searchDialog.setSelected(spot);

            } else if (mode.is(M.M_SELECT)) {
                // 检查重复输入
                if (!provider.isBufferEmpty() && spot.equals(provider.bufferTop())) {
                    Toast.makeText(this, "选择重复", Toast.LENGTH_SHORT).show();
                    return false;
                }
                // 压入栈中，视图相应变化
                provider.pushBuffer(spot);
                multiSelectWindow.addPosition(spot);
                OverlayHelper.onMarkerClicked(marker);
            }
            return true;
        });

        // 地图触摸监听，控制弹窗区域手势不生效，地图区域手势生效
        map.setOnMapTouchListener(latLng -> {
            float touchY = latLng.getRawY();
            if (mode.is(M.DEFAULT)) {
                map.getUiSettings().setAllGesturesEnabled(true);

            } else if (mode.isRouteOpen()) {
                Window w = mode.getState().getWindow();
                RouteWindow routeWindow = (RouteWindow) w;
                routeWindow.autoGestureControl(latLng, map, w);

            } else {
                int windowY = mode.getState().getWindow().getWindowY();
                map.getUiSettings().setAllGesturesEnabled(touchY < windowY);
            }
        });

        // searchWindow对象监听
        searchWindow.setSearchListener(mode, v -> {
            searchDialog.show();
            searchDialog.setSelected(null);
        });

        searchWindow.setEntryListener(mode, v -> mode.changeTo(M.M_SELECT));

        // 多点选择地点监听
        multiSelectWindow.setButtonListener(mode, view -> {
            if (provider.bufferSize() < 2) {
                Toast.makeText(this, "地点数不足2个", Toast.LENGTH_SHORT).show();
            } else {
                List<Position> dests = provider.getBuffer().toList(true);
                calculateMultiRoute(dests, Map.FOOT_PASS);
            }
        });

        multiSelectWindow.setRemoveListener(mode, v -> {
            boolean isCompleted = multiSelectWindow.removePosition();
            if (isCompleted) {
                Position removedSpot = provider.bufferTop();
                manager.popBuffer();
                provider.popBuffer();
                OverlayHelper.onSpotRemoved(removedSpot);
            } else {
                Toast.makeText(this, "无选中顶点", Toast.LENGTH_SHORT).show();
            }
        });

        selectClickWindow.setButtonListener(mode, selected -> {
            if (selected != null) {
                calculateSingleRoute(selected, Map.FOOT_PASS);
            }
        });

        // 单点路径窗口监听
        singleRouteWindow.setWayChangeListener(mode, (dest, group, checkedId) -> {
            if (checkedId == R.id.segment_footway) {
                calculateSingleRoute(dest, Map.FOOT_PASS);
            } else if (checkedId == R.id.segment_driveway) {
                calculateSingleRoute(dest, Map.DRIVE_PASS);
            }
        });

        // 多点路径窗口监听
        multiRouteWindow.setWayChangeListener(mode, (dests, group, checkedId) -> {
            if (checkedId == R.id.segment_footway) {
                calculateMultiRoute(dests, Map.FOOT_PASS);
            } else if (checkedId == R.id.segment_driveway) {
                calculateMultiRoute(dests, Map.DRIVE_PASS);
            }
        });
    }

    @Override
    public void onSingleSelect() {
        Toast.makeText(this, "请选择你想去的地点~，按返回键返回", Toast.LENGTH_SHORT).show();
        mode.changeTo(M.S_SELECT);
    }

    @Override
    public void onDestReceive(Position dest) {
        calculateSingleRoute(dest, Map.FOOT_PASS);
    }

    private void calculateSingleRoute(@NonNull Position destPosition, int pass) {
        try {
            // 判断当前定位点是否存在
            if (myLocation == null) {
                throw new Exception("定位点未找到");
            }

            // 计算距定位点最近的连接点
            Position attach = manager.attachToMap(myLocation, destPosition, pass);
            // 获取目的地邻接点，目的地邻接点表spotAttached
            List<Position> spotAttached = manager.getSpotAttached(destPosition, pass);
            if (spotAttached == null) {
                throw new Exception("连接点错误");
            }

            // 清空先前的结果
            routeResults.clear();
            for (Position dest : spotAttached) {
                if (pass >= dest.getPass()) {
                    manager.pushBuffer(attach);
                    manager.pushBuffer(dest);
                    manager.calculate(false, pass, this);
                }
            }
            // 筛选最优方案
            manager.filter(routeResults);

            // 在循环中经过onSingleRouteSuccess生成规划结果数据，解析结果
            singleRouteWindow.set(routeResults, destPosition, myLocation);
            mode.changeTo(M.S_ROUTE);

        } catch (Exception e) {
            mode.changeTo(M.DEFAULT);
            String msg = "单点计算错误：" + e.getMessage();
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            Log.e("CamNav-CalculateError", msg);
        }
    }

    private void calculateMultiRoute(List<Position> dests, int pass) {
        try {
            for (Position dest : dests) {
                List<Position> spotAttach = manager.getSpotAttached(dest, pass);
                if (spotAttach == null) {
                    throw new Exception("连接点错误");
                }
                // 此处邻接点的选取还是有点欠妥
                manager.pushBuffer(spotAttach.get(0));
            }
            manager.calculate(true, pass, this);

        } catch (Exception e) {
            mode.changeTo(M.DEFAULT);
            String msg = "多点计算错误：" + e.getMessage();
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            Log.e("CamNav-CalculateError", msg);
        }
    }

    @Override
    public void onSingleRouteReceive(@NonNull List<Route> results) {
        for (int i = 0; i < results.length(); i++) {
            Route result = results.get(i);
            routeResults.push(result);
        }
    }

    @Override
    public void onMultiRouteReceive(List<Route> results) {
        // 多点只有一种方案，直接展示
        List<Position> dests = provider.getBuffer().toList(true);
        multiRouteWindow.set(results, dests);
        mode.changeTo(M.M_ROUTE);
    }

    @Override
    public void onBackPressed() {
        if (mode.is(M.DEFAULT)) {
            super.onBackPressed();
        } else {
            M last = mode.getState();
            mode.changeTo(M.DEFAULT);
            // 关闭采用饿汉式，防止前一状态的相关方法再次触发
            if (last == M.S_ROUTE) {
                OverlayHelper.removeAllLines();
                singleRouteWindow.initChecked();
            } else if (last == M.M_ROUTE) {
                OverlayHelper.removeAllLines();
                OverlayHelper.initAllMarkers();
                multiRouteWindow.initChecked();
                multiSelectWindow.removeAllPosition();
                provider.popBufferAll();
            } else if (last == M.M_SELECT) {
                provider.popBufferAll();
                multiSelectWindow.removeAllPosition();
            }
        }
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
    }

    private void initWindow() {
        CoordinatorLayout container = findViewById(R.id.view_container);
        searchWindow = SearchWindow.newInstance(this, container);
        multiSelectWindow = MultiSelectWindow.newInstance(this, container);
        multiRouteWindow = MultiRouteWindow.newInstance(this, container);
        singleRouteWindow = SingleRouteWindow.newInstance(this, container);
        singleSelectWindow = SingleSelectWindow.newInstance(this, container);
        selectClickWindow = SelectClickWindow.newInstance(this, container);

        searchDialog = DialogHelper.buildSpotSearchDialog(this, mode, provider, this);

        searchWindow.open();
    }

    private void setMap() {
        LatLng northeast = new LatLng(39.880384,116.488162);
        LatLng southwest = new LatLng(39.869273,116.477091);

        MyLocationStyle locationStyle = new MyLocationStyle()
                .interval(1200)
                .myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
                .strokeColor(Color.TRANSPARENT)
                .radiusFillColor(Color.TRANSPARENT)
                .showMyLocation(true);

        map.showMapText(false);
        map.setMapStatusLimits(new LatLngBounds(southwest, northeast));
        map.setLocationSource(this);
        map.setMyLocationStyle(locationStyle);
        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(false);

        List<String> spotNames = provider.allNames();
        for (String n : spotNames) {
            runOnUiThread(() -> {
                OverlayHelper.drawMarker(provider.getPosition(n));
                OverlayHelper.drawText(provider.getPosition(n), n);
            });
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
                Log.e("CamNav-LocationError", e.getMessage());
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