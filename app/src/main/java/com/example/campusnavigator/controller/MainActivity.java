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
import com.example.campusnavigator.model.PositionProvider;
import com.example.campusnavigator.utility.DialogHelper;
import com.example.campusnavigator.utility.List;
import com.example.campusnavigator.utility.callbacks.OnSpotSelectListener;
import com.example.campusnavigator.utility.OverlayManager;
import com.example.campusnavigator.utility.callbacks.RouteResultCallback;
import com.example.campusnavigator.utility.Stack;
import com.example.campusnavigator.utility.Tuple;
import com.example.campusnavigator.window.MultiRouteWindow;
import com.example.campusnavigator.window.MultiSelectWindow;
import com.example.campusnavigator.window.RouteWindow;
import com.example.campusnavigator.window.SearchWindow;


public class MainActivity extends AppCompatActivity implements LocationSource, AMapLocationListener, RouteResultCallback, OnSpotSelectListener {
    private MapView mapView;
    private AMap map = null;

    // 主要数据管理对象
    private PositionProvider provider;
    private MapManager manager;
    private OverlayManager overlayManager;
    private Stack<Position> spotBuffer = new Stack<>(); // 地点参数栈
    private Position myLocation;
    private Mode mode = Mode.DEFAULT;

    // 窗口对象
    private SearchWindow searchWindow;
    private MultiSelectWindow multiSelectWindow;
    private MultiRouteWindow multiRouteWindow;
    private RouteWindow routeWindow;

    private OnLocationChangedListener locationListener; // 定位改变回调接口
    private AMapLocationClient locationClient; // 定位启动和销毁类

    // 路径计算结果
    private List<Position> attachPos;
    private List<List<Tuple<Position, Position>>> routeOfPlans = new List<>();
    private List<Double> distanceOfPlans = new List<>();
    private List<Double> timeOfPlans = new List<>();


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

        // Marker点击监听
        map.setOnMarkerClickListener(marker -> {
            LatLng latLng = marker.getPosition();
            Position spot = provider.getPosByLatLng(latLng);
            switch (mode) {
                case SINGLE_SELECT:
                    // 选择点后唤起对话框
                    DialogHelper.showSpotSearchDialog(this, spot, this);
                    break;
                case MULTI_SELECT:
                    try {
                        List<Position> spotAttachList = Map.spotAttached.get(spot);
                        if (spotAttachList == null) {
                            throw new Exception("地点连接点错误");
                        }
                        Position spotAttach = spotAttachList.get(0);
                        if (spotBuffer.isNotEmpty() && spotAttach.equals(spotBuffer.top())) {
                            Toast.makeText(this, "选择重复", Toast.LENGTH_SHORT).show();
                        } else { // 检查通过，将顶点压入栈中
                            spotBuffer.push(spotAttach);
                            multiSelectWindow.addPosition(spot);
                            Toast.makeText(this, "已选中", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        onError(e);
                    }
                    break;
                default:
                    break;
            }
            return true;
        });

        // 地图触摸监听，控制弹窗区域手势不生效，地图区域手势生效
        map.setOnMapTouchListener(latLng -> {
            int routeWindowY = routeWindow.getWindowY();
            int multiRouteWindowY = multiRouteWindow.getWindowY();
            int multiSelectWindowY = multiSelectWindow.getWindowY();
            float touchY = latLng.getRawY();
            switch (mode) {
                case SINGLE_ROUTE_OPEN: // 单点路径弹窗处于打开状态
                    // 触摸起始点位于弹窗外侧，关闭弹窗
                    if (latLng.getAction() == MotionEvent.ACTION_DOWN && touchY < routeWindowY) {
                        mode = Mode.SINGLE_ROUTE_CLOSE;
                        routeWindow.closePlanBox();
                        routeWindow.setExpendButtonUp(true);
                        map.getUiSettings().setAllGesturesEnabled(true);
                    } else if (touchY >= routeWindowY) { // 触摸点位于弹窗内侧
                        // 轨迹位于外侧时，由于起始点必定不在外侧，所以保持false状态
                        map.getUiSettings().setAllGesturesEnabled(false);
                    }
                    break;
                case SINGLE_ROUTE_CLOSE: // 单点路径弹窗处于关闭状态，触摸点位于外侧开启手势，位于内侧关闭手势
                    map.getUiSettings().setAllGesturesEnabled(touchY < routeWindowY);
                    break;
                case MULTI_ROUTE: // 多点路径弹窗显示状态，触摸点位于外侧开启手势，位于内侧关闭手势
                    map.getUiSettings().setAllGesturesEnabled(touchY < multiRouteWindowY);
                    break;
                case MULTI_SELECT:
                    map.getUiSettings().setAllGesturesEnabled(touchY < multiSelectWindowY);
                    break;
                default:
                    break;
            }
        });

        // searchWindow对象监听
        searchWindow.setSearchFieldListener(view -> DialogHelper.showSpotSearchDialog(this, this));

        searchWindow.setMultiSelectEntryListener(view -> {
            if (mode == Mode.DEFAULT) { // 由初始状态切换到多点选择状态
                mode = Mode.MULTI_SELECT;
                searchWindow.close();
                multiSelectWindow.open();
            }
        });

        // 多点选择地点监听
        multiSelectWindow.setRouteButtonListener(view -> {
            if (mode == Mode.MULTI_SELECT) {
                try {
                    if (spotBuffer.size() < 2) {
                        // 恢复原状态
                        multiSelectWindow.close();
                        searchWindow.open();
                        spotBuffer.popAll();
                        throw new Exception("地点数不足");
                    }
                    mode = Mode.MULTI_ROUTE;
                    multiSelectWindow.close();
                    multiRouteWindow.open();
                    manager.getRoutePlan(spotBuffer, true, this);
                } catch (Exception e) {
                    onError(e);
                }
            }
        });

        multiSelectWindow.setSelectRemoveListener(v -> {
            boolean isRemove = multiSelectWindow.removePosition();
            if (!isRemove) {
                Toast.makeText(this, "无选中顶点", Toast.LENGTH_SHORT).show();
            }
        });

        // 单点路径结果弹窗监听
        routeWindow.setExpendButtonListener(view -> {
            if (mode == Mode.SINGLE_ROUTE_OPEN) { // 处于打开状态，关闭planBox
                mode = Mode.SINGLE_ROUTE_CLOSE;
                routeWindow.closePlanBox();
                routeWindow.setExpendButtonUp(true);
            } else if (mode == Mode.SINGLE_ROUTE_CLOSE) { // 处于关闭状态，打开planBox
                mode = Mode.SINGLE_ROUTE_OPEN;
                routeWindow.openPlanBox();
                routeWindow.setExpendButtonUp(false);
            }
        });

        int count = routeWindow.getPlanCount();
        for (int i = 0; i < count; i++) {
            final int selected = i;
            final int indexOfAttach = i / 2;
            routeWindow.setPlanListener(i, v -> {
                Position attach = attachPos.get(indexOfAttach);
                Tuple<Position, Position> attachToMe = new Tuple<>(attach, myLocation);
                routeWindow.displayPlan(routeOfPlans, selected, attachToMe, overlayManager);
            });
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

        CoordinatorLayout container = findViewById(R.id.view_container);
        searchWindow = new SearchWindow(this, container);
        multiSelectWindow = new MultiSelectWindow(this, container);
        multiRouteWindow = new MultiRouteWindow(this, container);
        routeWindow = new RouteWindow(this, container);
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
    public void onMapStateChange(Mode mode) {
        this.mode = mode;
        if (mode == Mode.SINGLE_SELECT) {
            Toast.makeText(this, "请选择你想去的地点", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestReceiveSuccess(String name) {
        overlayManager.removeLines(); // 清除线段
        routeWindow.setDestName(name);

        Position destPosition = provider.getPosByName(name).get(0);
        try {
            // 判断当前定位点是否存在
            if (myLocation == null) {
                throw new Exception("定位点未找到");
            }
            // 计算距定位点最近的连接点
            attachPos = manager.attachToMap(myLocation);
            List<Position> spotAttached = Map.spotAttached.get(destPosition);
            if (attachPos != null && attachPos.length() != 0 && spotAttached != null) {
                routeOfPlans.clear();
                distanceOfPlans.clear();
                timeOfPlans.clear();
                // 计算连接点到目的地的路径方案，共有2nm种方案
                for (Position attach : attachPos) {
                    for (Position dest : spotAttached) {
                        spotBuffer.push(attach);
                        spotBuffer.push(dest);
                        // 对于每一对起点和终点有2种方案
                        manager.getRoutePlan(spotBuffer, false, this);
                    }
                }
                // 在循环中经过onSuccess()生成规划结果数据，之后展示方案
                routeWindow.setPlansInfo(timeOfPlans, distanceOfPlans); // 设置planBox内容
                Tuple<Position, Position> attachToMe = new Tuple<>(attachPos.get(0), myLocation);
                routeWindow.displayPlan(routeOfPlans, 0, attachToMe, overlayManager);
            }
            // 更换布局
            searchWindow.close();
            routeWindow.open();
            routeWindow.openPlanBox();
        } catch (Exception e) {
            onDestReceiveError(e);
        }
    }

    @Override
    public void onDestReceiveError(Exception e) {
        mode = Mode.DEFAULT; // 恢复到初始状态
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e("MainActivity", e.toString());
    }

    @Override
    public void onSuccess(List<List<Tuple<Position, Position>>> results, List<Double> distances, List<Double> times, boolean isMultiSpot) {
        // 该方法传入路径计算结果并保存
        if (!isMultiSpot) {
            // 每一对起点和终点返回2种方案，向容器中添加方案
            for (int i = 0; i < 2; i++) {
                List<Tuple<Position, Position>> result = results.get(i);
                double dist = distances.get(i);
                double time = times.get(i);
                routeOfPlans.add(result);
                distanceOfPlans.add(dist);
                timeOfPlans.add(time);
            }
        } else {
            // 多地点只有一种方案，直接提取方案进行展示
            List<Tuple<Position, Position>> route = results.get(0);
            Double dist = distances.get(0);
            Double time = times.get(0);
            multiRouteWindow.setRouteInfo(time, dist);
            multiRouteWindow.displayRoute(route, overlayManager);
        }
    }

    @Override
    public void onError(Exception e) {
        mode = Mode.DEFAULT;
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e("MainActivity", e.toString());
    }

    @Override
    public void onBackPressed() {
        if (mode == Mode.SINGLE_ROUTE_OPEN || mode == Mode.SINGLE_ROUTE_CLOSE) { // 若当前处于单点路径结果弹窗
            mode = Mode.DEFAULT;
            routeWindow.close();
            searchWindow.open();
            overlayManager.removeLines();
        } else if (mode == Mode.MULTI_SELECT) { // 若当前处于多点路径选择状态
            mode = Mode.DEFAULT;
            spotBuffer.popAll();
            multiSelectWindow.removeAllPosition();
            multiSelectWindow.close();
            searchWindow.open();
        } else if (mode == Mode.MULTI_ROUTE) { // 若当前处于多点路径结果弹窗
            mode = Mode.DEFAULT;
            multiRouteWindow.close();
            searchWindow.open();
            overlayManager.removeLines();
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