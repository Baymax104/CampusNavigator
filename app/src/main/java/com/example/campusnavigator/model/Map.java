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
    protected static int size;
    protected static int sizeOfSpot;
    protected static final double INF = 65535;
    protected static final int INITIAL_SPOT_SIZE = 30;
    protected static final int INITIAL_POSITION_SIZE = 100;
    protected static HashMap<Position, List<Position>> spotAttached; // 每个地点的入口点

    protected static double[][] map;

    public static final double SPEED_WALK = 4.0 * 50 / 3; // 4km/h

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
            // 获取position
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

            // 获取positions
            positions = new Position[INITIAL_POSITION_SIZE];
            JSONArray positionArray = jsonObject.getJSONArray("positions");
            size = positionArray.length();
            for (int i = 0; i < size; i++) {
                JSONObject route = positionArray.getJSONObject(i);
                double lat = route.getDouble("lat");
                double lng = route.getDouble("lng");
                positions[i] = new Position(i, lat, lng);
            }

            // 解析文件创建map
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

            // 获取crossings
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

        } catch (IOException | JSONException e) {
            Log.e("MapGenerateError", e.getMessage());
            e.printStackTrace();
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
