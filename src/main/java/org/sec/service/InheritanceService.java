package org.sec.service;

import org.sec.core.InheritanceMap;
import org.sec.core.InheritanceUtil;
import org.sec.model.ClassReference;
import org.apache.log4j.Logger;

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
        return InheritanceUtil.derive(classMap);
    }
}
