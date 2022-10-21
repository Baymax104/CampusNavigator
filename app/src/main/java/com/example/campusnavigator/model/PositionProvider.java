package com.example.campusnavigator.model;

import com.amap.api.maps.model.LatLng;
import com.example.campusnavigator.utility.structures.List;
import com.example.campusnavigator.utility.structures.Stack;

/**
 * @Description 提供外部查找Position的方法类
 * @Author John
 * @email
 * @Date 2022/8/31 9:47
 * @Version 1
 */
public class PositionProvider extends Map {

    private Stack<Position> spotBuffer;
    private static PositionProvider obj;

    private PositionProvider() {
        spotBuffer = new Stack<>();
    }

    public static PositionProvider getInstance() {
        if (obj == null) {
            obj = new PositionProvider();
        }
        return obj;
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

    public Stack<Position> getBuffer() {
        return spotBuffer;
    }

    public void pushBuffer(Position position) {
        if (spotBuffer != null) {
            spotBuffer.push(position);
        }
    }

    public void popBuffer() {
        if (spotBuffer != null) {
            spotBuffer.pop();
        }
    }

    public void popBufferAll() {
        if (spotBuffer != null) {
            spotBuffer.popAll();
        }
    }
}
