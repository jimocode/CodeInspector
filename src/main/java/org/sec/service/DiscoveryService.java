package org.sec.service;

import org.sec.core.DiscoveryClassVisitor;
import org.sec.model.ClassFile;
import org.sec.model.ClassReference;
import org.sec.model.MethodReference;
import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class DiscoveryService {
    private static final Logger logger = Logger.getLogger(DiscoveryService.class);

    public static void start(List<ClassFile> classFileList,
                             List<ClassReference> discoveredClasses,
                             List<MethodReference> discoveredMethods) {
        logger.info("discover all classes");
        for (ClassFile file : classFileList) {
            try {
                DiscoveryClassVisitor dcv = new DiscoveryClassVisitor(discoveredClasses, discoveredMethods);
                InputStream ins = file.getInputStream();
                ClassReader cr = new ClassReader(ins);
                ins.close();
                cr.accept(dcv, ClassReader.EXPAND_FRAMES);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
