package com.example.campusnavigator.utility.structures;


/**
 * @Description 栈的双向链表实现类
 * @Author John
 * @email
 * @Date 2022/9/1 14:15
 * @Version 1
 */
public class Stack<T> {
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

    private Node<T> head = new Node<>();
    private Node<T> tail = head;
    private int size = 0;

    public Stack() {
    }

    public void push(T value) {
        Node<T> node = new Node<>(value);
        tail.next = node;
        node.pre = tail;
        tail = tail.next;
        size++;
    }

    public boolean pop() {
        if (size == 0) {
            return false;
        }
        tail = tail.pre;
        size--;
        return true;
    }

    public T top() {
        if (size == 0) {
            return null;
        }
        return tail.value;
    }

    public void popAll() {
        tail = head;
        size = 0;
    }

    public boolean isNotEmpty() {
        return size != 0;
    }

    public int size() {
        return size;
    }

    public List<T> toList(boolean reverse) {
        List<T> list = new List<>();
        Node<T> cur = reverse ? head.next : tail;
        for (int l = size; l > 0; l--) {
            list.add(cur.value);
            cur = reverse ? cur.next : cur.pre;
        }
        return list;
    }
}
