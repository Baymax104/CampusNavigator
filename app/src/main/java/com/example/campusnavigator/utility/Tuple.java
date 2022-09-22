package com.example.campusnavigator.utility;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/21 16:22
 * @Version 1
 */
public class Tuple<T1, T2> {
    public final T1 first;
    public final T2 second;

    public Tuple(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }
}
