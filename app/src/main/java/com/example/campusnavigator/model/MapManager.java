package com.example.campusnavigator.model;

import android.content.Context;

import com.example.campusnavigator.utility.List;
import com.example.campusnavigator.utility.Queue;

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
                mp[i][j] = INF;
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
                mp[from][to] = 1;
                mp[to][from] = 1;
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

    private void refreshVisited() {
        for (int i = 0; i < size; i++) {
            visited[i] = false;
        }
    }
}
