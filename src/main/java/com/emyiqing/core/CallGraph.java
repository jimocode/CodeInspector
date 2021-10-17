package com.emyiqing.core;

import com.emyiqing.model.MethodReference;

import java.util.Objects;

public class CallGraph {
    private final MethodReference.Handle callerMethod;
    private final MethodReference.Handle targetMethod;
    private final int callerArgIndex;
    private final String callerArgPath;
    private final int targetArgIndex;

    public CallGraph(MethodReference.Handle callerMethod, MethodReference.Handle targetMethod,
                     int callerArgIndex, String callerArgPath, int targetArgIndex) {
        this.callerMethod = callerMethod;
        this.targetMethod = targetMethod;
        this.callerArgIndex = callerArgIndex;
        this.callerArgPath = callerArgPath;
        this.targetArgIndex = targetArgIndex;
    }

    public MethodReference.Handle getCallerMethod() {
        return callerMethod;
    }

    public MethodReference.Handle getTargetMethod() {
        return targetMethod;
    }

    public int getCallerArgIndex() {
        return callerArgIndex;
    }

    public String getCallerArgPath() {
        return callerArgPath;
    }

    public int getTargetArgIndex() {
        return targetArgIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CallGraph CallGraph = (CallGraph) o;

        if (callerArgIndex != CallGraph.callerArgIndex) return false;
        if (targetArgIndex != CallGraph.targetArgIndex) return false;
        if (!Objects.equals(callerMethod, CallGraph.callerMethod))
            return false;
        if (!Objects.equals(targetMethod, CallGraph.targetMethod))
            return false;
        return Objects.equals(callerArgPath, CallGraph.callerArgPath);
    }

    @Override
    public int hashCode() {
        int result = callerMethod != null ? callerMethod.hashCode() : 0;
        result = 31 * result + (targetMethod != null ? targetMethod.hashCode() : 0);
        result = 31 * result + callerArgIndex;
        result = 31 * result + (callerArgPath != null ? callerArgPath.hashCode() : 0);
        result = 31 * result + targetArgIndex;
        return result;
    }
}
