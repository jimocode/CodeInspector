package org.sec.service;

import org.sec.core.InheritanceMap;
import org.sec.core.InheritanceUtil;
import org.sec.model.ClassReference;
import org.sec.model.MethodReference;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.Set;

public class GadgetChainService {
    private static final Logger logger = Logger.getLogger(GadgetChainService.class);

    public static void start(InheritanceMap inheritanceMap, Map<MethodReference.Handle, MethodReference> methodMap) {
        logger.info("build gadget chains");
        Map<MethodReference.Handle, Set<MethodReference.Handle>> methodImplMap = InheritanceUtil
                .getAllMethodImplementations(
                        inheritanceMap, methodMap);
        Map<ClassReference.Handle, Set<MethodReference.Handle>> methodsByClass = InheritanceUtil
                .getMethodsByClass(methodMap);

    }
}
