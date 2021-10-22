package org.sec.model;

import org.sec.util.IOUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class ClassFile {
    private final String className;
    private InputStream inputStream;

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
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IOUtil.copy(inputStream, outputStream);
        this.inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    public void close() {
        try {
            this.inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}