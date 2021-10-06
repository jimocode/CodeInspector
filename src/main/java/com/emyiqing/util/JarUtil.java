package com.emyiqing.util;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class JarUtil {
    private static final Logger logger = Logger.getLogger(JarUtil.class);

    /**
     * write classes and get all jars
     * @param jarPath springboot jar path
     * @return url classloader
     */
    public static ClassLoader resolveSpringBootJarFile(String jarPath) {
        try {
            final Path tmpDir = Files.createTempDirectory("exploded-jar");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                DirUtil.removeDir(new File(String.valueOf(tmpDir)));
            }));
            InputStream is = new FileInputStream(jarPath);
            JarInputStream jarInputStream = new JarInputStream(is);
            JarEntry jarEntry;
            while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                Path fullPath = tmpDir.resolve(jarEntry.getName());
                if (!jarEntry.isDirectory()) {
                    Path dirName = fullPath.getParent();
                    if (!Files.exists(dirName)) {
                        Files.createDirectories(dirName);
                    }
                    // write file
                    try (OutputStream outputStream = Files.newOutputStream(fullPath)) {
                        IOUtil.copy(jarInputStream, outputStream);
                    }
                }
            }
            final List<URL> classPathUrls = new ArrayList<>();
            classPathUrls.add(tmpDir.resolve("BOOT-INF/classes").toUri().toURL());
            Files.list(tmpDir.resolve("BOOT-INF/lib")).forEach(p -> {
                try {
                    classPathUrls.add(p.toUri().toURL());
                } catch (MalformedURLException e) {
                    logger.error("error ", e);
                }
            });
            return new URLClassLoader(classPathUrls.toArray(new URL[0]));
        } catch (Exception e) {
            logger.error("error ", e);
        }
        return null;
    }

    /**
     * resolve normal jar file
     * @param filenameList jar filename list
     * @return url classloader
     */
    public static ClassLoader resolveNormalJarFile(List<String> filenameList) {
        try {
            List<Path> pathList = StringToPath(filenameList);
            final List<URL> classPathUrls = new ArrayList<>();
            for (Path jarPath : pathList) {
                classPathUrls.add(jarPath.toUri().toURL());
            }
            return new URLClassLoader(classPathUrls.toArray(new URL[0]));
        } catch (Exception e) {
            logger.error("error ", e);
        }
        return null;
    }

    private static List<Path> StringToPath(List<String> path) {
        List<Path> res = new ArrayList<>();
        for (String p : path) {
            res.add(Paths.get(p));
        }
        return res;
    }
}
