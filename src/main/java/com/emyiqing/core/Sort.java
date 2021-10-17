package com.emyiqing.core;

import com.emyiqing.model.MethodReference;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Sort {
    public static void dfsSort(Map<MethodReference.Handle, Set<MethodReference.Handle>> outgoingReferences,
                                 List<MethodReference.Handle> sortedMethods, Set<MethodReference.Handle> visitedNodes,
                                 Set<MethodReference.Handle> stack, MethodReference.Handle node) {
        if (stack.contains(node)) {
            return;
        }
        if (visitedNodes.contains(node)) {
            return;
        }
        Set<MethodReference.Handle> outgoingRefs = outgoingReferences.get(node);
        if (outgoingRefs == null) {
            return;
        }
        stack.add(node);
        for (MethodReference.Handle child : outgoingRefs) {
            dfsSort(outgoingReferences, sortedMethods, visitedNodes, stack, child);
        }
        stack.remove(node);
        visitedNodes.add(node);
        sortedMethods.add(node);
    }
}
