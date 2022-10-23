package com.example.campusnavigator.utility.interfaces;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/10/20 22:22
 * @Version 1
 */
public interface Stackable<E> {
    void push(E e);
    void pop();
    E top();
}
