package com.emyiqing.util;

import java.io.*;

public class SaveUtil {
    public static void save(Object object,String path) {
        try {
            FileOutputStream aos = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(aos);
            oos.writeObject(object);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Object read(String path) {
        try {
            FileInputStream ais = new FileInputStream(path);
            ObjectInputStream ois = new ObjectInputStream(ais);
            Object object = ois.readObject();
            ois.close();
            return object;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
