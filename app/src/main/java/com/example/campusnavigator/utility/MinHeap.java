package com.example.campusnavigator.utility;


import java.lang.reflect.Array;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/1 19:13
 * @Version 1
 */
public class MinHeap<T1, T2 extends Comparable<T2>> {
    private static final int MAX_SIZE = 80;
    public class Entry {
        private T1 v;
        private T2 priority;
        public Entry(T1 v, T2 priority) {
            this.v = v;
            this.priority = priority;
        }
        public T1 first() {
            return v;
        }
        public T2 second() {
            return priority;
        }
    }
    private int size;
    private Entry[] array;

    @SuppressWarnings("unchecked")
    public MinHeap() {
        array = (Entry[]) Array.newInstance(Entry.class, MAX_SIZE);
    }

    @SuppressWarnings("unchecked")
    public MinHeap(Entry[] entries) {
        array = (Entry[]) Array.newInstance(Entry.class, MAX_SIZE);
        int i;
        for (i = 0; i < entries.length; i++) {
            array[i] = entries[i];
            shiftUp(i);
        }
        size = i;
    }

    public boolean push(T1 t1, T2 t2) {
        if (size == MAX_SIZE) {
            return false;
        }
        Entry entry = new Entry(t1, t2);
        array[size] = entry;
        size++;
        shiftUp(size - 1);
        return true;
    }

    public boolean pop() {
        if (size == 0) {
            return false;
        }
        array[0] = array[size - 1];
        size--;
        shiftDown();
        return true;
    }

    public Entry top() {
        return array[0];
    }

    private void shiftDown() {
        int index = 0;
        int child = 1;
        while (child < size) {
            if (child + 1 < size && array[child].priority.compareTo(array[child + 1].priority) > 0) {
                child++;
            }
            if (array[index].priority.compareTo(array[child].priority) <= 0) {
                break;
            }
            Entry temp = array[index];
            array[index] = array[child];
            array[child] = temp;
            index = child;
            child = 2 * index + 1;
        }
    }

    private void shiftUp(int index) {
        int parent = (index - 1) / 2;
        while (parent >= 0) {
            if (array[parent].priority.compareTo(array[index].priority) <= 0) {
                break;
            }
            Entry temp = array[parent];
            array[parent] = array[index];
            array[index] = temp;
            index = parent;
            parent = (index - 1) / 2;
        }
    }

    public boolean isEmpty() {
        return size == 0;
    }
}
