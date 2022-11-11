package com.example.campusnavigator.model;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.example.campusnavigator.utility.structures.HashMap;
import com.example.campusnavigator.utility.structures.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/4 16:08
 * @Version 1
 */
public class Map {
    protected static Position[] spots; // 地点
    protected static Position[] positions; // 路口采点
    protected static HashMap<Position, List<Position>> spotAttached; // 每个地点的入口点
    protected static double[][] map; // 地图邻接矩阵
    protected static int size;
    protected static int sizeOfSpot;
    protected static final double INF = 65535;
    protected static final int INITIAL_SPOT_SIZE = 30;
    protected static final int INITIAL_POSITION_SIZE = 100;

    // 对于通行道路的区别，采用通行权限进行区分
    // 权限高的可以走权限低的道路，权限低的不能走权限高的路
    public static final int DRIVE_PASS = 1;
    public static final int FOOT_PASS = 2;

    public static final double SPEED_WALK = 4.0 * 50 / 3; // 4km/h
    public static final double SPEED_DRIVE = 12.0 * 50 / 3; // 12km/h

    protected Map() {
    }

    public static void bind(Context context) {
        // 读取json文件并解析，获取Position数据
        try(InputStreamReader streamReader = new InputStreamReader(context.getAssets().open("map_data.json"), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader)) {
            // 读取文件
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            // 解析文件
            JSONObject jsonObject = new JSONObject(builder.toString());

            // 获取spot
            generateSpot(jsonObject);

            // 获取positions
            generatePosition(jsonObject);

            // 解析文件创建map
            generateMap(jsonObject);

            // 获取crossings
            generateCrossing(jsonObject);

            // 设置spot类型
            setBuildingType(jsonObject);

            // 设置通行权限
            setPositionPass(jsonObject);

        } catch (IOException | JSONException e) {
            Log.e("MapGenerateError", e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateSpot(@NonNull JSONObject jsonObject) throws JSONException {
        spots = new Position[INITIAL_SPOT_SIZE];
        JSONArray spotArray = jsonObject.getJSONArray("spots");
        sizeOfSpot = spotArray.length();
        for (int i = 0; i < sizeOfSpot; i++) {
            JSONObject pos = spotArray.getJSONObject(i);
            double lat = pos.getDouble("lat");
            double lng = pos.getDouble("lng");
            String name = pos.getString("name");
            spots[i] = new Position(i, lat, lng, name);
        }
    }

    private static void generatePosition(@NonNull JSONObject jsonObject) throws JSONException {
        positions = new Position[INITIAL_POSITION_SIZE];
        JSONArray positionArray = jsonObject.getJSONArray("positions");
        size = positionArray.length();
        for (int i = 0; i < size; i++) {
            JSONObject route = positionArray.getJSONObject(i);
            double lat = route.getDouble("lat");
            double lng = route.getDouble("lng");
            positions[i] = new Position(i, lat, lng);
        }
    }

    private static void generateMap(JSONObject jsonObject) throws JSONException {
        map = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == j) {
                    map[i][j] = 0;
                } else {
                    map[i][j] = INF;
                }
            }
        }
        // 获取paths
        JSONArray pathArray = jsonObject.getJSONArray("paths");
        for (int j = 0; j < pathArray.length(); j++) {
            JSONObject path = pathArray.getJSONObject(j);
            int from = path.getInt("from") - 1;
            int to = path.getInt("to") - 1;
            double distance = getDistance(from, to);
            map[from][to] = distance;
            map[to][from] = distance;
        }
    }

    private static void generateCrossing(@NonNull JSONObject jsonObject) throws JSONException {
        spotAttached = new HashMap<>();
        JSONArray crossingArray = jsonObject.getJSONArray("crossings");
        for (int i = 0; i < crossingArray.length(); i++) {
            JSONObject crossing = crossingArray.getJSONObject(i);
            int from = crossing.getInt("from");
            JSONArray attachArray = crossing.getJSONArray("to");

            Position spot = spots[from];
            List<Position> attachList = new List<>();
            for (int j = 0; j < attachArray.length(); j++) {
                int positionIndex = attachArray.getInt(j) - 1;
                attachList.push(positions[positionIndex]);
            }
            spotAttached.put(spot, attachList);
        }
    }

    private static void setBuildingType(JSONObject jsonObject) throws JSONException {
        Class<?> cl = BuildingType.class;
        Object[] instances = cl.getEnumConstants();
        if (instances != null) {
            JSONObject spotType = jsonObject.getJSONObject("spotType");
            for (Object instance : instances) {
                BuildingType type = (BuildingType) instance;
                JSONArray posArray = spotType.getJSONArray(type.name());
                for (int i = 0; i < posArray.length(); i++) {
                    int pos = posArray.getInt(i);
                    spots[pos].setType(type);
                }
            }
        }
    }

    private static void setPositionPass(JSONObject jsonObject) throws JSONException {
        // 默认为机动车道
        for (int i = 0; i < size; i++) {
            positions[i].setPass(DRIVE_PASS);
        }
        // 设置人行道
        JSONObject attr = jsonObject.getJSONObject("attr");
        JSONArray footwayArray = attr.getJSONArray("onlyFootway");
        for (int i = 0; i < footwayArray.length(); i++) {
            int index = footwayArray.getInt(i) - 1;
            positions[index].setPass(FOOT_PASS);
        }
    }

    protected static double getDistance(int p1, int p2) {
        LatLng l1 = positions[p1].getLatLng();
        LatLng l2 = positions[p2].getLatLng();
        return AMapUtils.calculateLineDistance(l1, l2);
    }

    protected static double getDistance(@NonNull Position p1, @NonNull Position p2) {
        LatLng l1 = p1.getLatLng();
        LatLng l2 = p2.getLatLng();
        return AMapUtils.calculateLineDistance(l1, l2);
    }
}
