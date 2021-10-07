package com.emyiqing.model;

import java.io.InputStream;
import java.util.Objects;

public class ClassFile {
    private final String className;
    private final InputStream inputStream;

    public ClassFile(String className, InputStream inputStream) {
        this.className = className;
        this.inputStream = inputStream;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClassFile classFile = (ClassFile) o;
        return Objects.equals(className, classFile.className);
    }

    @Override
    public int hashCode() {
        return className != null ? className.hashCode() : 0;
    }

    public String getClassName() {
        return className;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}