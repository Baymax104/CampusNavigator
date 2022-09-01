package com.example.campusnavigator.utility;


/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/1 14:15
 * @Version 1
 */
public class Stack<T> {
    private static final int MAX_SIZE = 80;
    @SuppressWarnings("unchecked")
    private T[] array = (T[]) new Object[MAX_SIZE];
    private int size = 0;

    public Stack() {
    }

    public Stack(T[] obj) {
        int i;
        for (i = 0; i < obj.length; i++) {
            array[i] = obj[i];
        }
        size = i - 1;
    }

    public boolean push(T obj) {
        if (size == MAX_SIZE) {
            return false;
        }
        array[size] = obj;
        size++;
        return true;
    }

    public boolean pop() {
        if (size == 0) {
            return false;
        }
        size--;
        return true;
    }

    public T top() {
        if (size == 0) {
            return null;
        }
        return array[size - 1];
    }

    public boolean isEmpty() {
        return size == 0;
    }
}
