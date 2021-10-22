package org.sec.service;

import org.sec.core.InheritanceMap;
import org.sec.core.InheritanceUtil;
import org.sec.model.ClassReference;
import org.apache.log4j.Logger;
import org.sec.model.MethodReference;

import java.util.List;
import java.util.Map;

public class InheritanceService {
    private static final Logger logger = Logger.getLogger(InheritanceService.class);

    public static InheritanceMap start(List<ClassReference> discoveredClasses,
                                       List<MethodReference> discoveredMethods,
                                       Map<ClassReference.Handle, ClassReference> classMap,
                                       Map<MethodReference.Handle, MethodReference> methodMap) {
        logger.info("build inheritance");
        for (ClassReference clazz : discoveredClasses) {
            classMap.put(clazz.getHandle(), clazz);
        }
        for (MethodReference method : discoveredMethods) {
            methodMap.put(method.getHandle(), method);
        }
        return InheritanceUtil.derive(classMap);
    }
}
