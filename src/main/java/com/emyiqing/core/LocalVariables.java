package com.emyiqing.core;

import java.util.ArrayList;

public class LocalVariables<T> {
    private final ArrayList<T> array;

    public LocalVariables() {
        this.array = new ArrayList<>();
    }

    public void set(int index, T t) {
        array.set(index, t);
    }

    public T get(int index) {
        return array.get(index);
    }
}
