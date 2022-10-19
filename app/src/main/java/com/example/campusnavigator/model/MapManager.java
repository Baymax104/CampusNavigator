package com.example.campusnavigator.model;

import android.content.Context;

import androidx.annotation.NonNull;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.example.campusnavigator.utility.callbacks.RouteResultReceiver;
import com.example.campusnavigator.utility.structures.List;
import com.example.campusnavigator.utility.structures.MinHeap;
import com.example.campusnavigator.utility.structures.Stack;
import com.example.campusnavigator.utility.structures.Tuple;

import java.util.Arrays;


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

    private boolean checkResult(RouteResult result) {
        return result != null &&
                result.getRoute() != null && result.getRoute().length() != 0 &&
                result.getTime() != null && !result.getTime().isInfinite() &&
                result.getDist() != null && !result.getDist().isInfinite();
    }

    public void calculateRoutePlan(Stack<Position> spotBuffer, boolean isMultiSpot, RouteResultReceiver receiver) throws Exception {
        if (isMultiSpot) {
            List<RouteResult> results = getMultiDestRoute(spotBuffer);
            for (RouteResult result : results) {
                if (!checkResult(result)) {
                    throw new Exception("多点结果错误");
                }
            }
            receiver.onMultiRouteReceive(results);
            return;
        }
        PriorityType[] types = PriorityType.values();
        List<RouteResult> results = new List<>();
        Position to = spotBuffer.top();
        spotBuffer.pop();
        Position from = spotBuffer.top();
        spotBuffer.pop();
        for (PriorityType type : types) {
            RouteResult result = getSingleDestRoute(from, to, type);
            if (!checkResult(result)) {
                throw new Exception("单点结果错误");
            }
            results.add(result);
        }
        receiver.onSingleRouteReceive(results);
    }

    public List<RouteResult> getMultiDestRoute(Stack<Position> spotBuffer) {
        List<RouteResult> results = new List<>();
        Position to = spotBuffer.top();
        spotBuffer.pop();

        while (spotBuffer.isNotEmpty()) {
            Position from = spotBuffer.top();
            spotBuffer.pop();
            RouteResult singleDestRoute = getSingleDestRoute(from, to, PriorityType.DISTANCE);
            results.add(singleDestRoute);
            to = from;
        }

        return results;
    }

    public RouteResult getSingleDestRoute(@NonNull Position from, @NonNull Position to, PriorityType type) {
        List<Position> route = new List<>();
        int[] paths = new int[size];
        Arrays.fill(paths, -1);

        int fromId = from.getId();
        int toId = to.getId();

        Tuple<Double, Double> timeAndDist = Astar(fromId, toId, type, paths);

        double time = timeAndDist.first;
        double dist = timeAndDist.second;
        // 生成的路线为逆序
        Path path = map[toId][paths[toId]];
        Position startPoint = positions[path.from];
        route.add(startPoint);
        while (paths[toId] != -1) {
            path = map[toId][paths[toId]];
            Position p = positions[path.to];
            route.add(p);
            toId = paths[toId];
        }
        return new RouteResult(route, time, dist);
    }

    public Tuple<Double, Double> Astar(int source, int dest, @NonNull PriorityType type, @NonNull int[] paths) {
        double[] cost = new double[size];
        double[] dist = new double[size];
        double[] time = new double[size];

        int v = source;
        MinHeap<Integer, Double> heap = new MinHeap<>();

        // 初始化
        Arrays.fill(cost, INF);
        Arrays.fill(dist, INF);
        Arrays.fill(time, INF);
        cost[v] = 0;
        dist[v] = 0;
        time[v] = 0;
        paths[v] = -1;
        heap.push(v, 0.0);

        while (!heap.isEmpty()) {
            v = heap.top().first();
            heap.pop();
            if (v == dest) { // 查找到目的地直接退出
                break;
            }
            visited[v] = true;
            for (int i = 0; i < size; i++) {
                if (map[v][i].dist != INF && !visited[i]) { // 若v到i有路径并且i未访问过
                    if (type == PriorityType.DISTANCE && cost[v] + map[v][i].dist < cost[i]) { // 距离最小
                        cost[i] = cost[v] + map[v][i].dist;
                        dist[i] = dist[v] + map[v][i].dist;
                        time[i] = time[v] + map[v][i].time;
                        double h = getDistanceById(i, dest); // 启发式信息为i到dest的直线距离
                        double priority = cost[i] + h; // f(i) = g(i) + h(i)
                        heap.push(i, priority);
                        paths[i] = v;
                    } else if (type == PriorityType.TIME && cost[v] + map[v][i].time < cost[i]) { // 时间最短
                        cost[i] = cost[v] + map[v][i].time;
                        dist[i] = dist[v] + map[v][i].dist;
                        time[i] = time[v] + map[v][i].time;
                        double h = getDistanceById(i, dest);
                        double priority = cost[i] + h;
                        heap.push(i, priority);
                        paths[i] = v;
                    }
                }
            }
        }
        refreshVisited();
        return new Tuple<>(time[dest], dist[dest]);
    }

    private void refreshVisited() {
        for (int i = 0; i < size; i++) {
            visited[i] = false;
        }
    }

    public List<Position> attachToMap(Position myPosition) {
        List<Position> attachPositions = new List<>();
        LatLng latLng = myPosition.getLatLng();

        // 使用最小堆取距离最小的两个点
        MinHeap<Integer, Double> minDist = new MinHeap<>();
        for (int i = 0; i < size; i++) {
            double distance = AMapUtils.calculateLineDistance(latLng, positions[i].getLatLng());
            minDist.push(i, distance);
        }
        int min1 = minDist.top().first();
        minDist.pop();
        int min2 = minDist.top().first();
        attachPositions.add(positions[min1]);
        attachPositions.add(positions[min2]);
        return attachPositions;
    }
}
