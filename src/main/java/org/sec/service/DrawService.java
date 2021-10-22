package org.sec.service;

import org.sec.core.CallGraph;
import org.sec.model.ClassReference;
import org.sec.model.MethodReference;
import org.sec.util.DrawUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DrawService {
    public static void start(Set<CallGraph> discoveredCalls, String finalPackageName,
                             Map<ClassReference.Handle, ClassReference> classMap,
                             Map<MethodReference.Handle, Set<MethodReference.Handle>> methodImplMap) {
        Set<CallGraph> targetCallGraphs = new HashSet<>();
        for (CallGraph callGraph : discoveredCalls) {
            ClassReference callerClass = classMap.get(callGraph.getCallerMethod().getClassReference());
            ClassReference targetClass = classMap.get(callGraph.getTargetMethod().getClassReference());
            if (targetClass == null) {
                continue;
            }
            if (targetClass.getName().equals("java/lang/Object") &&
                    callGraph.getTargetMethod().getName().equals("<init>") &&
                    callGraph.getTargetMethod().getDesc().equals("()V")) {
                continue;
            }
            if (callerClass.getName().startsWith(finalPackageName)) {
                targetCallGraphs.add(callGraph);
                if (targetClass.isInterface()) {
                    for (MethodReference.Handle handle : methodImplMap.get(callGraph.getTargetMethod())) {
                        targetCallGraphs.add(new CallGraph(callGraph.getTargetMethod(), handle));
                    }
                }
            }
        }
        DrawUtil.drawCallGraph(targetCallGraphs);
    }
}
