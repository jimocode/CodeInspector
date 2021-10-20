package org.sec;

import com.beust.jcommander.JCommander;
import org.sec.config.Command;
import org.sec.config.Logo;
import org.sec.decide.Decider;
import org.sec.decide.SimpleSerializableDecider;
import org.sec.model.ClassFile;
import org.sec.model.ClassReference;
import org.sec.model.MethodReference;
import org.sec.util.RtUtil;
import org.apache.log4j.Logger;
import org.sec.core.CallGraph;
import org.sec.core.InheritanceMap;
import org.sec.core.Source;
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
    // 类名->类资源
    private static final Map<String, ClassFile> classFileByName = new HashMap<>();
    // 所有的入口（漏洞触发点）
    private static final List<Source> discoveredSources = new ArrayList<>();
    // 方法->方法对象
    private static final Map<MethodReference.Handle, MethodReference> methodMap = new HashMap<>();
    // 方法->调用关系集合
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
        if (command.jars != null && command.jars.size() != 0) {
            start(command.jars);
        }
    }

    private static void start(List<String> jars) {
        // 读取JDK和输入Jar所有class资源
        List<ClassFile> classFileList = RtUtil.getAllClassesFromJars(jars);
        // 获取所有方法和类
        DiscoveryService.start(classFileList, discoveredClasses, discoveredMethods);
        // 根据已有方法和类得到继承关系
        InheritanceMap inheritanceMap = InheritanceService.start(discoveredClasses, classMap);
        // 得到方法中的方法调用
        MethodCallService.start(classFileList, methodCalls, classFileByName);
        // 对方法进行拓扑逆排序
        List<MethodReference.Handle> sortedMethods = SortService.start(methodCalls);
        // 设置决策者
        Decider decider = new SimpleSerializableDecider(inheritanceMap);
        // 分析方法返回值与哪些参数有关
        DataFlowService.start(inheritanceMap, sortedMethods,
                classFileByName, classMap, dataFlow, decider);
        // 根据已有条件得到方法调用关系
        CallGraphService.start(inheritanceMap, discoveredCalls,
                sortedMethods, classFileByName, classMap, dataFlow, decider);
        // 根据已有条件得到入口信息
        SourceService.start(inheritanceMap, methodMap, discoveredCalls, graphCallMap,
                discoveredMethods, classMap, discoveredSources, decider);
        // 最终构造利用链
        GadgetChainService.start(inheritanceMap, methodMap);
    }
}
