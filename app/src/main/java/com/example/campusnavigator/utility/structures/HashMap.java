package com.example.campusnavigator.utility.structures;

/**
 * @Description
 * @Author John
 * @email
 * @Date 2022/9/1 20:11
 * @Version 1
 */
public class HashMap<K, V> {
    private static final int MAX_SIZE = 64;

    private static class Entry<K, V> {
        final int hash;
        final K key;
        V value;
        Entry<K, V> next;
        Entry(int hash, K key, V value) {
            this.hash = hash;
            this.key = key;
            this.value = value;
        }
    }

    @SuppressWarnings("unchecked")
    private final Entry<K, V>[] table = (Entry<K, V>[]) new Entry[MAX_SIZE];

    private static int hash(Object obj) {
        int h;
        return (obj == null) ? 0 : (h = obj.hashCode()) ^ (h >>> 16);
    }

    private int index(Object obj) {
        return (table.length - 1) & hash(obj);
    }

    public void put(K key, V value) {
        if (key == null) {
            return;
        }
        int i = index(key);
        Entry<K, V> entry = table[i];
        if (entry == null) {
            entry = new Entry<>(hash(key), key, value);
            table[i] = entry;
        } else {
            Entry<K, V> e = entry;
            // 查找是否存在重复对象，存在则更新
            while (e != null) {
                if (e.hash == hash(key) && (e.key == key || key.equals(e.key))) {
                    e.value = value;
                    return;
                }
                e = e.next;
            }

            Entry<K, V> newEntry = new Entry<>(hash(key), key, value);
            newEntry.next = entry;
            table[i] = newEntry;
        }
    }

    public V get(K key) {
        if (key == null) {
            return null;
        }
        int i = index(key);
        int hash = hash(key);
        Entry<K, V> e = table[i];
        while (e != null) {
            if (e.hash == hash && (e.key == key || key.equals(e.key))) {
                return e.value;
            }
            e = e.next;
        }
        return null;
    }
}
