package com.example.campusnavigator.utility.structures;


import androidx.annotation.NonNull;

import com.example.campusnavigator.utility.interfaces.Stackable;

import java.util.Arrays;
import java.util.Iterator;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/1 13:45
 * @Version 1
 */
public class List<T> implements Iterable<T>, Stackable<T> {
    private static final int MAX_SIZE = 80;
    @SuppressWarnings("unchecked")
    private T[] array = (T[]) new Object[MAX_SIZE];
    private int size = 0;


    public List() {
    }

    public List(List<T> other) {
        // 只需要复制数组内对象的引用
        System.arraycopy(other.array, 0, this.array, 0, other.size);
        this.size = other.size;
    }

    @Override
    public void push(T value) {
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

    @Override
    public void pop() {
        if (size > 0) {
            size--;
        }
    }

    @Override
    public T top() {
        return size == 0 ? null : array[size - 1];
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

    public static <T> void reverse(List<T> l) {
        int i = 0;
        int j = l.size - 1;
        while (i < j) {
            T t = l.array[i];
            l.array[i] = l.array[j];
            l.array[j] = t;
            i++;
            j--;
        }
    }

    public static <T> void sort(List<T> list) {
        Arrays.sort(list.array, 0, list.size);
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
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
