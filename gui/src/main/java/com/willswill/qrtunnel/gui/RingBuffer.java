package com.willswill.qrtunnel.gui;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Will
 */
public class RingBuffer<T> {
    private final List<T> buf;
    private final int maxSize;
    private int cursor;

    public RingBuffer(int maxSize) {
        this.maxSize = maxSize;
        buf = new ArrayList<>(maxSize);
        for (int i = 0; i < maxSize; i++) {
            buf.add(null);
        }
        cursor = 0;
    }

    public synchronized void put(T object) {
        buf.set(cursor++, object);
        if (cursor >= maxSize) {
            cursor = 0;
        }
    }

    public List<T> readFully() {
        List<T> ret = new ArrayList<>(maxSize);
        int cursor = this.cursor;
        for (int i = cursor; i < maxSize; i++) {
            T t = buf.get(i);
            if (t != null) {
                ret.add(t);
            }
        }
        for (int i = 0; i < cursor; i++) {
            T t = buf.get(i);
            if (t != null) {
                ret.add(t);
            }
        }
        return ret;
    }
}
