package com.emyiqing.util;

import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileUtil {
    private static final Logger logger = Logger.getLogger(FileUtil.class);

    public static String readFile(String filename) {
        try {
            InputStream r = new FileInputStream(filename);
            return new String(doReadFile(r), StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("error ", e);
        }
        return "";
    }

    public static String readFile(InputStream is) {
        return new String(doReadFile(is), StandardCharsets.UTF_8);
    }

    public static byte[] readFileBytes(InputStream is) {
        return doReadFile(is);
    }

    private static byte[] doReadFile(InputStream is) {
        try {
            ByteArrayOutputStream byteData = new ByteArrayOutputStream();
            byte[] temp = new byte[1024];
            byte[] context;
            int i;
            while ((i = is.read(temp)) > 0) {
                byteData.write(temp, 0, i);
            }
            context = byteData.toByteArray();
            is.close();
            byteData.close();
            return context;
        } catch (Exception e) {
            logger.error("error ", e);
        }
        return new byte[0];
    }

    public static void writeFile(String filename, String output) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            if (file.createNewFile()) {
                logger.debug("create new output file");
            }
        }
        BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file),
                        StandardCharsets.UTF_8));
        bw.write(output);
        bw.close();
    }

    public static String getFileExt(String filename) {
        String[] splits = filename.split("\\.");
        return splits[splits.length - 1];
    }
}
