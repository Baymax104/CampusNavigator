package com.example.campusnavigator.model;

import android.content.Context;

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
    private static double[][] mp;
    private static MapManager manager;

    private MapManager(Context context, String filename) {
        // 初始化图
        mp = new double[100][100];
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                mp[i][j] = -1;
            }
        }

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
}
