package org.sec.service;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.sec.core.CallGraph;
import org.sec.core.InheritanceMap;
import org.sec.core.SSRFClassVisitor;
import org.sec.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SSRFService {
    private static final Logger logger = Logger.getLogger(SSRFService.class);

    private static Map<MethodReference.Handle, Set<CallGraph>> allCalls;
    private static Map<String, ClassFile> classFileMap;
    private static InheritanceMap localInheritanceMap;
    private static Map<MethodReference.Handle, Set<Integer>> localDataFlow;

    public static void start(Map<String, ClassFile> classFileByName,
                             List<SpringController> controllers,
                             InheritanceMap inheritanceMap,
                             Map<MethodReference.Handle, Set<Integer>> dataFlow,
                             Map<MethodReference.Handle, Set<CallGraph>> discoveredCalls,
                             Map<MethodReference.Handle, MethodReference> methodMap) {
        allCalls = discoveredCalls;
        classFileMap = classFileByName;
        localInheritanceMap = inheritanceMap;
        localDataFlow = dataFlow;

        logger.info("analysis ssrf...");
        for (SpringController controller : controllers) {
            for (SpringMapping mapping : controller.getMappings()) {
                MethodReference methodReference = mapping.getMethodReference();
                if (methodReference == null) {
                    continue;
                }
                Type[] argTypes = Type.getArgumentTypes(methodReference.getDesc());
                Type[] extendedArgTypes = new Type[argTypes.length + 1];
                System.arraycopy(argTypes, 0, extendedArgTypes, 1, argTypes.length);
                argTypes = extendedArgTypes;
                boolean[] vulnerableIndex = new boolean[argTypes.length];
                for (int i = 1; i < argTypes.length; i++) {
                    if (argTypes[i].getClassName().equals("java.lang.String")) {
                        vulnerableIndex[i] = true;
                    }
                }
                Set<CallGraph> calls = allCalls.get(methodReference.getHandle());
                if (calls == null || calls.size() == 0) {
                    continue;
                }
                // mapping method中的call
                for (CallGraph callGraph : calls) {
                    int callerIndex = callGraph.getCallerArgIndex();
                    if (callerIndex == -1) {
                        continue;
                    }
                    if (vulnerableIndex[callerIndex]) {
                        // 防止循环
                        List<MethodReference.Handle> visited = new ArrayList<>();
                        doTask(callGraph.getTargetMethod(), callGraph.getTargetArgIndex(), visited);
                    }
                }
            }
        }
    }

    private static void doTask(MethodReference.Handle targetMethod, int targetIndex,
                               List<MethodReference.Handle> visited) {
        if (visited.contains(targetMethod)) {
            return;
        } else {
            visited.add(targetMethod);
        }
        ClassFile file = classFileMap.get(targetMethod.getClassReference().getName());
        try {
            InputStream ins = file.getInputStream();
            ClassReader cr = new ClassReader(ins);
            ins.close();
            SSRFClassVisitor cv = new SSRFClassVisitor(
                    targetMethod, targetIndex, localInheritanceMap, localDataFlow);
            cr.accept(cv, ClassReader.EXPAND_FRAMES);
            if (cv.getPass().size() == 3 && !cv.getPass().contains(false)) {
                String message = targetMethod.getClassReference().getName() + "." + targetMethod.getName();
                logger.info("detect ssrf: " + message);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Set<CallGraph> calls = allCalls.get(targetMethod);
        if (calls == null || calls.size() == 0) {
            return;
        }
        for (CallGraph callGraph : calls) {
            if (callGraph.getCallerArgIndex() == targetIndex && targetIndex != -1) {
                if (visited.contains(callGraph.getTargetMethod())) {
                    return;
                }
                doTask(callGraph.getTargetMethod(), callGraph.getTargetArgIndex(), visited);
            }
        }
    }
}
