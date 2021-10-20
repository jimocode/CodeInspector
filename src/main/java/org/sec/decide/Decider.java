package org.sec.decide;

import org.sec.model.ClassReference;

import java.util.function.Function;

public interface Decider extends Function<ClassReference.Handle, Boolean> {
}
