package org.sec.service;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.sec.core.SpringClassVisitor;
import org.sec.model.ClassFile;
import org.sec.model.ClassReference;
import org.sec.model.MethodReference;
import org.sec.model.SpringController;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class SpringService {
    private static final Logger logger = Logger.getLogger(SpringService.class);

    public static void start(List<ClassFile> classFileList, String packageName,
                             List<SpringController> controllers,
                             Map<ClassReference.Handle, ClassReference> classMap,
                             Map<MethodReference.Handle, MethodReference> methodMap) {
        for (ClassFile file : classFileList) {
            try {
                SpringClassVisitor mcv = new SpringClassVisitor(packageName,controllers,classMap,methodMap);
                InputStream ins = file.getInputStream();
                ClassReader cr = new ClassReader(ins);
                ins.close();
                cr.accept(mcv, ClassReader.EXPAND_FRAMES);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
