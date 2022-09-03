package com.example.campusnavigator.model;

import android.content.Context;

import com.amap.api.maps.AMapUtils;
import com.example.campusnavigator.utility.List;
import com.example.campusnavigator.utility.MinHeap;
import com.example.campusnavigator.utility.Queue;
import com.example.campusnavigator.utility.Stack;

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
 * @Date 2022/8/31 13:30
 * @Version 1
 */
public class MapManager {

    private double[][] mp;
    private static final double INF = 65535;
    private boolean[] visited;
    private Position[] positions;
    private int size;
    private static MapManager manager;

    private MapManager(Context context, String filename) {
        size = PositionProvider.getSize();
        // 初始化图
        mp = new double[size][size];
        visited = new boolean[size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == j) {
                    mp[i][j] = 0;
                } else {
                    mp[i][j] = INF;
                }
            }
        }
        positions = PositionProvider.getPositions();

        try(InputStreamReader streamReader = new InputStreamReader(context.getAssets().open(filename), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader)) {
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            JSONObject jsonObject = new JSONObject(builder.toString());
            JSONArray pathArray = jsonObject.getJSONArray("paths");
            for (int i = 0; i < pathArray.length(); i++) {
                JSONObject path = pathArray.getJSONObject(i);
                int from = path.getInt("from");
                int to = path.getInt("to");
                double length = AMapUtils.calculateLineDistance(positions[from].getLatLng(),positions[to].getLatLng());
                mp[from][to] = length;
                mp[to][from] = length;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public static MapManager getInstance(Context context, String filename) {
        if (manager == null) {
            manager = new MapManager(context, filename);
        }
        return manager;
    }

    public double getWeight(int from, int to) {
        return mp[from][to];
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
            if (!visited[i] && mp[v][i] != INF) {
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
                if (!visited[i] && mp[v][i] != INF) {
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

    public List<Position[]> getSingleShortPath(Position from, Position to) {
        List<Position[]> list = new List<>();
        double[] dist = new double[size];
        int[] paths = Dijkstra(from, dist);
        int toId = to.getId();
        // 生成路径为逆序，使用栈改变顺序
        Stack<Position[]> stack = new Stack<>();
        while (paths[toId] != -1) {
            stack.push(new Position[] {positions[paths[toId]], positions[toId]});
            toId = paths[toId];
        }
        while (!stack.isEmpty()) {
            list.add(stack.top());
            stack.pop();
        }
        return list;
    }

    public int[] Dijkstra(Position source, double[] dist) {
        // dist[i]: distance of source --> i;
        if (dist == null) {
            return null;
        }
        int[] paths = new int[size];
        int v = source.getId();
        MinHeap heap = new MinHeap();
        for (int i = 0; i < size; i++) {
            dist[i] = INF;
            paths[i] = -1;
        }
        dist[v] = 0;
        paths[v] = -1;
        heap.push(new MinHeap.Entry(v, 0));

        while (!heap.isEmpty()) {
            v = heap.top().v;
            heap.pop();
            if (visited[v]) {
                continue;
            }
            visited[v] = true;
            for (int i = 0; i < size; i++) {
                if (mp[v][i] != INF && dist[v] + mp[v][i] < dist[i]) {
                    dist[i] = dist[v] + mp[v][i];
                    heap.push(new MinHeap.Entry(i, dist[i]));
                    paths[i] = v;
                }
            }
        }
        refreshVisited();
        return paths;
    }

    public List<Position[]> getMultiShortPath(Stack<Position> spots) {
        List<Position[]> list = new List<>();
        Position from = spots.top();
        spots.pop();
        while (!spots.isEmpty()) {
            Position to = spots.top();
            spots.pop();
            List<Position[]> results = getSingleShortPath(from, to);
            for (int i = 0; i < results.getSize(); i++) {
                list.add(results.get(i));
            }
            from = to;
        }
        return list;
    }

    private void refreshVisited() {
        for (int i = 0; i < size; i++) {
            visited[i] = false;
        }
    }

}
