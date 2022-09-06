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

    public double getWeight(int from, int to) {
        return map[from][to];
    }

    public List<Position[]> DFS(Position source) {
        List<Position[]> list = new List<>();
        int v = source.getId();
        dfs(list, v);
        refreshVisited();
        return list;
    }

    private void dfs(List<Position[]> list, int v) {
        visited[v] = true;
        for (int i = 0; i < size; i++) {
            if (!visited[i] && map[v][i] != INF) {
                Position[] result = new Position[] {positions[v], positions[i]};
                list.add(result);
                dfs(list, i);
            }
        }
    }

    public List<Position[]> BFS(Position source) {
        Queue<Integer> queue = new Queue<>();
        List<Position[]> list = new List<>();
        int v = source.getId();
        visited[v] = true;
        queue.push(v);
        while (!queue.isEmpty()) {
            v = queue.front();
            queue.pop();
            for (int i = 0; i < size; i++) {
                if (!visited[i] && map[v][i] != INF) {
                    Position[] result = new Position[] {positions[v], positions[i]};
                    list.add(result);
                    visited[i] = true;
                    queue.push(i);
                }
            }
        }
        refreshVisited();
        return list;
    }

    public boolean Dijkstra(int source, double[] dist, int[] paths) {
        // dist[i]: distance of source --> i;
        if (dist == null || paths == null) {
            return false;
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
            v = heap.top().getFirst();
            heap.pop();
            if (visited[v]) {
                continue;
            }
            visited[v] = true;
            for (int i = 0; i < size; i++) {
                if (map[v][i] != INF && dist[v] + map[v][i] < dist[i]) {
                    dist[i] = dist[v] + map[v][i];
                    heap.push(i, dist[i]);
                    paths[i] = v;
                }
            }
        }
        refreshVisited();
        return true;
    }

    public double getSingleDestRoute(Position from, Position to, List<Position[]> results) {
        double[] dist = new double[size];
        int[] paths = new int[size];
        int fromId = from.getId();
        int toId = to.getId();
        Dijkstra(fromId, dist, paths);
        // 生成的路线为逆序
        while (paths[toId] != -1) {
            Position[] line = new Position[] {positions[toId], positions[paths[toId]]};
            results.add(line);
            toId = paths[toId];
        }
        return dist[toId];
    }

    public void getMultiDestRoute(Stack<Position> spots, RouteResultCallback callback) {
        List<Position[]> list = new List<>();
        List<Position[]> singleRouteResult = new List<>();
        Position to = spots.top();
        spots.pop();
        while (!spots.isEmpty()) {
            Position from = spots.top();
            spots.pop();
            double distance = getSingleDestRoute(from, to, singleRouteResult);
            for (int i = 0; i < singleRouteResult.getSize(); i++) {
                list.add(singleRouteResult.get(i));
            }
            singleRouteResult.clear();
            to = from;
        }
        callback.showMultiDestRoute(list);
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
