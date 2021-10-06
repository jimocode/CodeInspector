package com.emyiqing.core;

import com.google.common.reflect.ClassPath;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class ClassResourceUtil {
    private static final Logger logger = Logger.getLogger(ClassResourceUtil.class);

    private final ClassLoader classLoader;

    public ClassResourceUtil(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public List<ClassResource> getAllClasses() {
        try {
            Set<ClassResource> result = new HashSet<>(getRuntimeClasses());
            for (ClassPath.ClassInfo classInfo : ClassPath.from(classLoader).getAllClasses()) {
                result.add(new ClassResource(classLoader, classInfo.getResourceName()));
            }
            return new ArrayList<>(result);
        } catch (Exception e) {
            logger.error("error ", e);
        }
        return null;
    }

    private Collection<ClassResource> getRuntimeClasses() throws IOException {
        URL stringClassUrl = Object.class.getResource("String.class");
        URLConnection connection = null;
        if (stringClassUrl != null) {
            connection = stringClassUrl.openConnection();
        }
        Collection<ClassResource> result = new ArrayList<>();
        if (connection instanceof JarURLConnection) {
            URL runtimeUrl = ((JarURLConnection) connection).getJarFileURL();
            URLClassLoader classLoader = new URLClassLoader(new URL[]{runtimeUrl});
            for (ClassPath.ClassInfo classInfo : ClassPath.from(classLoader).getAllClasses()) {
                result.add(new ClassResource(classLoader, classInfo.getResourceName()));
            }
        }
        return result;
    }
}
