package com.example.campusnavigator.utility.structures;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/21 16:22
 * @Version 1
 */
public class Tuple<E1, E2> {
    public final E1 first;
    public final E2 second;

    public Tuple(E1 first, E2 second) {
        this.first = first;
        this.second = second;
    }
}
