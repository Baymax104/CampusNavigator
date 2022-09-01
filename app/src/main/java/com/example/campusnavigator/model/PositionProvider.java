package com.example.campusnavigator.model;

import android.content.Context;

import com.example.campusnavigator.domain.Position;

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
 * @Date 2022/8/31 9:47
 * @Version 1
 */
public class PositionProvider {
    private static Position[] positions;
    private static PositionProvider provider;

    private PositionProvider(Context context, String filename) {
        // 读取json文件并解析
        try(InputStreamReader streamReader = new InputStreamReader(context.getAssets().open(filename), StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(streamReader)) {
            // 读取文件
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            positions = new Position[100];
            // 解析文件
            JSONObject jsonObject = new JSONObject(builder.toString());
            JSONArray positionArray = jsonObject.getJSONArray("positions");
            for (int i = 0; i < positionArray.length(); i++) {
                JSONObject pos = positionArray.getJSONObject(i);
                double lat = pos.getDouble("lat");
                double lng = pos.getDouble("lng");
                String name = pos.getString("name");
                positions[i] = new Position(i, lat, lng, name);
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public static PositionProvider getInstance(Context context, String filename) {
        if (provider == null) {
            provider = new PositionProvider(context, filename);
        }
        return provider;
    }

    public Position[] getPositions() {
        return positions;
    }

    public Position getPosByName(String name) {
        for (Position pos : positions) {
            if (pos.getName() != null && pos.getName().equals(name)) {
                return pos;
            }
        }
        return null;
    }

    public Position getPosById(int id) {
        return positions[id];
    }
}
