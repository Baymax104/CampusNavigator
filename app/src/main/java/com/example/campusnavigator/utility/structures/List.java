package com.example.campusnavigator.utility.structures;


import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.RandomAccess;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/1 13:45
 * @Version 1
 */
public class List<T> implements Iterable<T>, RandomAccess {
    private static final int MAX_SIZE = 80;
    @SuppressWarnings("unchecked")
    private T[] array = (T[]) new Object[MAX_SIZE];
    private int size = 0;

    public List() {
    }

    public List(List<T> other) {
        // 只需要复制数组内对象的引用
        System.arraycopy(other.array, 0, this.array, 0, other.size);
        size = other.size;
    }

    public void add(T value) {
        if (size == MAX_SIZE) {
            grow(); // 当存满时进行扩容
        }
        array[size] = value;
        size++;
    }

    public T get(int i) {
        if (i < 0 || i >= MAX_SIZE) {
            return null;
        }
        return array[i];
    }

    public void popBack() {
        if (size > 0) {
            size--;
        }
    }

    public int length() {
        return size;
    }

    public void clear() {
        size = 0;
    }

    private void grow() {
        int newCapacity = size + (size >> 1); // 扩容为1.5倍
        array = Arrays.copyOf(array, newCapacity);
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            int cur = 0;
            @Override
            public boolean hasNext() {
                return cur < size;
            }

            @Override
            public T next() {
                return array[cur++];
            }
        };
    }
}
