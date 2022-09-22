package com.example.campusnavigator.model;

import android.content.Context;

import androidx.annotation.NonNull;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.example.campusnavigator.controller.RouteResultCallback;
import com.example.campusnavigator.utility.List;
import com.example.campusnavigator.utility.MinHeap;
import com.example.campusnavigator.utility.Stack;
import com.example.campusnavigator.utility.Tuple;


/**
 * @Description 路线规划算法的实现类
 * @Author John
 * @email
 * @Date 2022/8/31 13:30
 * @Version 1
 */
public class MapManager extends Map {

    private boolean[] visited;
    private static MapManager manager;

    private MapManager(Context context) {
        super(context);
        visited = new boolean[size + 1];
    }

    public static MapManager getInstance(Context context) {
        if (manager == null) {
            manager = new MapManager(context);
        }
        return manager;
    }

    public void getRoutePlan(Stack<Position> spots, boolean isMultiSpot, RouteResultCallback callback) {
        try {
            if (spots.getSize() < 2) {
                throw new Exception("地点数不足");
            }
            List<Double> distances = new List<>();
            List<Double> times = new List<>();
            List<List<Tuple<Position, Position>>> routeList = new List<>();
            PriorityType[] types = PriorityType.values();
            List<Position> spotsList = new List<>();
            while (!spots.isEmpty()) {
                Position pos = spots.top();
                spots.pop();
                spotsList.add(pos);
            }
            List<Tuple<Position, Position>> routeTemp = new List<>();
            if (!isMultiSpot) {
                for (int i = 0; i < 3; i++) {
                    PriorityType type = types[i];
                    Tuple<Double, Double> distAndTime = getMultiDestRoute(spotsList, type, routeTemp);
                    double distance = distAndTime.first;
                    double time = distAndTime.second;
                    List<Tuple<Position, Position>> routes = new List<>(routeTemp);
                    distances.add(distance);
                    times.add(time);
                    routeList.add(routes);
                    routeTemp.clear();
                    callback.onSuccess(routeList, distances, times, false);
                }
            } else {
                PriorityType type = PriorityType.TOTAL;
                Tuple<Double, Double> distAndTime = getMultiDestRoute(spotsList, type, routeTemp);
                double distance = distAndTime.first;
                double time = distAndTime.second;
                List<Tuple<Position, Position>> routes = new List<>(routeTemp);
                distances.add(distance);
                times.add(time);
                routeList.add(routes);
                routeTemp.clear();
                callback.onSuccess(routeList, distances, times, true);
            }
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    public Tuple<Double, Double> getMultiDestRoute(List<Position> spotsList, PriorityType type, @ NonNull List<Tuple<Position, Position>> routes) {
        List<Path> singleRouteResult = new List<>();
        List<Double> distList = new List<>();
        List<Double> timeList = new List<>();
        int toIndex = 0;
        int fromIndex = 1;

        while (fromIndex < spotsList.length()) {
            Position to = spotsList.get(toIndex);
            Position from = spotsList.get(fromIndex);
            Tuple<Double, Double> distAndTime = getSingleDestRoute(from, to, type, singleRouteResult);
            distList.add(distAndTime.first);
            timeList.add(distAndTime.second);
            for (Path path : singleRouteResult) {
                Position p1 = positions[path.from];
                Position p2 = positions[path.to];
                routes.add(new Tuple<>(p1, p2));
            }
            singleRouteResult.clear();
            toIndex = fromIndex;
            fromIndex++;
        }

        double totalDistance = 0;
        double totalTime = 0;
        for (double dist : distList) {
            totalDistance += dist;
        }
        for (double t : timeList) {
            totalTime += t;
        }
        return new Tuple<>(totalDistance, totalTime);
    }

    public Tuple<Double, Double> getSingleDestRoute(@NonNull Position from, @NonNull Position to, PriorityType type, List<Path> results) {
        int[] paths = new int[size];
        int fromId = from.getId();
        int toId = to.getId();
        Tuple<double[], double[]> distAndTime = Astar(fromId, toId, type, paths);
        double[] distList = distAndTime.first;
        double[] timeList = distAndTime.second;
        double dist = distList[toId];
        double time = timeList[toId];
        // 生成的路线为逆序
        while (paths[toId] != -1) {
            Path path = map[toId][paths[toId]];
            results.add(path);
            toId = paths[toId];
        }
        return new Tuple<>(dist, time);
    }

    public Tuple<double[], double[]> Astar(int source, int dest, @NonNull PriorityType type, @NonNull int[] paths) {
        double[] cost = new double[size];
        double[] dist = new double[size];
        double[] time = new double[size];

        int v = source;
        MinHeap<Integer, Double> heap = new MinHeap<>();
        for (int i = 0; i < size; i++) {
            cost[i] = INF;
            dist[i] = INF;
            time[i] = INF;
            paths[i] = -1;
        }
        cost[v] = 0;
        dist[v] = 0;
        time[v] = 0;
        paths[v] = -1;
        heap.push(v, 0.0);

        while (!heap.isEmpty()) {
            v = heap.top().first();
            heap.pop();
            if (v == dest) {
                break;
            }
            if (visited[v]) {
                continue;
            }
            visited[v] = true;
            for (int i = 0; i < size; i++) {
                if (map[v][i].dist != INF) {
                    if (type == PriorityType.DISTANCE && cost[v] + map[v][i].dist < cost[i]) { // 距离最小
                        cost[i] = cost[v] + map[v][i].dist;
                        dist[i] = dist[v] + map[v][i].dist;
                        time[i] = time[v] + map[v][i].time;
                        double priority = cost[i] + map[v][i].eval;
                        heap.push(i, priority);
                        paths[i] = v;
                    } else if (type == PriorityType.TIME && cost[v] + map[v][i].time < cost[i]) { // 时间最短
                        cost[i] = cost[v] + map[v][i].time;
                        dist[i] = dist[v] + map[v][i].dist;
                        time[i] = time[v] + map[v][i].time;
                        double priority = cost[i] + map[v][i].eval;
                        heap.push(i, priority);
                        paths[i] = v;
                    } else if (type == PriorityType.TOTAL && cost[v] + map[v][i].dist + map[v][i].time < cost[i]) { // 综合最优
                        cost[i] = cost[v] + map[v][i].dist + map[v][i].time;
                        dist[i] = dist[v] + map[v][i].dist;
                        time[i] = time[v] + map[v][i].time;
                        double priority = cost[i] + map[v][i].eval;
                        heap.push(i, priority);
                        paths[i] = v;
                    }
                }
            }
        }
        refreshVisited();
        return new Tuple<>(dist, time);
    }

    private void refreshVisited() {
        for (int i = 0; i < size; i++) {
            visited[i] = false;
        }
    }

    public Position attachToMap(Position myPosition) {
        LatLng latLng = myPosition.getLatLng();
        Position attachPosition = null;
        double minDistance = INF;
        for (int i = 0; i < size; i++) {
            double distance = AMapUtils.calculateLineDistance(latLng, positions[i].getLatLng());
            if (distance < minDistance) {
                minDistance = distance;
                attachPosition = positions[i];
            }
        }
        return attachPosition;
    }
}
