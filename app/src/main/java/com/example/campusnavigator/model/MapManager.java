package com.example.campusnavigator.model;

import android.content.Context;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.example.campusnavigator.controller.RouteResultCallback;
import com.example.campusnavigator.utility.List;
import com.example.campusnavigator.utility.MinHeap;
import com.example.campusnavigator.utility.Queue;
import com.example.campusnavigator.utility.Stack;


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

    public void Dijkstra(int source, int dest, double[] dist, int[] paths) {
        // dist[i]: distance of source --> i;
        if (dist == null || paths == null) {
            return;
        }
        int v = source;
        MinHeap<Integer, Double> heap = new MinHeap<>();
        for (int i = 0; i < size; i++) {
            dist[i] = INF;
            paths[i] = -1;
        }
        dist[v] = 0;
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
                if (map[v][i].dist != INF && dist[v] + map[v][i].dist < dist[i]) {
                    dist[i] = dist[v] + map[v][i].dist;
                    heap.push(i, dist[i]);
                    paths[i] = v;
                }
            }
        }
        refreshVisited();
    }


    public double getSingleDestRoute(Position from, Position to, List<Path> results) {
        double[] dist = new double[size];
        int[] paths = new int[size];
        int fromId = from.getId();
        int toId = to.getId();
        Astar(fromId, toId, dist, paths);
//        Dijkstra(fromId, toId, dist, paths);
        // 生成的路线为逆序
        while (paths[toId] != -1) {
            Path path = map[toId][paths[toId]];
            results.add(path);
            toId = paths[toId];
        }
        return dist[toId];
    }

    public void getMultiDestRoute(Stack<Position> spots, RouteResultCallback callback) {
        List<Position[]> list = new List<>();
        List<Path> singleRouteResult = new List<>();
        Position to = spots.top();
        spots.pop();
        while (!spots.isEmpty()) {
            Position from = spots.top();
            spots.pop();
            double distance = getSingleDestRoute(from, to, singleRouteResult);
            for (Path path : singleRouteResult) {
                Position p1 = positions[path.from];
                Position p2 = positions[path.to];
                list.add(new Position[]{p1, p2});
            }
            singleRouteResult.clear();
            to = from;
        }
        callback.showMultiDestRoute(list);
    }

    public void Astar(int source, int dest, double[] dist, int[] paths) {
        // dist[i]: distance of source --> i;
        if (dist == null || paths == null) {
            return;
        }
        int v = source;
        MinHeap<Integer, Double> heap = new MinHeap<>();
        for (int i = 0; i < size; i++) {
            dist[i] = INF;
            paths[i] = -1;
        }
        dist[v] = 0;
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
                if (map[v][i].dist != INF && dist[v] + map[v][i].dist < dist[i]) {
                    dist[i] = dist[v] + map[v][i].dist;
                    double priority = dist[v] + map[v][i].dist + map[v][i].eval;
                    heap.push(i, priority);
                    paths[i] = v;
                }
            }
        }
        refreshVisited();
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
