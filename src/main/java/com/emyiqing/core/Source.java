package com.emyiqing.core;

import com.emyiqing.model.MethodReference;

public class Source {
    private final MethodReference.Handle sourceMethod;
    private final int taintedArgIndex;

    public Source(MethodReference.Handle sourceMethod,int taintedArgIndex) {
        this.sourceMethod = sourceMethod;
        this.taintedArgIndex = taintedArgIndex;
    }

    public MethodReference.Handle getSourceMethod() {
        return sourceMethod;
    }

    public int getTaintedArgIndex() {
        return taintedArgIndex;
    }
}
