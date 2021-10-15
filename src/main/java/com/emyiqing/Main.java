package com.emyiqing;

import com.beust.jcommander.JCommander;
import com.emyiqing.config.Command;
import com.emyiqing.config.Logo;
import com.emyiqing.core.CoreClassVisitor;
import com.emyiqing.core.DiscoveryClassVisitor;
import com.emyiqing.model.ClassFile;
import com.emyiqing.model.ClassReference;
import com.emyiqing.model.MethodReference;
import com.emyiqing.util.JarUtil;
import com.emyiqing.util.RtUtil;
import org.apache.log4j.Logger;
import org.checkerframework.checker.units.qual.A;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.util.List;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);

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
            System.out.println(classFileList.size());
            for (ClassFile file : classFileList) {
                try {
                    DiscoveryClassVisitor dcv = new DiscoveryClassVisitor();
                    ClassReader cr = new ClassReader(file.getInputStream());
                    cr.accept(dcv, ClassReader.EXPAND_FRAMES);
                    List<MethodReference> methodList = dcv.getDiscoveredMethods();
                    List<ClassReference> classList = dcv.getDiscoveredClasses();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (command.boots != null && command.boots.size() != 0) {
            List<ClassFile> classFileList = RtUtil.getAllClassesFromJars(command.boots);
            System.out.println(classFileList.size());
        }
    }
}
