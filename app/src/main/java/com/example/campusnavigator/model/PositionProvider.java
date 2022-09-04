package com.example.campusnavigator.model;

import android.content.Context;

import com.amap.api.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @Description 提供外部查找Position的方法类
 * @Author John
 * @email
 * @Date 2022/8/31 9:47
 * @Version 1
 */
public class PositionProvider extends Map {
    private static PositionProvider provider;

    private PositionProvider(Context context) {
        super(context);
    }

    public static PositionProvider getInstance(Context context) {
        if (provider == null) {
            provider = new PositionProvider(context);
        }
        return provider;
    }

    public static Position[] getPositions() {
        return positions;
    }
    public static int getSize() {
        return size;
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

    public Position getPosByLatLng(LatLng latLng) {
        for (Position pos : positions) {
            if (pos.getLat() == latLng.latitude && pos.getLng() == latLng.longitude) {
                return pos;
            }
        }
        return null;
    }
}
