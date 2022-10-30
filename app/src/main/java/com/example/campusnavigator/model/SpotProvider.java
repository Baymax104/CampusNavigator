package com.example.campusnavigator.model;

import androidx.annotation.NonNull;

import com.example.campusnavigator.utility.structures.List;
import com.example.campusnavigator.utility.structures.Stack;

/**
 * @Description 提供外部查找Position的方法类
 * @Author John
 * @email
 * @Date 2022/8/31 9:47
 * @Version 1
 */
public class SpotProvider extends Map {

    private final Stack<Position> spotBuffer;
    private static SpotProvider obj;

    private SpotProvider() {
        spotBuffer = new Stack<>();
    }

    public static SpotProvider getInstance() {
        if (obj == null) {
            obj = new SpotProvider();
        }
        return obj;
    }

    public List<String> allNames() {
        List<String> list = new List<>();
        for (int i = 0; i < sizeOfSpot; i++) {
            list.push(spots[i].getName());
        }
        return list;
    }

    public Position getPosition(@NonNull String name) {
        for (int i = 0; i < sizeOfSpot; i++) {
            Position spot = spots[i];
            if (spot.getName() != null && spot.getName().equals(name)) {
                return spot;
            }
        }
        return null;
    }

    public Position getPosByMarkerId(@NonNull String markerId) {
        for (int i = 0; i < sizeOfSpot; i++) {
            Position spot = spots[i];
            if (spot.getMarkerId().equals(markerId)) {
                return spot;
            }
        }
        return null;
    }

    @NonNull
    public List<Position> fuzzyQuery(@NonNull String name) {
        // 将字符串处理为正则表达式
        char[] nameArray = name.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (char c : nameArray) {
            builder.append("(.*)").append(c);
        }
        builder.append("(.*)");
        String regex = builder.toString();

        // 根据正则表达式模糊搜索
        List<Position> result = new List<>();
        for (int i = 0; i < sizeOfSpot; i++) {
            Position spot = spots[i];
            if (spot.getName().matches(regex)) {
                result.push(spot);
            }
        }
        return result;
    }

    @NonNull
    public static List<String> extractName(@NonNull List<Position> positions) {
        List<String> names = new List<>();
        for (Position p : positions) {
            names.push(p.getName());
        }
        return names;
    }


    public Stack<Position> getBuffer() {
        return spotBuffer;
    }

    public void pushBuffer(@NonNull Position position) {
        spotBuffer.push(position);
    }

    public Position bufferTop() {
        return spotBuffer.top();
    }

    public void popBuffer() {
        spotBuffer.pop();
    }

    public void popBufferAll() {
        spotBuffer.popAll();
    }
}
