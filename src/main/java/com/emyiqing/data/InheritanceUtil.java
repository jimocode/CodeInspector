package com.emyiqing.data;

import com.emyiqing.model.ClassReference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InheritanceUtil {
    public static InheritanceMap derive(Map<ClassReference.Handle, ClassReference> classMap) {
        Map<ClassReference.Handle, Set<ClassReference.Handle>> implicitInheritance = new HashMap<>();
        for (ClassReference classReference : classMap.values()) {
            Set<ClassReference.Handle> allParents = new HashSet<>();
            getAllParents(classReference, classMap, allParents);
            implicitInheritance.put(classReference.getHandle(), allParents);
        }
        return new InheritanceMap(implicitInheritance);
    }

    private static void getAllParents(ClassReference classReference,
                                      Map<ClassReference.Handle, ClassReference> classMap,
                                      Set<ClassReference.Handle> allParents) {
        Set<ClassReference.Handle> parents = new HashSet<>();
        if (classReference.getSuperClass() != null) {
            parents.add(new ClassReference.Handle(classReference.getSuperClass()));
        }
        for (String i : classReference.getInterfaces()) {
            parents.add(new ClassReference.Handle(i));
        }

        for (ClassReference.Handle immediateParent : parents) {
            ClassReference parentClassReference = classMap.get(immediateParent);
            if (parentClassReference == null) {
                continue;
            }
            allParents.add(parentClassReference.getHandle());
            getAllParents(parentClassReference, classMap, allParents);
        }
    }
}
