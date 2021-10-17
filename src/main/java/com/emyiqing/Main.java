package com.emyiqing;

import com.beust.jcommander.JCommander;
import com.emyiqing.config.Command;
import com.emyiqing.config.Logo;
import com.emyiqing.core.*;
import com.emyiqing.data.InheritanceMap;
import com.emyiqing.data.InheritanceUtil;
import com.emyiqing.model.ClassFile;
import com.emyiqing.model.ClassReference;
import com.emyiqing.model.MethodReference;
import com.emyiqing.service.Decider;
import com.emyiqing.service.SimpleSerializableDecider;
import com.emyiqing.util.RtUtil;
import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
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

        logger.info("get all classes");
        List<ClassFile> classFileList = RtUtil.getAllClassesFromJars(jars);

        logger.info("discover all classes");
        for (ClassFile file : classFileList) {
            try {
                DiscoveryClassVisitor dcv = new DiscoveryClassVisitor(discoveredClasses, discoveredMethods);
                InputStream ins = file.getInputStream();
                ClassReader cr = new ClassReader(ins);
                ins.close();
                cr.accept(dcv, ClassReader.EXPAND_FRAMES);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        logger.info("build inheritance");
        Map<ClassReference.Handle, ClassReference> classMap = new HashMap<>();
        for (ClassReference clazz : discoveredClasses) {
            classMap.put(clazz.getHandle(), clazz);
        }
        inheritanceMap = InheritanceUtil.derive(classMap);

        logger.info("get method calls in method");
        Map<String, ClassFile> classFileByName = new HashMap<>();
        for (ClassFile file : classFileList) {
            try {
                MethodCallClassVisitor mcv = new MethodCallClassVisitor(methodCalls);
                InputStream ins = file.getInputStream();
                ClassReader cr = new ClassReader(ins);
                ins.close();
                cr.accept(mcv, ClassReader.EXPAND_FRAMES);
                classFileByName.put(mcv.getName(), file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        logger.info("topological sort methods");
        Map<MethodReference.Handle, Set<MethodReference.Handle>> outgoingReferences = new HashMap<>();
        for (Map.Entry<MethodReference.Handle, Set<MethodReference.Handle>> entry : methodCalls.entrySet()) {
            MethodReference.Handle method = entry.getKey();
            outgoingReferences.put(method, new HashSet<>(entry.getValue()));
        }
        Set<MethodReference.Handle> dfsStack = new HashSet<>();
        Set<MethodReference.Handle> visitedNodes = new HashSet<>();
        sortedMethods = new ArrayList<>(outgoingReferences.size());
        for (MethodReference.Handle root : outgoingReferences.keySet()) {
            Sort.dfsSort(outgoingReferences, sortedMethods, visitedNodes, dfsStack, root);
        }

        logger.info("get data flow");
        Decider decider = new SimpleSerializableDecider(inheritanceMap);
        for (MethodReference.Handle method : sortedMethods) {
            if (method.getName().equals("<clinit>")) {
                continue;
            }
            ClassFile file = classFileByName.get(method.getClassReference().getName());
            try {
                InputStream ins = file.getInputStream();
                ClassReader cr = new ClassReader(ins);
                ins.close();
                DataFlowClassVisitor cv = new DataFlowClassVisitor(classMap, inheritanceMap,
                        decider, dataflow, method);
                cr.accept(cv, ClassReader.EXPAND_FRAMES);
                dataflow.put(method, cv.getReturnTaint());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        logger.info("build call graph");
        for (MethodReference.Handle method : sortedMethods) {
            ClassFile file = classFileByName.get(method.getClassReference().getName());
            try {
                InputStream ins = file.getInputStream();
                ClassReader cr = new ClassReader(ins);
                ins.close();
                CallGraphClassVisitor cv = new CallGraphClassVisitor(classMap, inheritanceMap,
                        dataflow, decider, discoveredCalls);
                cr.accept(cv, ClassReader.EXPAND_FRAMES);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        logger.info("build source");
        final List<Source> discoveredSources = new ArrayList<>();
        Map<MethodReference.Handle, MethodReference> methodMap = new HashMap<>();
        for (MethodReference method : discoveredMethods) {
            methodMap.put(method.getHandle(), method);
        }
        final Decider serializableDecider = new SimpleSerializableDecider(inheritanceMap);
        Map<MethodReference.Handle, Set<CallGraph>> graphCallMap = new HashMap<>();
        for (CallGraph graphCall : discoveredCalls) {
            MethodReference.Handle caller = graphCall.getCallerMethod();
            if (!graphCallMap.containsKey(caller)) {
                Set<CallGraph> graphCalls = new HashSet<>();
                graphCalls.add(graphCall);
                graphCallMap.put(caller, graphCalls);
            } else {
                graphCallMap.get(caller).add(graphCall);
            }
        }
        for (MethodReference.Handle method : methodMap.keySet()) {
            if (Boolean.TRUE.equals(serializableDecider.apply(method.getClassReference()))) {
                if (method.getName().equals("finalize") && method.getDesc().equals("()V")) {
                    discoveredSources.add(new Source(method, 0));
                }
            }
        }
        for (MethodReference.Handle method : methodMap.keySet()) {
            if (Boolean.TRUE.equals(serializableDecider.apply(method.getClassReference()))) {
                if (method.getName().equals("readObject") &&
                        method.getDesc().equals("(Ljava/io/ObjectInputStream;)V")) {
                    discoveredSources.add(new Source(method, 1));
                }
            }
        }
        for (ClassReference.Handle clazz : classMap.keySet()) {
            if (Boolean.TRUE.equals(serializableDecider.apply(clazz))
                    && inheritanceMap.isSubclassOf(clazz,
                    new ClassReference.Handle("java/lang/reflect/InvocationHandler"))) {
                MethodReference.Handle method = new MethodReference.Handle(clazz, "invoke",
                        "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;");
                discoveredSources.add(new Source(method, 0));
            }
        }
        for (MethodReference.Handle method : methodMap.keySet()) {
            if (Boolean.TRUE.equals(serializableDecider.apply(method.getClassReference()))) {
                if (method.getName().equals("hashCode") && method.getDesc().equals("()I")) {
                    discoveredSources.add(new Source(method, 0));
                }
                if (method.getName().equals("equals") && method.getDesc().equals("(Ljava/lang/Object;)Z")) {
                    discoveredSources.add(new Source(method, 0));
                    discoveredSources.add(new Source(method, 1));
                }
            }
        }
        System.out.println(discoveredSources);
    }
}
