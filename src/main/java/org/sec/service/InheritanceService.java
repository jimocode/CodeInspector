package org.sec.service;

import org.sec.core.InheritanceMap;
import org.sec.core.InheritanceUtil;
import org.sec.model.ClassReference;
import org.sec.util.SaveUtil;
import org.apache.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class InheritanceService {
    private static final Logger logger = Logger.getLogger(InheritanceService.class);

    public static InheritanceMap start(List<ClassReference> discoveredClasses,
                                       Map<ClassReference.Handle, ClassReference> classMap) {
        logger.info("build inheritance");
        for (ClassReference clazz : discoveredClasses) {
            classMap.put(clazz.getHandle(), clazz);
        }
        InheritanceMap inheritanceMap;
        if (Files.exists(Paths.get("inheritance.dat"))) {
            inheritanceMap = (InheritanceMap) SaveUtil.read("inheritance.dat");
            logger.info("use cache data");
        } else {
            inheritanceMap = InheritanceUtil.derive(classMap);
            SaveUtil.save(inheritanceMap, "inheritance.dat");
        }
        return inheritanceMap;
    }
}
