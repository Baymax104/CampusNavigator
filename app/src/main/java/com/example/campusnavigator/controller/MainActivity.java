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
import com.example.campusnavigator.model.RouteResult;
import com.example.campusnavigator.utility.Mode;
import com.example.campusnavigator.utility.callbacks.SingleSelectListener;
import com.example.campusnavigator.utility.callbacks.RouteResultReceiver;
import com.example.campusnavigator.utility.helpers.DialogHelper;
import com.example.campusnavigator.utility.helpers.OverlayHelper;
import com.example.campusnavigator.utility.structures.List;
import com.example.campusnavigator.utility.structures.Stack;
import com.example.campusnavigator.window.MultiRouteWindow;
import com.example.campusnavigator.window.MultiSelectWindow;
import com.example.campusnavigator.window.RouteWindow;
import com.example.campusnavigator.window.SearchWindow;


public class MainActivity extends AppCompatActivity implements LocationSource, AMapLocationListener, RouteResultReceiver, SingleSelectListener {
    private MapView mapView;
    private AMap map = null;

    // 主要数据管理对象
    private PositionProvider provider;
    private MapManager manager;
    private Stack<Position> spotBuffer = new Stack<>(); // 地点参数栈
    private Stack<Position> destBuffer = new Stack<>(); // 目的地栈
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
    private List<RouteResult> routeResults = new List<>();


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
        provider = new PositionProvider(this);
        manager = new MapManager(this);
        OverlayHelper.bind(map, mapView, this);
        // 设置地图属性
        setMap();

        // Marker点击监听
        map.setOnMarkerClickListener(marker -> {
            LatLng latLng = marker.getPosition();
            Position spot = provider.getPosByLatLng(latLng);
            switch (mode) {
                case SINGLE_SELECT:
                    // 选择点后唤起对话框
                    DialogHelper.showSpotSearchDialog(this, provider, this, spot);
                    break;
                case MULTI_SELECT:
                    List<Position> spotAttachList = Map.spotAttached.get(spot);
                    // 检查地点连接点空指针
                    if (spotAttachList == null) {
                        Toast.makeText(this, "地点选择错误，请重新选择", Toast.LENGTH_SHORT).show();
                    } else {
                        Position spotAttach = spotAttachList.get(0);
                        // 检查重复输入
                        if (spotBuffer.isNotEmpty() && spotAttach.equals(spotBuffer.top())) {
                            Toast.makeText(this, "选择重复", Toast.LENGTH_SHORT).show();
                        } else { // 检查通过，将入口和目的地压入栈中
                            spotBuffer.push(spotAttach);
                            destBuffer.push(spot);
                            multiSelectWindow.addPosition(spot);
                        }
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
                        routeWindow.closePlanBox();
                        routeWindow.setExpendButtonUp(true);
                        map.getUiSettings().setAllGesturesEnabled(true);
                        mode = Mode.SINGLE_ROUTE_CLOSE;
                    } else if (touchY >= routeWindowY) { // 触摸点位于弹窗内侧
                        // 轨迹位于外侧时，由于起始点必定不在外侧，所以保持false状态
                        map.getUiSettings().setAllGesturesEnabled(false);
                    }
                    break;
                case MULTI_ROUTE_OPEN: // 多点路径弹窗打开状态
                    if (latLng.getAction() == MotionEvent.ACTION_DOWN && touchY < multiRouteWindowY) {
                        multiRouteWindow.closeSpotBox();
                        multiRouteWindow.setExpendButtonUp(true);
                        map.getUiSettings().setAllGesturesEnabled(true);
                        mode = Mode.MULTI_ROUTE_CLOSE;
                    } else if (touchY >= multiRouteWindowY) { // 触摸点位于弹窗内侧
                        // 轨迹位于外侧时，由于起始点必定不在外侧，所以保持false状态
                        map.getUiSettings().setAllGesturesEnabled(false);
                    }
                    break;
                case SINGLE_ROUTE_CLOSE: // 单点路径弹窗处于关闭状态，触摸点位于外侧开启手势，位于内侧关闭手势
                    map.getUiSettings().setAllGesturesEnabled(touchY < routeWindowY);
                    break;
                case MULTI_ROUTE_CLOSE:
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
        searchWindow.setSearchFieldListener(view -> {
            if (mode == Mode.DEFAULT) {
                DialogHelper.showSpotSearchDialog(this, provider, this);
            }
        });

        searchWindow.setMultiSelectEntryListener(view -> {
            if (mode == Mode.DEFAULT) { // 由初始状态切换到多点选择状态
                searchWindow.close();
                multiSelectWindow.open();
                mode = Mode.MULTI_SELECT;
            }
        });

        // 多点选择地点监听
        multiSelectWindow.setRouteButtonListener(view -> {
            if (mode == Mode.MULTI_SELECT) {
                if (spotBuffer.size() < 2) {
                    Toast.makeText(this, "地点数不足2个", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        manager.calculateRoutePlan(spotBuffer, true, this);
                        mode = Mode.MULTI_ROUTE_OPEN;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        multiSelectWindow.setSelectRemoveListener(v -> {
            boolean isCompleted = multiSelectWindow.removePosition();
            if (isCompleted) {
                spotBuffer.pop();
                destBuffer.pop();
            } else {
                Toast.makeText(this, "无选中顶点", Toast.LENGTH_SHORT).show();
            }
        });

        // 单点路径结果弹窗监听
        routeWindow.setExpendButtonListener(view -> {
            if (mode == Mode.SINGLE_ROUTE_OPEN) { // 处于打开状态，关闭planBox
                routeWindow.closePlanBox();
                routeWindow.setExpendButtonUp(true);
                mode = Mode.SINGLE_ROUTE_CLOSE;
            } else if (mode == Mode.SINGLE_ROUTE_CLOSE) { // 处于关闭状态，打开planBox
                routeWindow.openPlanBox();
                routeWindow.setExpendButtonUp(false);
                mode = Mode.SINGLE_ROUTE_OPEN;
            }
        });

        int count = routeWindow.getPlanCount();
        for (int i = 0; i < count; i++) {
            final int selected = i;
            routeWindow.setPlanListener(i, v -> {
                routeWindow.refreshSelected();
                List<List<Position>> route = RouteResult.extractRoute(routeResults);
                routeWindow.displayPlan(route, selected, myLocation);
            });
        }

        // 多点路径弹窗监听
        multiRouteWindow.setExpendButtonListener(v -> {
            if (mode == Mode.MULTI_ROUTE_OPEN) {
                multiRouteWindow.closeSpotBox();
                multiRouteWindow.setExpendButtonUp(true);
                mode = Mode.MULTI_ROUTE_CLOSE;
            } else if (mode == Mode.MULTI_ROUTE_CLOSE) {
                multiRouteWindow.openSpotBox();
                multiRouteWindow.setExpendButtonUp(false);
                mode = Mode.MULTI_ROUTE_OPEN;
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
            OverlayHelper.drawMarker(provider.getPosByName(s));
            // 设置文字
            OverlayHelper.drawText(provider.getPosByName(s), s);
        }
    }

    @Override
    public void onSingleSelect() {
        Toast.makeText(this, "请选择你想去的地点~", Toast.LENGTH_SHORT).show();
        searchWindow.close();
        mode = Mode.SINGLE_SELECT;
    }

    @Override
    public void onDestReceiveSuccess(Position dest) {
        routeWindow.setDestName(dest.getName());
        calculateRoute(dest);
    }

    @Override
    public void onDestReceiveError(Exception e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e("MainActivity", e.toString());
        mode = Mode.DEFAULT; // 恢复到初始状态
    }

    private void calculateRoute(Position destPosition) {
        try {
            // 判断当前定位点是否存在
            if (myLocation == null) {
                throw new Exception("定位点未找到");
            }

            // 计算距定位点最近的连接点，定位点邻接点表attachPos
            List<Position> attachPos = manager.attachToMap(myLocation);
            // 获取目的地邻接点，目的地邻接点表spotAttached
            List<Position> spotAttached = Map.spotAttached.get(destPosition);
            if (attachPos == null || attachPos.length() == 0 ||
                    spotAttached == null || spotAttached.length() == 0) {
                throw new Exception("连接点错误");
            }

            // 清空先前的结果
            routeResults.clear();

            // 计算连接点到目的地的路径方案，共有2nm种方案
            for (Position attach : attachPos) {
                for (Position dest : spotAttached) {
                    if (attach == null || dest == null) {
                        throw new Exception("连接点错误");
                    }
                    spotBuffer.push(attach);
                    spotBuffer.push(dest);
                    // 对于每一对起点和终点有2种方案
                    manager.calculateRoutePlan(spotBuffer, false, this);
                }
            }

            // 在循环中经过onSingleRouteSuccess生成规划结果数据，解析结果
            List<List<Position>> routes = RouteResult.extractRoute(routeResults);
            List<Double> times = RouteResult.extractTime(routeResults);
            List<Double> distances = RouteResult.extractDist(routeResults);
            // 初始化数据
            routeWindow.notifyRouteInfo(times, distances);
            routeWindow.refreshSelected();
            // 展示
            routeWindow.displayPlan(routes, 0, myLocation);
            // 更换布局
            searchWindow.close();
            routeWindow.open();
            routeWindow.openPlanBox();
            mode = Mode.SINGLE_ROUTE_OPEN;
        } catch (Exception e) {
            mode = Mode.DEFAULT;
            String errorMsg = "计算错误：" + e.getMessage();
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            Log.e("MainActivity", errorMsg);
            e.printStackTrace();
        }
    }

    @Override
    public void onSingleRouteReceive(List<RouteResult> results) {
        // 每一对起点和终点返回2种方案，向容器中添加方案
        for (int i = 0; i < 2; i++) {
            RouteResult result = results.get(i);
            routeResults.add(result);
        }
    }

    @Override
    public void onMultiRouteReceive(List<RouteResult> results) {
        // 多地点只有一种方案，包含到多个地点的路径
        List<Position> route = RouteResult.combineRoute(results);
        List<Double> times = RouteResult.extractTime(results);
        List<Double> distances = RouteResult.extractDist(results);
        // 初始化数据
        multiRouteWindow.notifyRouteInfo(destBuffer, times, distances);
        // 展示
        multiRouteWindow.displayRoute(route);
        // 清空展示之前的临时数据
        multiSelectWindow.removeAllPosition();
        destBuffer.popAll();
        // 更换布局
        multiSelectWindow.close();
        multiRouteWindow.open();
        multiRouteWindow.openSpotBox();
    }

    @Override
    public void onBackPressed() {
        if (mode == Mode.SINGLE_SELECT) {
            searchWindow.open();
            mode = Mode.DEFAULT;
        } else if (mode == Mode.SINGLE_ROUTE_OPEN || mode == Mode.SINGLE_ROUTE_CLOSE) { // 若当前处于单点路径结果弹窗
            routeWindow.close();
            searchWindow.open();
            OverlayHelper.removeLines();
            mode = Mode.DEFAULT;
        } else if (mode == Mode.MULTI_SELECT) { // 若当前处于多点路径选择状态
            spotBuffer.popAll();
            multiSelectWindow.removeAllPosition();
            multiSelectWindow.close();
            searchWindow.open();
            mode = Mode.DEFAULT;
        } else if (mode == Mode.MULTI_ROUTE_OPEN || mode == Mode.MULTI_ROUTE_CLOSE) { // 若当前处于多点路径结果弹窗
            multiRouteWindow.close();
            searchWindow.open();
            OverlayHelper.removeLines();
            mode = Mode.DEFAULT;
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