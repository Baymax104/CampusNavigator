package com.example.campusnavigator.utility.structures;


import com.example.campusnavigator.utility.interfaces.Stackable;

/**
 * @Description 栈的双向链表实现类
 * @Author John
 * @email
 * @Date 2022/9/1 14:15
 * @Version 1
 */
public class Stack<E> implements Stackable<E> {
    private static class Node<T> {
        private T value;
        private Node<T> pre;
        private Node<T> next;
        private Node() {
        }
        private Node(T value) {
            this.value = value;
        }
    }

    private Node<E> head = new Node<>();
    private Node<E> tail = head;
    private int size = 0;

    public Stack() {
    }

    @Override
    public void push(E value) {
        Node<E> node = new Node<>(value);
        tail.next = node;
        node.pre = tail;
        tail = tail.next;
        size++;
    }

    @Override
    public void pop() {
        if (size == 0) {
            return;
        }
        tail = tail.pre;
        size--;
    }

    @Override
    public E top() {
        if (size == 0) {
            return null;
        }
        return tail.value;
    }

    public void popAll() {
        tail = head;
        size = 0;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public List<E> toList(boolean reverse) {
        List<E> list = new List<>();
        Node<E> cur = reverse ? head.next : tail;
        for (int l = size; l > 0; l--) {
            list.push(cur.value);
            cur = reverse ? cur.next : cur.pre;
        }
        return list;
    }
}
