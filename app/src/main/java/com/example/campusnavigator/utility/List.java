package com.example.campusnavigator.utility;


/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/1 13:45
 * @Version 1
 */
public class List<T> {
    private static final int MAX_SIZE = 80;
    @SuppressWarnings("unchecked")
    private T[] array = (T[]) new Object[MAX_SIZE];
    private int size = 0;

    public List() {
    }

    public List(T[] obj) {
        int i;
        for (i = 0; i < obj.length; i++) {
            array[i] = obj[i];
        }
        size = i - 1;
    }

    public boolean add(T obj) {
        if (size == MAX_SIZE) {
            return false;
        }
        array[size] = obj;
        size++;
        return true;
    }

    public T get(int i) {
        if (i < 0 || i >= MAX_SIZE) {
            return null;
        }
        return array[i];
    }

    public int getSize() {
        return size;
    }
}
