package com.emyiqing;

import com.beust.jcommander.JCommander;
import com.emyiqing.config.Command;
import com.emyiqing.config.Logo;
import com.emyiqing.core.ClassResource;
import com.emyiqing.core.ClassResourceUtil;
import com.emyiqing.util.FileUtil;
import com.emyiqing.util.JarUtil;
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
        ClassLoader classLoader = JarUtil.resolveNormalJarFile(command.files);
        ClassResourceUtil classResourceUtil = new ClassResourceUtil(classLoader);
        List<ClassResource> cls = classResourceUtil.getAllClasses();
    }
}
