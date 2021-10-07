package com.emyiqing.util;

import com.emyiqing.model.ClassFile;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class JarUtil {
    private static final Logger logger = Logger.getLogger(JarUtil.class);
    private static final Set<ClassFile> classFileSet = new HashSet<>();

    public static List<ClassFile> resolveSpringBootJarFile(String jarPath) {
        try {
            final Path tmpDir = Files.createTempDirectory(UUID.randomUUID().toString());
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                closeAll();
                DirUtil.removeDir(tmpDir.toFile());
            }));
            resolve(jarPath, tmpDir);
            resolveBoot(jarPath, tmpDir);
            Files.list(tmpDir.resolve("BOOT-INF/lib")).forEach(p -> {
                resolveNormalJarFile(p.toFile().getAbsolutePath());
            });
            return new ArrayList<>(classFileSet);
        } catch (Exception e) {
            logger.error("error ", e);
        }
        return new ArrayList<>();
    }

    public static List<ClassFile> resolveNormalJarFile(String jarPath) {
        try {
            final Path tmpDir = Files.createTempDirectory(UUID.randomUUID().toString());
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                closeAll();
                DirUtil.removeDir(tmpDir.toFile());
            }));
            resolve(jarPath, tmpDir);
            return new ArrayList<>(classFileSet);
        } catch (Exception e) {
            logger.error("error ", e);
        }
        return new ArrayList<>();
    }

    private static void resolve(String jarPath, Path tmpDir) {
        try {
            InputStream is = new FileInputStream(jarPath);
            JarInputStream jarInputStream = new JarInputStream(is);
            JarEntry jarEntry;
            while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                Path fullPath = tmpDir.resolve(jarEntry.getName());
                if (!jarEntry.isDirectory()) {
                    if (!jarEntry.getName().endsWith(".class")) {
                        continue;
                    }
                    Path dirName = fullPath.getParent();
                    if (!Files.exists(dirName)) {
                        Files.createDirectories(dirName);
                    }
                    OutputStream outputStream = Files.newOutputStream(fullPath);
                    IOUtil.copy(jarInputStream, outputStream);
                    InputStream fis = new FileInputStream(fullPath.toFile());
                    ClassFile classFile = new ClassFile(jarEntry.getName(), fis);
                    classFileSet.add(classFile);
                }
            }
        } catch (Exception e) {
            logger.error("error ", e);
        }
    }

    private static void resolveBoot(String jarPath, Path tmpDir) {
        try {
            InputStream is = new FileInputStream(jarPath);
            JarInputStream jarInputStream = new JarInputStream(is);
            JarEntry jarEntry;
            while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                Path fullPath = tmpDir.resolve(jarEntry.getName());
                if (!jarEntry.isDirectory()) {
                    if (!jarEntry.getName().endsWith(".jar")) {
                        continue;
                    }
                    Path dirName = fullPath.getParent();
                    if (!Files.exists(dirName)) {
                        Files.createDirectories(dirName);
                    }
                    OutputStream outputStream = Files.newOutputStream(fullPath);
                    IOUtil.copy(jarInputStream, outputStream);
                }
            }
        } catch (Exception e) {
            logger.error("error ", e);
        }
    }

    private static void closeAll() {
        List<ClassFile> classFileList = new ArrayList<>(classFileSet);
        for (ClassFile classFile : classFileList) {
            try {
                classFile.getInputStream().close();
            } catch (IOException e) {
                logger.error("error ", e);
            }
        }
    }
}