package com.example.campusnavigator.utility.structures;


/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/1 19:13
 * @Version 1
 */
public class MinHeap<E extends Comparable<E>> {
    private static final int MAX_SIZE = 80;
    private int size;
    private final E[] array;

    @SuppressWarnings("unchecked")
    public MinHeap() {
        array = (E[]) new Comparable[MAX_SIZE];
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean push(E e) {
        if (size == MAX_SIZE) {
            return false;
        }
        array[size] = e;
        size++;
        shiftUp(size - 1);
        return true;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean pop() {
        if (size == 0) {
            return false;
        }
        array[0] = array[size - 1];
        size--;
        shiftDown();
        return true;
    }

    public E top() {
        return array[0];
    }

    private void shiftDown() {
        int index = 0;
        int child = 1;
        while (child < size) {
            if (child + 1 < size && array[child].compareTo(array[child + 1]) > 0) {
                child++;
            }
            if (array[index].compareTo(array[child]) <= 0) {
                break;
            }
            E temp = array[index];
            array[index] = array[child];
            array[child] = temp;
            index = child;
            child = 2 * index + 1;
        }
    }

    private void shiftUp(int index) {
        int parent = (index - 1) / 2;
        while (parent >= 0) {
            if (array[parent].compareTo(array[index]) <= 0) {
                break;
            }
            E temp = array[parent];
            array[parent] = array[index];
            array[index] = temp;
            index = parent;
            parent = (index - 1) / 2;
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isEmpty() {
        return size == 0;
    }
}
