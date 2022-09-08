package com.example.campusnavigator.model;

import android.content.Context;

import com.amap.api.maps.AMapUtils;

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
    protected static Position[] positions;
    protected static int size;
    protected static int sizeOfSpot;
    protected static final int INF = 65535;
    public static class Path {
        protected int from;
        protected int to;
        protected double dist;
        protected double eval;
        protected Path(int from, int to) {
            this.from = from;
            this.to = to;
        }
        protected Path(int from, int to, double dist, double eval) {
            this.from = from;
            this.to = to;
            this.dist = dist;
            this.eval = eval;
        }
        public int from() {
            return from;
        }
        public int to() {
            return to;
        }
        public double getDist() {
            return dist;
        }
        public double getEval() {
            return eval;
        }
    }
    protected static Path[][] map;

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
                positions = new Position[80];
                // 解析文件
                JSONObject jsonObject = new JSONObject(builder.toString());
                // 获取position
                JSONArray positionArray = jsonObject.getJSONArray("positions");
                int i;
                for (i = 0; i < positionArray.length(); i++) {
                    JSONObject pos = positionArray.getJSONObject(i);
                    double lat = pos.getDouble("lat");
                    double lng = pos.getDouble("lng");
                    String name = pos.getString("name");
                    positions[i] = new Position(i, lat, lng, name);
                }
                sizeOfSpot = i; // 记录position数量，用于map赋值转换

                // 获取routes
                JSONArray routeArray = jsonObject.getJSONArray("routes");
                for (int j = 0; j < routeArray.length(); j++) {
                    JSONObject route = routeArray.getJSONObject(j);
                    double lat = route.getDouble("lat");
                    double lng = route.getDouble("lng");
                    positions[i] = new Position(i, lat, lng);
                    i++;
                }
                size = i; // 记录地点总数量

                // 解析文件创建map
                map = new Path[size][size];
                for (i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        if (i == j) {
                            map[i][j] = new Path(i, j);
                        } else {
                            map[i][j] = new Path(i, j, INF, 0);
                        }
                    }
                }
                // 获取crossings
                JSONArray crossingArray = jsonObject.getJSONArray("crossings");
                for (int j = 0; j < crossingArray.length(); j++) {
                    JSONObject crossing = crossingArray.getJSONObject(j);
                    int from = crossing.getInt("from");
                    int to = crossing.getInt("to") + sizeOfSpot - 1; // 将to转换为实际数组下标
                    double distance = AMapUtils.calculateLineDistance(positions[from].getLatLng(), positions[to].getLatLng());
                    map[from][to].dist = distance;
                    map[to][from].dist = distance;
                }

                // 获取paths
                JSONArray pathArray = jsonObject.getJSONArray("paths");
                for (int j = 0; j < pathArray.length(); j++) {
                    JSONObject path = pathArray.getJSONObject(j);
                    int from = path.getInt("from") + sizeOfSpot - 1;
                    int to = path.getInt("to") + sizeOfSpot - 1;
                    double distance = AMapUtils.calculateLineDistance(positions[from].getLatLng(),positions[to].getLatLng());
                    map[from][to].dist = distance;
                    map[to][from].dist = distance;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
