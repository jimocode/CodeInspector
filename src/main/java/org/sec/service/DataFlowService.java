package org.sec.service;

import org.sec.core.DataFlowClassVisitor;
import org.sec.core.InheritanceMap;
import org.sec.decide.Decider;
import org.sec.model.ClassFile;
import org.sec.model.ClassReference;
import org.sec.model.MethodReference;
import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataFlowService {
    private static final Logger logger = Logger.getLogger(DataFlowService.class);

    public static void start(InheritanceMap inheritanceMap,
                             List<MethodReference.Handle> sortedMethods,
                             Map<String, ClassFile> classFileByName,
                             Map<ClassReference.Handle, ClassReference> classMap,
                             Map<MethodReference.Handle, Set<Integer>> dataflow,
                             Decider decider) {
        logger.info("get data flow");
        for (MethodReference.Handle method : sortedMethods) {
            if (method.getName().equals("<clinit>")) {
                continue;
            }
            ClassFile file = classFileByName.get(method.getClassReference().getName());
            try {
                InputStream ins = file.getInputStream();
                ClassReader cr = new ClassReader(ins);
                ins.close();
                DataFlowClassVisitor cv = new DataFlowClassVisitor(classMap, inheritanceMap,
                        decider, dataflow, method);
                cr.accept(cv, ClassReader.EXPAND_FRAMES);
                dataflow.put(method, cv.getReturnTaint());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
