package com.example.campusnavigator.model;

import com.example.campusnavigator.domain.Position;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/8/31 11:43
 * @Version 1
 */
public class QueueCN {
    private Position[] array = new Position[50];
    private int size = 50;
    private int front = 0;
    private int rear = 0;

    public QueueCN() {
    }

    public QueueCN(Position[] positions) {
        for (int i = 0; i < positions.length; i++) {
            array[i] = positions[i];
        }
        rear = positions.length - 1;
    }

    public boolean push(Position position) {
        if (isFull()) {
            return true;
        }
        array[rear] = position;
        rear = (rear + 1) % size;
        return true;
    }

    public boolean pop() {
        if (isEmpty()) {
            return false;
        }
        rear = (rear != 0) ? rear - 1 : size - 1;
        return true;
    }

    public Position top() {
        return isEmpty() ? null : array[front];
    }

    public boolean isEmpty() {
        return rear == front;
    }

    public boolean isFull() {
        return (rear + 1) % size == front;
    }
}
