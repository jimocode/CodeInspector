package com.emyiqing;

import com.beust.jcommander.JCommander;
import com.emyiqing.config.Command;
import com.emyiqing.config.Logo;
import com.emyiqing.core.DiscoveryClassVisitor;
import com.emyiqing.data.InheritanceMap;
import com.emyiqing.data.InheritanceUtil;
import com.emyiqing.model.ClassFile;
import com.emyiqing.model.ClassReference;
import com.emyiqing.model.MethodReference;
import com.emyiqing.util.RtUtil;
import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);
    private static final List<ClassReference> discoveredClasses = new ArrayList<>();
    private static final List<MethodReference> discoveredMethods = new ArrayList<>();

    private static  InheritanceMap inheritanceMap;

    public static void main(String[] args) {
        Logo.PrintLogo();
        logger.info("start code inspector");
        Command command = new Command();
        JCommander jc = JCommander.newBuilder().addObject(command).build();
        jc.parse(args);
        if (command.help) {
            jc.usage();
        }
        if (command.jars != null && command.jars.size() != 0) {
            List<ClassFile> classFileList = RtUtil.getAllClassesFromJars(command.jars);
            for (ClassFile file : classFileList) {
                try {
                    DiscoveryClassVisitor dcv = new DiscoveryClassVisitor(discoveredClasses, discoveredMethods);
                    ClassReader cr = new ClassReader(file.getInputStream());
                    cr.accept(dcv, ClassReader.EXPAND_FRAMES);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Map<ClassReference.Handle, ClassReference> classMap = new HashMap<>();
            for (ClassReference clazz : discoveredClasses) {
                classMap.put(clazz.getHandle(), clazz);
            }
            inheritanceMap = InheritanceUtil.derive(classMap);
            System.out.println(inheritanceMap);
        }
    }
}
