package com.example.campusnavigator.model;

import android.content.Context;

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
    protected static final int INF = 65535;
    public static HashMap<Position, List<Position>> spotAttached; // 每个地点的入口点

    public static class Path {
        public final int from;
        public final int to;
        protected double dist;
        protected double time;
        protected Path(int from, int to) {
            this.from = from;
            this.to = to;
        }
        protected Path(int from, int to, double dist, double time) {
            this.from = from;
            this.to = to;
            this.time = time;
            this.dist = dist;
        }
    }
    protected static Path[][] map;

    protected static final double SPEED_WALK = 5; // 4km/h
    protected static final double SPEED_CYCLING = 18; // 18km/h
    protected static final double SPEED_DRIVE = 20; // 20km/h
    public enum PriorityType {
        TIME,
        DISTANCE
    }

    protected Map(Context context) {
        if (positions == null && map == null) {
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
                spots = new Position[20];
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
                positions = new Position[80];
                JSONArray positionArray = jsonObject.getJSONArray("positions");
                size = positionArray.length();
                for (int i = 0; i < size; i++) {
                    JSONObject route = positionArray.getJSONObject(i);
                    double lat = route.getDouble("lat");
                    double lng = route.getDouble("lng");
                    positions[i] = new Position(i, lat, lng);
                }

                // 解析文件创建map
                map = new Path[size][size];
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        if (i == j) {
                            map[i][j] = new Path(i, j);
                        } else {
                            map[i][j] = new Path(i, j, INF, INF);
                        }
                    }
                }

                // 获取paths
                JSONArray pathArray = jsonObject.getJSONArray("paths");
                for (int j = 0; j < pathArray.length(); j++) {
                    JSONObject path = pathArray.getJSONObject(j);
                    int from = path.getInt("from") - 1;
                    int to = path.getInt("to") - 1;
                    double distance = AMapUtils.calculateLineDistance(positions[from].getLatLng(),positions[to].getLatLng());
                    map[from][to].dist = distance;
                    map[to][from].dist = distance;

                    int randomFactor = (int) (Math.random() * 5 - 2); // uniform(-2,2)
                    // 初始为步行
                    double speed = (SPEED_WALK + randomFactor) * 50 / 3; // km/h -> m/min
                    double time = distance / speed;
                    map[from][to].time = time;
                    map[to][from].time = time;
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
                e.printStackTrace();
            }
        }
    }

    protected static double getDistanceById(int p1, int p2) {
        LatLng l1 = positions[p1].getLatLng();
        LatLng l2 = positions[p2].getLatLng();
        return AMapUtils.calculateLineDistance(l1, l2);
    }
}
