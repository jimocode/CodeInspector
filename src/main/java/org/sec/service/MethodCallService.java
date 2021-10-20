package org.sec.service;

import org.sec.core.MethodCallClassVisitor;
import org.sec.model.ClassFile;
import org.sec.model.MethodReference;
import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MethodCallService {
    private static final Logger logger = Logger.getLogger(MethodCallService.class);

    public static void start(List<ClassFile> classFileList,
                             Map<MethodReference.Handle, Set<MethodReference.Handle>> methodCalls,
                             Map<String, ClassFile> classFileByName) {
        logger.info("get method calls in method");
        for (ClassFile file : classFileList) {
            try {
                MethodCallClassVisitor mcv = new MethodCallClassVisitor(methodCalls);
                InputStream ins = file.getInputStream();
                ClassReader cr = new ClassReader(ins);
                ins.close();
                cr.accept(mcv, ClassReader.EXPAND_FRAMES);
                classFileByName.put(mcv.getName(), file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
