package com.emyiqing.service;

import com.emyiqing.data.InheritanceMap;
import com.emyiqing.model.ClassReference;

import java.util.HashMap;
import java.util.Map;

public class SimpleSerializableDecider implements Decider {
    private final Map<ClassReference.Handle, Boolean> cache = new HashMap<>();
    private final InheritanceMap inheritanceMap;

    public SimpleSerializableDecider(InheritanceMap inheritanceMap) {
        this.inheritanceMap = inheritanceMap;
    }

    @Override
    public Boolean apply(ClassReference.Handle handle) {
        Boolean cached = cache.get(handle);
        if (cached != null) {
            return cached;
        }
        Boolean result = inheritanceMap.isSubclassOf(handle,
                new ClassReference.Handle("java/io/Serializable"));
        cache.put(handle, result);
        return result;
    }
}
