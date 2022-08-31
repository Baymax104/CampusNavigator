package com.example.campusnavigator.model;

import com.example.campusnavigator.domain.Position;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/8/31 9:47
 * @Version 1
 */
public class PositionOperator {
    private static Position[] positions;
    private static int size = 0;
    private static final int MAX_SIZE = 50;

    public PositionOperator() {
    }

    public Position[] getPositions() {
        return positions;
    }

    public void setPositions(Position[] positions) {
        PositionOperator.positions = positions;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        PositionOperator.size = size;
    }

    public boolean add(Position position) {
        if (size == MAX_SIZE) {
            return false;
        }
        positions[size] = position;
        size++;
        return true;
    }

    public Position getPosition(String name) {
        for (Position pos : positions) {
            if (pos.getName() != null && pos.getName().equals(name)) {
                return pos;
            }
        }
        return null;
    }
}
