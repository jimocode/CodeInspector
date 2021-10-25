package org.sec;

import com.beust.jcommander.JCommander;
import org.sec.config.Command;
import org.sec.config.Logo;
import org.sec.core.InheritanceUtil;
import org.sec.model.*;
import org.sec.util.DataUtil;
import org.sec.util.RtUtil;
import org.apache.log4j.Logger;
import org.sec.core.CallGraph;
import org.sec.core.InheritanceMap;
import org.sec.service.*;

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
    // 方法名->方法调用关系
    private static final Map<MethodReference.Handle, Set<CallGraph>> graphCallMap = new HashMap<>();

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
        List<ClassFile> classFileList = RtUtil.getAllClassesFromBoot(boots, true);
        // 获取所有方法和类
        DiscoveryService.start(classFileList, discoveredClasses, discoveredMethods);
        // 根据已有方法和类得到继承关系
        InheritanceMap inheritanceMap = InheritanceService.start(discoveredClasses, discoveredMethods,
                classMap, methodMap);
        // 包名
        String finalPackageName = packageName.replace(".", "/");
        // 获取全部controller
        List<SpringController> controllers = new ArrayList<>();
        SpringService.start(classFileList, finalPackageName, controllers, classMap, methodMap);
        // 得到方法中的方法调用
        MethodCallService.start(classFileList, methodCalls, classFileByName);
        // 对方法进行拓扑逆排序
        List<MethodReference.Handle> sortedMethods = SortService.start(methodCalls);
        // 分析方法返回值与哪些参数有关
        DataFlowService.start(inheritanceMap, sortedMethods, classFileByName, classMap, dataFlow);
        DataUtil.SaveDataFlows(dataFlow, methodMap);
        // 根据已有条件得到方法调用关系
        CallGraphService.start(inheritanceMap, discoveredCalls, sortedMethods, classFileByName,
                classMap, dataFlow, graphCallMap, methodMap);
        DataUtil.SaveCallGraphs(discoveredCalls);
        Map<MethodReference.Handle, Set<MethodReference.Handle>> methodImplMap = InheritanceUtil
                .getAllMethodImplementations(inheritanceMap, methodMap);
        SSRFService.start(classFileByName, controllers, inheritanceMap,
                dataFlow, graphCallMap, methodMap);
        // 画出指定package的调用图
        DrawService.start(discoveredCalls, finalPackageName, classMap, methodImplMap);
    }
}
