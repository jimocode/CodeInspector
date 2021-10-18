package com.emyiqing;

import com.beust.jcommander.JCommander;
import com.emyiqing.config.Command;
import com.emyiqing.config.Logo;
import com.emyiqing.core.*;
import com.emyiqing.service.*;
import com.emyiqing.model.ClassFile;
import com.emyiqing.model.ClassReference;
import com.emyiqing.model.MethodReference;
import com.emyiqing.util.RtUtil;
import org.apache.log4j.Logger;

import java.util.*;

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
            start(command.jars);
        }
    }

    private static void start(List<String> jars) {
        List<ClassReference> discoveredClasses = new ArrayList<>();
        List<MethodReference> discoveredMethods = new ArrayList<>();
        InheritanceMap inheritanceMap;
        Map<MethodReference.Handle, Set<MethodReference.Handle>> methodCalls = new HashMap<>();
        List<MethodReference.Handle> sortedMethods;
        Map<MethodReference.Handle, Set<Integer>> dataflow = new HashMap<>();
        Set<CallGraph> discoveredCalls = new HashSet<>();
        Map<ClassReference.Handle, ClassReference> classMap = new HashMap<>();
        Map<String, ClassFile> classFileByName = new HashMap<>();
        List<Source> discoveredSources = new ArrayList<>();
        Map<MethodReference.Handle, MethodReference> methodMap = new HashMap<>();
        Map<MethodReference.Handle, Set<CallGraph>> graphCallMap = new HashMap<>();

        List<ClassFile> classFileList = RtUtil.getAllClassesFromJars(jars);

        DiscoveryService.start(classFileList, discoveredClasses, discoveredMethods);

        inheritanceMap = InheritanceService.start(discoveredClasses, classMap);

        MethodCallService.start(classFileList, methodCalls, classFileByName);

        sortedMethods = SortService.start(methodCalls);

        Decider decider = new SimpleSerializableDecider(inheritanceMap);
        DataFlowService.start(inheritanceMap, sortedMethods,
                classFileByName, classMap, dataflow, decider);

        CallGraphService.start(inheritanceMap, discoveredCalls,
                sortedMethods, classFileByName, classMap, dataflow, decider);

        SourceService.start(inheritanceMap, methodMap,discoveredCalls,graphCallMap,
                discoveredMethods, classMap, discoveredSources, decider);

        logger.info("build gadget chains");
        Map<MethodReference.Handle, Set<MethodReference.Handle>> methodImplMap = InheritanceUtil
                .getAllMethodImplementations(
                        inheritanceMap, methodMap);
        Map<ClassReference.Handle, Set<MethodReference.Handle>> methodsByClass = InheritanceUtil
                .getMethodsByClass(methodMap);
        System.out.println("finish");
    }
}
