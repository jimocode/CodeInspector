package com.emyiqing.service;

import com.emyiqing.core.Sort;
import com.emyiqing.model.MethodReference;
import org.apache.log4j.Logger;

import java.util.*;

public class SortService {
    private static final Logger logger = Logger.getLogger(SortService.class);

    public static List<MethodReference.Handle> start(
            Map<MethodReference.Handle, Set<MethodReference.Handle>> methodCalls) {
        logger.info("topological sort methods");
        Map<MethodReference.Handle, Set<MethodReference.Handle>> outgoingReferences = new HashMap<>();
        for (Map.Entry<MethodReference.Handle, Set<MethodReference.Handle>> entry : methodCalls.entrySet()) {
            MethodReference.Handle method = entry.getKey();
            outgoingReferences.put(method, new HashSet<>(entry.getValue()));
        }
        Set<MethodReference.Handle> dfsStack = new HashSet<>();
        Set<MethodReference.Handle> visitedNodes = new HashSet<>();
        List<MethodReference.Handle> sortedMethods = new ArrayList<>(outgoingReferences.size());
        for (MethodReference.Handle root : outgoingReferences.keySet()) {
            Sort.dfsSort(outgoingReferences, sortedMethods, visitedNodes, dfsStack, root);
        }
        return sortedMethods;
    }
}
