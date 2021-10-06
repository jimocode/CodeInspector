package com.emyiqing.core;

import java.io.InputStream;
import java.util.Objects;

public class ClassResource {
    private final ClassLoader classLoader;
    private final String resourceName;

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClassResource that = (ClassResource) o;
        return Objects.equals(resourceName, that.resourceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classLoader, resourceName);
    }

    public ClassResource(ClassLoader classLoader, String resourceName) {
        this.classLoader = classLoader;
        this.resourceName = resourceName;
    }

    public InputStream getInputStream() {
        return classLoader.getResourceAsStream(resourceName);
    }

    public String getName() {
        return resourceName;
    }
}