package com.example.campusnavigator.utility;


/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/1 19:13
 * @Version 1
 */
public class MinHeap {
    private static final int MAX_SIZE = 80;
    public static class Entry {
        public int v;
        public double weight;
        public Entry(int v, double weight) {
            this.v = v;
            this.weight = weight;
        }
    }
    private Entry[] array = new Entry[MAX_SIZE];
    private int size;

    public MinHeap() {
    }

    public MinHeap(Entry[] entries) {
        int i;
        for (i = 0; i < entries.length; i++) {
            array[i] = entries[i];
            shiftUp(i);
        }
        size = i;
    }

    public boolean push(Entry entry) {
        if (size == MAX_SIZE) {
            return false;
        }
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
            if (child + 1 < size && array[child].weight > array[child + 1].weight) {
                child++;
            }
            if (array[index].weight <= array[child].weight) {
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
            if (array[parent].weight <= array[index].weight) {
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
