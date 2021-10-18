package com.emyiqing.service;

import com.emyiqing.core.DiscoveryClassVisitor;
import com.emyiqing.model.ClassFile;
import com.emyiqing.model.ClassReference;
import com.emyiqing.model.MethodReference;
import com.emyiqing.util.SaveUtil;
import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class DiscoveryService {
    private static final Logger logger = Logger.getLogger(DiscoveryService.class);

    @SuppressWarnings("unchecked")
    public static void start(List<ClassFile> classFileList,
                             List<ClassReference> discoveredClasses,
                             List<MethodReference> discoveredMethods) {
        logger.info("discover all classes");
        if (Files.exists(Paths.get("classes.dat"))) {
            discoveredClasses = (List<ClassReference>) SaveUtil.read("classes.dat");
            discoveredMethods = (List<MethodReference>) SaveUtil.read("methods.dat");
            if ((discoveredClasses == null || discoveredClasses.size() == 0)) {
                return;
            }
            if ((discoveredMethods == null || discoveredMethods.size() == 0)) {
                return;
            }
            logger.info("use cache data");
        } else {
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
            SaveUtil.save(discoveredClasses, "classes.dat");
            SaveUtil.save(discoveredMethods, "methods.dat");
        }
    }
}
