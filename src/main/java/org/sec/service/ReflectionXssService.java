package org.sec.service;

import org.sec.core.CallGraph;
import org.sec.model.MethodReference;
import org.sec.model.SpringController;
import org.sec.model.SpringMapping;
import org.sec.model.SpringParam;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReflectionXssService {
    public static void start(List<SpringController> controllers,
                             Map<MethodReference.Handle, Set<CallGraph>> graphCallMap,
                             Map<MethodReference.Handle, MethodReference> methodMap) {
        // 所有controller
        for (SpringController controller : controllers) {
            // 所有controller中含有mapping的方法
            for (SpringMapping mapping : controller.getMappings()) {
                MethodReference.Handle handle = mapping.getMethodName();
                // mapping的参数
                List<SpringParam> params = mapping.getParamMap();
                Set<CallGraph> calls;
                List<String> input;
                while ((calls = graphCallMap.get(handle)) != null) {
                    // 没有后续调用结束
                    if (calls.size() == 0) {
                        break;
                    }
                    boolean callerIsStatic;
                    boolean targetIsStatic;
                    callerIsStatic = methodMap.get(handle).isStatic();
                    for (CallGraph callGraph : calls) {
                        int callerIndex = callGraph.getCallerArgIndex();
                        if(!callerIsStatic){
                            callerIndex++;
                        }
                        SpringParam param = params.get(callerIndex);
                        // xss must use string
                        if (!param.getParamType().equals("java.lang.String")) {
                            continue;
                        }
                        MethodReference.Handle targetMethod = callGraph.getTargetMethod();
                        int targetIndex = callGraph.getTargetArgIndex();
                        if (methodMap.get(targetMethod).isStatic()) {

                        } else {

                        }
                    }
                }
            }
        }
    }
}
