package com.example.campusnavigator.model;

import android.content.Context;

import com.amap.api.maps.model.LatLng;
import com.example.campusnavigator.utility.structures.List;

/**
 * @Description 提供外部查找Position的方法类
 * @Author John
 * @email
 * @Date 2022/8/31 9:47
 * @Version 1
 */
public class PositionProvider extends Map {

    public PositionProvider(Context context) {
        super(context);
    }

    public List<String> getAllNames() {
        List<String> list = new List<>();
        for (int i = 0; i < sizeOfSpot; i++) {
            list.push(spots[i].getName());
        }
        return list;
    }

    public Position getPosByName(String name) {
        for (int i = 0; i < sizeOfSpot; i++) {
            Position pos = spots[i];
            if (pos.getName() != null && pos.getName().equals(name)) {
                return pos;
            }
        }
        return null;
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
