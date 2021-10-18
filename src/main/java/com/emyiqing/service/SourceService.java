package com.emyiqing.service;

import com.emyiqing.core.CallGraph;
import com.emyiqing.core.Source;
import com.emyiqing.model.ClassReference;
import com.emyiqing.model.MethodReference;
import org.apache.log4j.Logger;

import java.util.*;

public class SourceService {
    private static final Logger logger = Logger.getLogger(SourceService.class);

    public static void start(InheritanceMap inheritanceMap,
                             Map<MethodReference.Handle, MethodReference> methodMap,
                             Set<CallGraph> discoveredCalls,
                             Map<MethodReference.Handle, Set<CallGraph>> graphCallMap,
                             List<MethodReference> discoveredMethods,
                             Map<ClassReference.Handle, ClassReference> classMap,
                             List<Source> discoveredSources,
                             Decider decider) {
        logger.info("build source");
        for (MethodReference method : discoveredMethods) {
            methodMap.put(method.getHandle(), method);
        }
        for (CallGraph graphCall : discoveredCalls) {
            MethodReference.Handle caller = graphCall.getCallerMethod();
            if (!graphCallMap.containsKey(caller)) {
                Set<CallGraph> graphCalls = new HashSet<>();
                graphCalls.add(graphCall);
                graphCallMap.put(caller, graphCalls);
            } else {
                graphCallMap.get(caller).add(graphCall);
            }
        }
        for (MethodReference.Handle method : methodMap.keySet()) {
            if (Boolean.TRUE.equals(decider.apply(method.getClassReference()))) {
                if (method.getName().equals("finalize") && method.getDesc().equals("()V")) {
                    discoveredSources.add(new Source(method, 0));
                }
            }
        }
        for (MethodReference.Handle method : methodMap.keySet()) {
            if (Boolean.TRUE.equals(decider.apply(method.getClassReference()))) {
                if (method.getName().equals("readObject") &&
                        method.getDesc().equals("(Ljava/io/ObjectInputStream;)V")) {
                    discoveredSources.add(new Source(method, 1));
                }
            }
        }
        for (ClassReference.Handle clazz : classMap.keySet()) {
            if (Boolean.TRUE.equals(decider.apply(clazz))
                    && inheritanceMap.isSubclassOf(clazz,
                    new ClassReference.Handle("java/lang/reflect/InvocationHandler"))) {
                MethodReference.Handle method = new MethodReference.Handle(clazz, "invoke",
                        "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;");
                discoveredSources.add(new Source(method, 0));
            }
        }
        for (MethodReference.Handle method : methodMap.keySet()) {
            if (Boolean.TRUE.equals(decider.apply(method.getClassReference()))) {
                if (method.getName().equals("hashCode") && method.getDesc().equals("()I")) {
                    discoveredSources.add(new Source(method, 0));
                }
                if (method.getName().equals("equals") && method.getDesc().equals("(Ljava/lang/Object;)Z")) {
                    discoveredSources.add(new Source(method, 0));
                    discoveredSources.add(new Source(method, 1));
                }
            }
        }
    }
}
