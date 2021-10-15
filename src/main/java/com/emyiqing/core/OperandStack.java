package com.emyiqing.core;

import java.util.LinkedList;

public class OperandStack<T> {
    private final LinkedList<T> stack;

    public OperandStack() {
        this.stack = new LinkedList<>();
    }

    public void pop() {
        stack.remove(stack.size() - 1);
    }

    public void push(T t) {
        stack.add(t);
    }

    public void clear() {
        stack.clear();
    }

    public T get(int index) {
        return stack.get(stack.size() - index - 1);
    }

    public void set(int index, T t) {
        stack.set(stack.size() - index - 1, t);
    }
}
