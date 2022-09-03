package com.example.campusnavigator.utility;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/8/31 11:43
 * @Version 1
 */
public class Queue<T> {
    private static final int MAX_SIZE = 80;
    @SuppressWarnings("unchecked")
    private T[] array = (T[])new Object[MAX_SIZE];
    private int front = 0;
    private int rear = 0;

    public Queue() {
    }

    public Queue(T[] obj) {
        int i;
        for (i = 0; i < obj.length; i++) {
            array[i] = obj[i];
        }
        rear = i;
    }

    public boolean push(T obj) {
        if (isFull()) {
            return false;
        }
        array[rear] = obj;
        rear = (rear + 1) % MAX_SIZE;
        return true;
    }

    public boolean pop() {
        if (isEmpty()) {
            return false;
        }
        front = (front + 1) % MAX_SIZE;
        return true;
    }

    public T front() {
        return isEmpty() ? null : array[front];
    }

    public boolean isEmpty() {
        return rear == front;
    }

    public boolean isFull() {
        return (rear + 1) % MAX_SIZE == front;
    }

    public int getSize() {
        return (rear - front + MAX_SIZE) % MAX_SIZE;
    }
}
