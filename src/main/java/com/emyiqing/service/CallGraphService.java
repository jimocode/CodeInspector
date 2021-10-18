package com.emyiqing.service;

import com.emyiqing.core.CallGraph;
import com.emyiqing.core.CallGraphClassVisitor;
import com.emyiqing.model.ClassFile;
import com.emyiqing.model.ClassReference;
import com.emyiqing.model.MethodReference;
import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CallGraphService {
    private static final Logger logger = Logger.getLogger(CallGraphService.class);

    public static void start(InheritanceMap inheritanceMap,
                             Set<CallGraph> discoveredCalls,
                             List<MethodReference.Handle> sortedMethods,
                             Map<String, ClassFile> classFileByName,
                             Map<ClassReference.Handle, ClassReference> classMap,
                             Map<MethodReference.Handle, Set<Integer>> dataflow,
                             Decider decider) {
        logger.info("build call graph");
        for (MethodReference.Handle method : sortedMethods) {
            ClassFile file = classFileByName.get(method.getClassReference().getName());
            try {
                InputStream ins = file.getInputStream();
                ClassReader cr = new ClassReader(ins);
                ins.close();
                CallGraphClassVisitor cv = new CallGraphClassVisitor(classMap, inheritanceMap,
                        dataflow, decider, discoveredCalls);
                cr.accept(cv, ClassReader.EXPAND_FRAMES);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
