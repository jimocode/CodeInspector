package org.sec;

import com.beust.jcommander.JCommander;
import org.sec.config.Command;
import org.sec.config.Logo;
import org.sec.core.InheritanceUtil;
import org.sec.model.ClassFile;
import org.sec.model.ClassReference;
import org.sec.model.MethodReference;
import org.sec.util.DataUtil;
import org.sec.util.DrawUtil;
import org.sec.util.FileUtil;
import org.sec.util.RtUtil;
import org.apache.log4j.Logger;
import org.sec.core.CallGraph;
import org.sec.core.InheritanceMap;
import org.sec.service.*;

import javax.xml.crypto.Data;
import java.util.*;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);
    // 所有类
    private static final List<ClassReference> discoveredClasses = new ArrayList<>();
    // 所有方法
    private static final List<MethodReference> discoveredMethods = new ArrayList<>();
    // 所有方法内的方法调用
    private static final Map<MethodReference.Handle, Set<MethodReference.Handle>> methodCalls = new HashMap<>();
    // 方法返回值与哪些参数有关
    private static final Map<MethodReference.Handle, Set<Integer>> dataFlow = new HashMap<>();
    // 所有的方法调用关系
    private static final Set<CallGraph> discoveredCalls = new HashSet<>();
    // 类名->类对象
    private static final Map<ClassReference.Handle, ClassReference> classMap = new HashMap<>();
    // 方法名->方法对象
    private static final Map<MethodReference.Handle, MethodReference> methodMap = new HashMap<>();
    // 类名->类资源
    private static final Map<String, ClassFile> classFileByName = new HashMap<>();

    public static void main(String[] args) {
        Logo.PrintLogo();
        logger.info("start code inspector");
        Command command = new Command();
        JCommander jc = JCommander.newBuilder().addObject(command).build();
        jc.parse(args);
        if (command.help) {
            jc.usage();
        }
        if (command.boots != null && command.boots.size() != 0) {
            start(command.boots, command.packageName);
        }
    }

    private static void start(List<String> boots, String packageName) {
        // 读取JDK和输入Jar所有class资源
        List<ClassFile> classFileList = RtUtil.getAllClassesFromBoot(boots);
        // 获取所有方法和类
        DiscoveryService.start(classFileList, discoveredClasses, discoveredMethods);
        // 根据已有方法和类得到继承关系
        InheritanceMap inheritanceMap = InheritanceService.start(discoveredClasses, discoveredMethods,
                classMap, methodMap);
        // 得到方法中的方法调用
        MethodCallService.start(classFileList, methodCalls, classFileByName);
        // 对方法进行拓扑逆排序
        List<MethodReference.Handle> sortedMethods = SortService.start(methodCalls);
        // 包名
        String finalPackageName = packageName.replace(".", "/");
        // 分析方法返回值与哪些参数有关
        DataFlowService.start(inheritanceMap, sortedMethods, classFileByName, classMap, dataFlow);
        DataUtil.SaveDataFlows(dataFlow,methodMap);
        // 根据已有条件得到方法调用关系
        CallGraphService.start(inheritanceMap, discoveredCalls, sortedMethods, classFileByName, classMap, dataFlow);
        DataUtil.SaveCallGraphs(discoveredCalls);

        Map<ClassReference.Handle, Set<MethodReference.Handle>> methodsByClass = InheritanceUtil
                .getMethodsByClass(methodMap);
        Map<MethodReference.Handle, Set<MethodReference.Handle>> methodImplMap = InheritanceUtil
                .getAllMethodImplementations(inheritanceMap, methodMap);

        Set<CallGraph> targetCallGraphs = new HashSet<>();
        for (CallGraph callGraph : discoveredCalls) {
            ClassReference callerClass = classMap.get(callGraph.getCallerMethod().getClassReference());
            ClassReference targetClass = classMap.get(callGraph.getTargetMethod().getClassReference());
            if (targetClass == null) {
                continue;
            }
            if (targetClass.getName().equals("java/lang/Object") &&
                    callGraph.getTargetMethod().getName().equals("<init>") &&
                    callGraph.getTargetMethod().getDesc().equals("()V")) {
                continue;
            }
            if (callerClass.getName().startsWith(finalPackageName)) {
                targetCallGraphs.add(callGraph);
                if (targetClass.isInterface()) {
                    for (MethodReference.Handle handle : methodImplMap.get(callGraph.getTargetMethod())) {
                        targetCallGraphs.add(new CallGraph(callGraph.getTargetMethod(), handle));
                    }
                }
            }
        }
        DrawUtil.drawCallGraph(targetCallGraphs);
    }
}
