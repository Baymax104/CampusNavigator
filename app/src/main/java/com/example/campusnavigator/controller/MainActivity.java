package com.example.campusnavigator.controller;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.example.campusnavigator.utility.OverlayManager;
import com.example.campusnavigator.utility.Stack;
import com.example.campusnavigator.utility.Tuple;
import com.google.android.material.card.MaterialCardView;

import java.util.Locale;


public class MainActivity extends AppCompatActivity implements LocationSource, AMapLocationListener,RouteResultCallback,OnSpotSelectListener {
    private MapView mapView;
    private AMap map = null;

    // 主要数据管理对象
    private PositionProvider provider;
    private MapManager manager;
    private OverlayManager overlayManager;
    private Stack<Position> spotBuffer = new Stack<>(); // 地点参数栈
    private Position myLocation;
    private int modeCode = 0;

    // 弹窗布局对象
    private CoordinatorLayout container;
    private View searchWindow;

    private View multiSelectWindow;
    private Button test;

    private View multiRouteWindow;

    private View routeWindow;
    private LinearLayout routeContainer;
    private View routePlanBox;
    private GridLayout planGroup;

    private OnLocationChangedListener locationListener; // 定位改变回调接口
    private AMapLocationClient locationClient; // 定位启动和销毁类

    // 路径计算结果
    private List<Position> attachPositions;
    private List<List<Tuple<Position, Position>>> routesPlan = new List<>();
    private List<Double> allDistance = new List<>();
    private List<Double> allTimes = new List<>();


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
            if (modeCode == 1) { // 处于单点选点状态
                DialogHelper.showSpotSearchDialog(this, position, this);
            } else if (modeCode == 4) { // 处于多点选点状态
                try {
                    List<Position> spotAttach = Map.spotAttached.get(position);
                    if (spotAttach == null) {
                        throw new Exception("地点连接点错误");
                    }
                    spotBuffer.push(spotAttach.get(0));
                } catch (Exception e) {
                    onError(e);
                }
            }
            return true;
        });

        TextView searchField = searchWindow.findViewById(R.id.search_field);
        searchField.setOnClickListener(view -> DialogHelper.showSpotSearchDialog(this, this));

        ImageView expendButton = routeWindow.findViewById(R.id.expend_button);
        expendButton.setOnClickListener(view -> {
            if (modeCode == 2) { // 处于打开状态，关闭planBox
                routeContainer.removeView(routePlanBox);
                modeCode = 3;
                expendButton.setImageResource(R.drawable.expend_arrow_up);
            } else if (modeCode == 3) { // 处于关闭状态，打开planBox
                routeContainer.addView(routePlanBox);
                modeCode = 2;
                expendButton.setImageResource(R.drawable.expend_arrow_down);
            }
        });

        map.setOnMapTouchListener(latLng -> {
            if (modeCode == 2) { // 弹窗处于打开状态
                int[] locationInScreen = new int[2];
                routeWindow.getLocationOnScreen(locationInScreen);
                float touchY = latLng.getRawY();
                // 触摸起始点位于弹窗外侧
                if (latLng.getAction() == MotionEvent.ACTION_DOWN && touchY < locationInScreen[1]) {
                    routeContainer.removeView(routePlanBox);
                    modeCode = 3;
                    expendButton.setImageResource(R.drawable.expend_arrow_up);
                    map.getUiSettings().setAllGesturesEnabled(true);
                } else if (touchY >= locationInScreen[1]) { // 触摸点位于弹窗内侧
                    // 轨迹位于外侧时，由于起始点必定不在外侧，所以保持false状态
                    map.getUiSettings().setAllGesturesEnabled(false);
                }
            } else if (modeCode == 3) { // 弹窗处于关闭状态，触摸点位于外侧开启手势，位于内侧关闭手势
                int[] locationInScreen = new int[2];
                routeWindow.getLocationOnScreen(locationInScreen);
                float touchY = latLng.getRawY();
                map.getUiSettings().setAllGesturesEnabled(touchY < locationInScreen[1]);
            }
        });

        MaterialCardView multiSelectCard = searchWindow.findViewById(R.id.multi_select_spot);
        multiSelectCard.setOnClickListener(view -> {
            if (modeCode == 0) { // 由初始状态切换到多点选择状态
                modeCode = 4;
                container.removeView(searchWindow);
                container.addView(multiSelectWindow);
            }
        });

        test.setOnClickListener(view -> {
            if (modeCode == 4) {
                modeCode = 5;
                container.removeView(multiSelectWindow);
                container.addView(multiRouteWindow);
                manager.getRoutePlan(spotBuffer, true, this);
            }
        });

        for (int i = 0; i < planGroup.getChildCount(); i++) {
            View planView = planGroup.getChildAt(i);
            final int index = i;
            planView.setOnClickListener(view -> {
                showRoutes(index, false);
                changePlanSelect(index);
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

        container = findViewById(R.id.view_container);
        searchWindow = LayoutInflater.from(this).inflate(R.layout.layout_search_window, container, false);
        routeWindow = LayoutInflater.from(this).inflate(R.layout.layout_route_window, container, false);
        multiSelectWindow = LayoutInflater.from(this).inflate(R.layout.layout_multi_search_window, container, false);
        multiRouteWindow = LayoutInflater.from(this).inflate(R.layout.layout_multi_route_window, container, false);
        container.addView(searchWindow);

        routeContainer = routeWindow.findViewById(R.id.route_card);
        routePlanBox = LayoutInflater.from(this).inflate(R.layout.layout_route_plan_box, routeContainer, false);
        planGroup = routePlanBox.findViewById(R.id.plan_group);
        routeContainer.addView(routePlanBox);

        test = multiSelectWindow.findViewById(R.id.test_button);
    }

    private void setMap() {
        LatLng southwest = new LatLng(39.869922,116.477077);
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
    public void onMapStateChange(int modeCode) {
        this.modeCode = modeCode;
        if (modeCode == 1) {
            Toast.makeText(this, "请选择你想去的地点", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestReceiveSuccess(String name) {
        overlayManager.removeLines(); // 清除线段
        Position destPosition = provider.getPosByName(name).get(0);
        TextView destName = routeWindow.findViewById(R.id.dest_name);
        destName.setText(name);

        try {
            // 判断当前定位点是否存在
            if (myLocation == null) {
                throw new Exception("定位点未找到");
            }
            // 计算距定位点最近的连接点
            attachPositions = manager.attachToMap(myLocation);
            List<Position> spotAttached = Map.spotAttached.get(destPosition);
            if (attachPositions != null && attachPositions.length() != 0 && spotAttached != null) {
                routesPlan.clear();
                allDistance.clear();
                allTimes.clear();
                // 计算连接点到目的地的路径方案，共有2nm种方案
                for (Position attach : attachPositions) {
                    for (Position dest : spotAttached) {
                        spotBuffer.push(attach);
                        spotBuffer.push(dest);
                        // 对于每一对起点和终点有2种方案
                        manager.getRoutePlan(spotBuffer, false, this);
                    }
                }
                // 在循环中生成规划结果数据，之后进行绘制
                showDistAndTime(); // 设置planBox内容
                showRoutes(0, false); // 首先展示第一种方案路线
                changePlanSelect(0); // 选中第一个方案View
            }
            // 更换布局
            container.removeView(searchWindow);
            container.addView(routeWindow);
            if (routeContainer.findViewById(R.id.route_plan) == null) {
                routeContainer.addView(routePlanBox);
            }
        } catch (Exception e) {
            onDestReceiveError(e);
        }
    }

    @Override
    public void onDestReceiveError(Exception e) {
        modeCode = 0; // 恢复到初始状态
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e("MainActivity", e.toString());
    }

    @Override
    public void onSuccess(List<List<Tuple<Position, Position>>> results, List<Double> distances, List<Double> times, boolean isMultiSpot) {
        if (!isMultiSpot) {
            // 每一对起点和终点返回2种方案，向容器中添加方案
            for (int i = 0; i < 2; i++) {
                List<Tuple<Position, Position>> result = results.get(i);
                double dist = distances.get(i);
                double time = times.get(i);
                routesPlan.add(result);
                allDistance.add(dist);
                allTimes.add(time);
            }
        } else {
            // 多地点只有一种方案，直接赋值引用
            this.routesPlan = results;
            this.allDistance = distances;
            this.allTimes = times;
            showRoutes(0, true);
        }
    }

    @Override
    public void onError(Exception e) {
        modeCode = 0;
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e("MainActivity", e.toString());
    }

    private void showDistAndTime() {
        for (int i = 0; i < planGroup.getChildCount(); i++) {
            View planView = planGroup.getChildAt(i);
            TextView timeTxt = planView.findViewById(R.id.plan_time);
            TextView distanceTxt = planView.findViewById(R.id.plan_distance);
            int time = allTimes.get(i).intValue();
            int distance = allDistance.get(i).intValue();
            timeTxt.setText(String.format(Locale.CHINA, "%d分钟", time));
            distanceTxt.setText(String.format(Locale.CHINA, "%d米", distance));
        }
    }

    private void showRoutes(int planIndex, boolean isMultiSpot) {
        overlayManager.removeLines();
        List<Tuple<Position, Position>> route = routesPlan.get(planIndex);
        for (int i = 0; i < route.length(); i++) {
            Tuple<Position, Position> p = route.get(i);
            if (i == 0) {
                overlayManager.drawLine(p.first, p.second);
            } else {
                overlayManager.drawLine(p.second);
            }
        }
        if (!isMultiSpot) {
            overlayManager.drawLine(attachPositions.get(0), myLocation);
        } else {
            TextView distanceInfo = multiRouteWindow.findViewById(R.id.distance_info);
            TextView timeInfo = multiRouteWindow.findViewById(R.id.time_info);
            int distance = allDistance.get(planIndex).intValue();
            int time = allTimes.get(planIndex).intValue();
            distanceInfo.setText(String.format(Locale.CHINA, "距离：%d米", distance));
            timeInfo.setText(String.format(Locale.CHINA, "时间：%d分钟", time));
        }
    }

    private void changePlanSelect(int index) {
        for (int i = 0; i < planGroup.getChildCount(); i++) {
            View child = planGroup.getChildAt(i);
            if (index == i) { // 选中状态
                child.setBackgroundResource(R.drawable.shape_plan_item_bg_selected);
            } else { // 未选中状态
                child.setBackgroundResource(R.drawable.shape_plan_item_bg_normal);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (modeCode == 2 || modeCode == 3) { // 若当前处于单点路径结果弹窗
            modeCode = 0;
            container.removeView(routeWindow);
            container.addView(searchWindow);
            overlayManager.removeLines();
        } else if (modeCode == 4) { // 若当前处于多点路径选择状态
            modeCode = 0;
            container.removeView(multiSelectWindow);
            container.addView(searchWindow);
        } else if (modeCode == 5) { // 若当前处于多点路径结果弹窗
            modeCode = 0;
            container.removeView(multiRouteWindow);
            container.addView(searchWindow);
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