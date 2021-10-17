package com.emyiqing.service;

import com.emyiqing.model.ClassReference;

import java.util.function.Function;

public interface Decider extends Function<ClassReference.Handle, Boolean> {
}
