package com.example.campusnavigator.utility;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/1 20:11
 * @Version 1
 */
public class HashTable<K,V> {
    // TODO HashTable优化PositionProvider查找
    // HashMap使用hash()获取桶索引，当桶中元素个数<=6时，使用单链表存储，当个数>6时，使用红黑树存储
    // 单链表存储时，时间复杂度为O(1)+O(n)=O(n)
    // 红黑树存储时，时间复杂度为O(1)+O(logn)=O(logn)
}
