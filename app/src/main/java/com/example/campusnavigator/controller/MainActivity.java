package com.example.campusnavigator.controller;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
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
import com.example.campusnavigator.model.Map;
import com.example.campusnavigator.model.MapManager;
import com.example.campusnavigator.model.Position;
import com.example.campusnavigator.model.Route;
import com.example.campusnavigator.model.SpotProvider;
import com.example.campusnavigator.utility.helpers.DialogHelper;
import com.example.campusnavigator.utility.helpers.OverlayHelper;
import com.example.campusnavigator.utility.interfaces.RouteResultReceiver;
import com.example.campusnavigator.utility.interfaces.SingleSelectListener;
import com.example.campusnavigator.utility.structures.List;
import com.example.campusnavigator.utility.structures.Stack;
import com.example.campusnavigator.window.MultiRouteWindow;
import com.example.campusnavigator.window.MultiSelectWindow;
import com.example.campusnavigator.window.SearchWindow;
import com.example.campusnavigator.window.SingleRouteWindow;
import com.example.campusnavigator.window.SingleSelectClickWindow;
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
    private MultiSelectWindow multiSelectWindow;
    private MultiRouteWindow multiRouteWindow;
    private SingleRouteWindow singleRouteWindow;
    private SingleSelectWindow singleSelectWindow;
    private SingleSelectClickWindow selectClickWindow;

    private OnLocationChangedListener locationListener; // 定位改变回调接口
    private AMapLocationClient locationClient; // 定位启动和销毁类

    // 路径计算结果
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

        // 设置地图属性
        setMap();

        // Marker点击监听
        map.setOnMarkerClickListener(marker -> {
            String markerId = marker.getId();
            Position spot = provider.getPosByMarkerId(markerId);
            if (spot == null) {
                return false;
            }
            switch (mode.mode()) {
                case DEFAULT:
                    selectClickWindow.setMarkerInfo(spot, myLocation);
                    Window.transition(searchWindow, selectClickWindow);
                    mode.change(Mode.M.S_SELECT_CLICK);
                    break;
                case S_SELECT_CLICK:
                    selectClickWindow.setMarkerInfo(spot, myLocation);
                    break;
                case S_SELECT:
                    // 选择点后唤起对话框
                    DialogHelper.showSpotSearchDialog(this, mode, provider, this, spot);
                    break;
                case M_SELECT:
                    List<Position> spotAttachList = manager.getSpotAttached(spot);
                    // 检查地点连接点空指针，不进行处理
                    if (spotAttachList == null) {
                        return false;
                    }

                    Position spotAttach = spotAttachList.get(0);
                    // 检查重复输入
                    if (!manager.isBufferEmpty() && spotAttach.equals(manager.bufferTop())) {
                        Toast.makeText(this, "选择重复", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    // 检查通过，将入口和目的地压入栈中
                    manager.pushBuffer(spotAttach);
                    provider.pushBuffer(spot);
                    multiSelectWindow.addPosition(spot);
                    OverlayHelper.onMarkerClicked(marker);
                    break;
                default:
                    break;
            }
            return true;
        });

        // 地图触摸监听，控制弹窗区域手势不生效，地图区域手势生效
        map.setOnMapTouchListener(latLng -> {
            int routeWindowY = singleRouteWindow.getWindowY();
            int multiRouteWindowY = multiRouteWindow.getWindowY();
            int multiSelectWindowY = multiSelectWindow.getWindowY();
            int selectClickWindowY = selectClickWindow.getWindowY();
            float touchY = latLng.getRawY();
            switch (mode.mode()) {
                case DEFAULT:
                    map.getUiSettings().setAllGesturesEnabled(true);
                    break;
                case S_ROUTE_OPEN: // 单点路径弹窗处于打开状态
                    // 触摸起始点位于弹窗外侧，关闭弹窗
                    if (latLng.getAction() == MotionEvent.ACTION_DOWN && touchY < routeWindowY) {
                        singleRouteWindow.closeBox();
                        singleRouteWindow.setExpendButtonUp(true);
                        map.getUiSettings().setAllGesturesEnabled(true);
                        mode.change(Mode.M.S_ROUTE_CLOSE);

                    } else if (touchY >= routeWindowY) { // 触摸点位于弹窗内侧
                        // 轨迹位于外侧时，由于起始点必定不在外侧，所以保持false状态
                        map.getUiSettings().setAllGesturesEnabled(false);
                    }
                    break;
                case M_ROUTE_OPEN: // 多点路径弹窗打开状态
                    if (latLng.getAction() == MotionEvent.ACTION_DOWN && touchY < multiRouteWindowY) {
                        multiRouteWindow.closeBox();
                        multiRouteWindow.setExpendButtonUp(true);
                        map.getUiSettings().setAllGesturesEnabled(true);
                        mode.change(Mode.M.M_ROUTE_CLOSE);

                    } else if (touchY >= multiRouteWindowY) { // 触摸点位于弹窗内侧
                        // 轨迹位于外侧时，由于起始点必定不在外侧，所以保持false状态
                        map.getUiSettings().setAllGesturesEnabled(false);
                    }
                    break;
                case S_ROUTE_CLOSE: // 单点路径弹窗处于关闭状态，触摸点位于外侧开启手势，位于内侧关闭手势
                    map.getUiSettings().setAllGesturesEnabled(touchY < routeWindowY);
                    break;
                case M_ROUTE_CLOSE:
                    map.getUiSettings().setAllGesturesEnabled(touchY < multiRouteWindowY);
                    break;
                case M_SELECT:
                    map.getUiSettings().setAllGesturesEnabled(touchY < multiSelectWindowY);
                    break;
                case S_SELECT_CLICK:
                    map.getUiSettings().setAllGesturesEnabled(touchY < selectClickWindowY);
                    break;
                default:
                    break;
            }
        });

        // searchWindow对象监听
        searchWindow.setSearchListener(view -> {
            if (mode.is(Mode.M.DEFAULT)) {
                DialogHelper.showSpotSearchDialog(this, mode, provider, this);
            }
        });

        searchWindow.setEntryListener(view -> {
            if (mode.is(Mode.M.DEFAULT)) { // 由初始状态切换到多点选择状态
                Window.transition(searchWindow, multiSelectWindow);
                mode.change(Mode.M.M_SELECT);
            }
        });

        // 多点选择地点监听
        multiSelectWindow.setButtonListener(view -> {
            if (mode.is(Mode.M.M_SELECT)) {
                if (manager.bufferSize() < 2) {
                    Toast.makeText(this, "地点数不足2个", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        manager.calculate(true, this);
                        mode.change(Mode.M.M_ROUTE_OPEN);
                    } catch (Exception e) {
                        String msg = "计算错误：" + e.getMessage();
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        Log.e("CamNav-CalculateError", msg);
                    }
                }
            }
        });

        multiSelectWindow.setRemoveListener(v -> {
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

        selectClickWindow.setButtonListener(v -> {
            Position position = selectClickWindow.selected;
            if (position != null) {
                calculateSingleRoute(position);
            }
        });


        // 单点路径结果弹窗监听
        singleRouteWindow.setExpendButtonListener(view -> {
            if (mode.is(Mode.M.S_ROUTE_OPEN)) { // 处于打开状态，关闭planBox
                singleRouteWindow.closeBox();
                singleRouteWindow.setExpendButtonUp(true);
                mode.change(Mode.M.S_ROUTE_CLOSE);
            } else if (mode.is(Mode.M.S_ROUTE_CLOSE)) { // 处于关闭状态，打开planBox
                singleRouteWindow.openBox();
                singleRouteWindow.setExpendButtonUp(false);
                mode.change(Mode.M.S_ROUTE_OPEN);
            }
        });

        int count = singleRouteWindow.getPlanCount();
        for (int i = 0; i < count; i++) {
            final int selected = i;
            singleRouteWindow.setPlanListener(i, v -> {
                singleRouteWindow.refreshSelected();
                List<List<Position>> route = Route.extractRoute(routeResults);
                singleRouteWindow.displayPlan(route, selected, myLocation);
            });
        }

        // 多点路径弹窗监听
        multiRouteWindow.setExpendButtonListener(v -> {
            if (mode.is(Mode.M.M_ROUTE_OPEN)) {
                multiRouteWindow.closeBox();
                multiRouteWindow.setExpendButtonUp(true);
                mode.change(Mode.M.M_ROUTE_CLOSE);
            } else if (mode.is(Mode.M.M_ROUTE_CLOSE)) {
                multiRouteWindow.openBox();
                multiRouteWindow.setExpendButtonUp(false);
                mode.change(Mode.M.M_ROUTE_OPEN);
            }
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

        CoordinatorLayout container = findViewById(R.id.view_container);
        searchWindow = new SearchWindow(this, container);
        multiSelectWindow = new MultiSelectWindow(this, container);
        multiRouteWindow = new MultiRouteWindow(this, container);
        singleRouteWindow = new SingleRouteWindow(this, container);
        singleSelectWindow = new SingleSelectWindow(this, container);
        selectClickWindow = new SingleSelectClickWindow(this, container);

        searchWindow.open();
    }

    private void setMap() {
        LatLng northeast = new LatLng(39.880384,116.488162);
        LatLng southwest = new LatLng(39.869582,116.477077);

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
    public void onSingleSelect() {
        Toast.makeText(this, "请选择你想去的地点~，按返回键返回", Toast.LENGTH_SHORT).show();
        Window.transition(searchWindow, singleSelectWindow);
        mode.change(Mode.M.S_SELECT);
    }

    @Override
    public void onDestReceive(Position dest) {
        calculateSingleRoute(dest);
    }

    private void calculateSingleRoute(@NonNull Position destPosition) {
        try {
            // 判断当前定位点是否存在
            if (myLocation == null) {
                throw new Exception("定位点未找到");
            }

            // 计算距定位点最近的连接点，定位点邻接点表attachPos
            List<Position> attachPos = manager.attachToMap(myLocation, destPosition);
            // 获取目的地邻接点，目的地邻接点表spotAttached
            List<Position> spotAttached = manager.getSpotAttached(destPosition);
            if (attachPos.length() == 0 || spotAttached == null || spotAttached.length() == 0) {
                throw new Exception("连接点错误");
            }

            // 清空先前的结果
            routeResults.clear();

            Position attach = attachPos.get(0);
            Position dest = spotAttached.get(0);
            manager.pushBuffer(attach);
            manager.pushBuffer(dest);
            manager.calculate(false, this);

            // 在循环中经过onSingleRouteSuccess生成规划结果数据，解析结果
            List<List<Position>> routes = Route.extractRoute(routeResults);
            List<Double> times = Route.extractTime(routeResults);
            List<Double> distances = Route.extractDist(routeResults);

            // 初始化数据
            singleRouteWindow.setDestName(destPosition.getName());
            singleRouteWindow.setRouteInfo(times, distances);
            singleRouteWindow.refreshSelected();

            // 展示
            singleRouteWindow.displayPlan(routes, 0, myLocation);

            // 更换布局
            if (searchWindow.isOpen()) {
                Window.transition(searchWindow, singleRouteWindow);
            } else if (singleSelectWindow.isOpen()) {
                Window.transition(singleSelectWindow, singleRouteWindow);
            } else if (selectClickWindow.isOpen()) {
                Window.transition(selectClickWindow, singleRouteWindow);
            }

            singleRouteWindow.openBox();
            mode.change(Mode.M.S_ROUTE_OPEN);
        } catch (Exception e) {
            mode.change(Mode.M.DEFAULT);
            String msg = "计算错误：" + e.getMessage();
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            Log.e("CamNav-CalculateError", msg);
        }
    }

    @Override
    public void onSingleRouteReceive(@NonNull List<Route> results) {
        // 每一对起点和终点返回2种方案，向容器中添加方案
        for (int i = 0; i < results.length(); i++) {
            Route result = results.get(i);
            routeResults.push(result);
        }
    }

    @Override
    public void onMultiRouteReceive(List<Route> results) {
        // 多地点只有一种方案，包含到多个地点的路径
        List<Position> route = Route.combineRoute(results);
        List<Double> times = Route.extractTime(results);
        List<Double> distances = Route.extractDist(results);
        // 初始化数据
        Stack<Position> buffer = provider.getBuffer();
        multiRouteWindow.setRouteInfo(buffer, times, distances);
        // 展示
        multiRouteWindow.displayRoute(route);
        // 清空展示之前的临时数据
        multiSelectWindow.removeAllPosition();
        provider.popBufferAll();
        // 更换布局
        Window.transition(multiSelectWindow, multiRouteWindow);
        multiRouteWindow.openBox();
    }

    @Override
    public void onBackPressed() {
        if (mode.is(Mode.M.S_SELECT)) {
            Window.transition(singleSelectWindow, searchWindow);
            mode.setDefault();

        } else if (mode.isSingleRoute()) { // 若当前处于单点路径结果弹窗
            Window.transition(singleRouteWindow, searchWindow);
            OverlayHelper.removeAllLines();
            mode.setDefault();

        } else if (mode.is(Mode.M.M_SELECT)) { // 若当前处于多点路径选择状态
            manager.popBufferAll();
            multiSelectWindow.removeAllPosition();
            Window.transition(multiSelectWindow, searchWindow);
            mode.setDefault();

        } else if (mode.isMultiRoute()) { // 若当前处于多点路径结果弹窗
            Window.transition(multiRouteWindow, searchWindow);
            OverlayHelper.removeAllLines();
            OverlayHelper.initAllMarkers();
            mode.setDefault();

        } else if (mode.is(Mode.M.S_SELECT_CLICK)) {
            Window.transition(selectClickWindow, searchWindow);
            mode.setDefault();

        } else {
            super.onBackPressed();
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