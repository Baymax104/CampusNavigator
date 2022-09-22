package com.example.campusnavigator.model;

import android.content.Context;

import com.amap.api.maps.model.LatLng;
import com.example.campusnavigator.utility.List;

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

    public List<String> getAllNames() {
        List<String> list = new List<>();
        for (int i = 0; i < sizeOfSpot; i++) {
            list.add(spots[i].getName());
        }
        return list;
    }

    public List<Position> getPosByName(String name) {
        List<Position> results = new List<>();
        for (int i = 0; i < sizeOfSpot; i++) {
            Position pos = spots[i];
            if (pos.getName() != null && pos.getName().equals(name)) {
                results.add(pos);
            }
        }
        return results;
    }


    public Position getPosByLatLng(LatLng latLng) {
        for (int i = 0; i < sizeOfSpot; i++) {
            Position pos = spots[i];
            if (pos.getLat() == latLng.latitude && pos.getLng() == latLng.longitude) {
                return pos;
            }
        }
        return null;
    }
}
