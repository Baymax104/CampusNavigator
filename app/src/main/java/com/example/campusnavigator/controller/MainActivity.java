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

    // ????????????????????????
    private SpotProvider provider;
    private MapManager manager;
    private Position myLocation;
    private final Mode mode = new Mode();

    // ????????????
    private SearchWindow searchWindow;
    private SpotSearchDialog searchDialog;
    private MultiSelectWindow multiSelectWindow;
    private MultiRouteWindow multiRouteWindow;
    private SingleRouteWindow singleRouteWindow;
    private SingleSelectWindow singleSelectWindow;
    private SelectClickWindow selectClickWindow;

    private OnLocationChangedListener locationListener; // ????????????????????????
    private AMapLocationClient locationClient; // ????????????????????????

    // ????????????????????????
    private final List<Route> routeResults = new List<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // ????????????
        privacyCompliance();
        // ?????????
        initView();
        mapView.onCreate(savedInstanceState);
        if (map == null) {
            map = mapView.getMap();
        }

        // Map??????Context
        Map.bind(this);
        provider = SpotProvider.getInstance();
        manager = MapManager.getInstance();
        OverlayHelper.bind(map, mapView, this);

        // ???????????????
        initWindow();

        // ??????????????????
        setMap();

        // Marker????????????
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
                // ??????????????????
                if (!provider.isBufferEmpty() && spot.equals(provider.bufferTop())) {
                    Toast.makeText(this, "????????????", Toast.LENGTH_SHORT).show();
                    return false;
                }
                // ?????????????????????????????????
                provider.pushBuffer(spot);
                multiSelectWindow.addPosition(spot);
                OverlayHelper.onMarkerClicked(marker);
            }
            return true;
        });

        // ?????????????????????????????????????????????????????????????????????????????????
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

        // searchWindow????????????
        searchWindow.setSearchListener(mode, v -> {
            searchDialog.show();
            searchDialog.setSelected(null);
        });

        searchWindow.setEntryListener(mode, v -> mode.changeTo(M.M_SELECT));

        // ????????????????????????
        multiSelectWindow.setButtonListener(mode, view -> {
            if (provider.bufferSize() < 2) {
                Toast.makeText(this, "???????????????2???", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "???????????????", Toast.LENGTH_SHORT).show();
            }
        });

        selectClickWindow.setButtonListener(mode, selected -> {
            if (selected != null) {
                calculateSingleRoute(selected, Map.FOOT_PASS);
            }
        });

        // ????????????????????????
        singleRouteWindow.setWayChangeListener(mode, (dest, group, checkedId) -> {
            if (checkedId == R.id.segment_footway) {
                calculateSingleRoute(dest, Map.FOOT_PASS);
            } else if (checkedId == R.id.segment_driveway) {
                calculateSingleRoute(dest, Map.DRIVE_PASS);
            }
        });

        // ????????????????????????
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
        Toast.makeText(this, "???????????????????????????~?????????????????????", Toast.LENGTH_SHORT).show();
        mode.changeTo(M.S_SELECT);
    }

    @Override
    public void onDestReceive(Position dest) {
        calculateSingleRoute(dest, Map.FOOT_PASS);
    }

    private void calculateSingleRoute(@NonNull Position destPosition, int pass) {
        try {
            // ?????????????????????????????????
            if (myLocation == null) {
                throw new Exception("??????????????????");
            }

            // ????????????????????????????????????
            Position attach = manager.attachToMap(myLocation, destPosition, pass);
            // ????????????????????????????????????????????????spotAttached
            List<Position> spotAttached = manager.getSpotAttached(destPosition, pass);
            if (spotAttached == null) {
                throw new Exception("???????????????");
            }

            // ?????????????????????
            routeResults.clear();
            for (Position dest : spotAttached) {
                if (pass >= dest.getPass()) {
                    manager.pushBuffer(attach);
                    manager.pushBuffer(dest);
                    manager.calculate(false, pass, this);
                }
            }
            // ??????????????????
            manager.filter(routeResults);

            // ??????????????????onSingleRouteSuccess???????????????????????????????????????
            singleRouteWindow.set(routeResults, destPosition, myLocation);
            mode.changeTo(M.S_ROUTE);

        } catch (Exception e) {
            mode.changeTo(M.DEFAULT);
            String msg = "?????????????????????" + e.getMessage();
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            Log.e("CamNav-CalculateError", msg);
        }
    }

    private void calculateMultiRoute(List<Position> dests, int pass) {
        try {
            for (Position dest : dests) {
                List<Position> spotAttach = manager.getSpotAttached(dest, pass);
                if (spotAttach == null) {
                    throw new Exception("???????????????");
                }
                // ??????????????????????????????????????????
                manager.pushBuffer(spotAttach.get(0));
            }
            manager.calculate(true, pass, this);

        } catch (Exception e) {
            mode.changeTo(M.DEFAULT);
            String msg = "?????????????????????" + e.getMessage();
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
        // ???????????????????????????????????????
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
            // ?????????????????????????????????????????????????????????????????????
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
                //????????????????????????
                locationClient.setLocationListener(this);
                //??????????????????????????????
                locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                //??????????????????
                locationClient.setLocationOption(locationOption);
                locationClient.startLocation();//????????????
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
                locationListener.onLocationChanged(aMapLocation);// ?????????????????????
                double lat = aMapLocation.getLatitude();
                double lng = aMapLocation.getLongitude();
                if (myLocation == null) {
                    myLocation = new Position();
                }
                myLocation.setLat(lat);
                myLocation.setLng(lng);

            } else {
                String errText = "????????????," + aMapLocation.getErrorCode()+ ": " + aMapLocation.getErrorInfo();
                Log.e("MapLocation",errText);
            }
        }
    }
}