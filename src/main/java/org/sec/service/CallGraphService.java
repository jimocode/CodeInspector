package org.sec.service;

import org.sec.core.CallGraph;
import org.sec.core.CallGraphClassVisitor;
import org.sec.core.InheritanceMap;
import org.sec.core.InheritanceUtil;
import org.sec.model.ClassFile;
import org.sec.model.ClassReference;
import org.sec.model.MethodReference;
import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class CallGraphService {
    private static final Logger logger = Logger.getLogger(CallGraphService.class);

    public static void start(InheritanceMap inheritanceMap,
                             Set<CallGraph> discoveredCalls,
                             List<MethodReference.Handle> sortedMethods,
                             Map<String, ClassFile> classFileByName,
                             Map<ClassReference.Handle, ClassReference> classMap,
                             Map<MethodReference.Handle, Set<Integer>> dataflow,
                             Map<MethodReference.Handle, Set<CallGraph>> graphCallMap,
                             Map<MethodReference.Handle, MethodReference> methodMap) {
        logger.info("build call graph");
        for (MethodReference.Handle method : sortedMethods) {
            ClassFile file = classFileByName.get(method.getClassReference().getName());
            try {
                InputStream ins = file.getInputStream();
                ClassReader cr = new ClassReader(ins);
                ins.close();
                CallGraphClassVisitor cv = new CallGraphClassVisitor(classMap, inheritanceMap,
                        dataflow, discoveredCalls);
                cr.accept(cv, ClassReader.EXPAND_FRAMES);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Map<MethodReference.Handle, Set<MethodReference.Handle>> methodImplMap =
                InheritanceUtil.getAllMethodImplementations(inheritanceMap, methodMap);
        List<CallGraph> tempList = new ArrayList<>(discoveredCalls);
        for (int i = 0; i < discoveredCalls.size(); i++) {
            ClassReference.Handle handle = tempList.get(i).getTargetMethod().getClassReference();
            ClassReference classReference = classMap.get(handle);
            if (classReference != null && classReference.isInterface()) {
                Set<MethodReference.Handle> implSet = methodImplMap.get(tempList.get(i).getTargetMethod());
                if (implSet == null || implSet.size() == 0) {
                    continue;
                }
                for (MethodReference.Handle methodHandle : implSet) {
                    String callerDesc = methodMap.get(methodHandle).getDesc();
                    if (tempList.get(i).getTargetMethod().getDesc().equals(callerDesc)) {
                        tempList.add(new CallGraph(
                                tempList.get(i).getTargetMethod(),
                                methodHandle,
                                tempList.get(i).getTargetArgIndex(),
                                null,
                                tempList.get(i).getTargetArgIndex()
                        ));
                    }
                }
            }
        }
        discoveredCalls.clear();
        discoveredCalls.addAll(tempList);
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
    }
}
