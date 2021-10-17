package com.emyiqing.core;

import java.util.ArrayList;
import java.util.Set;

public class LocalVariables<T> {
    private final ArrayList<Set<T>> array;

    public LocalVariables() {
        this.array = new ArrayList<>();
    }

    public void clear(){
        this.array.clear();
    }

    public void add(Set<T> t){
        this.array.add(t);
    }

    public void set(int index, Set<T> t) {
        array.set(index, t);
    }

    public Set<T> get(int index) {
        return array.get(index);
    }

    public int size(){
        return this.array.size();
    }

    public void remove(int index){
        this.array.remove(index);
    }
}
