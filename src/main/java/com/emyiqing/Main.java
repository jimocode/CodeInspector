package com.emyiqing;

import com.beust.jcommander.JCommander;
import com.emyiqing.config.Command;
import com.emyiqing.config.Logo;
import com.emyiqing.model.ClassFile;
import com.emyiqing.util.JarUtil;
import com.emyiqing.util.RtUtil;
import org.apache.log4j.Logger;

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
        }
        if (command.boots != null && command.boots.size() != 0) {
            List<ClassFile> classFileList = RtUtil.getAllClassesFromJars(command.boots);
            System.out.println(classFileList.size());
        }
    }
}
