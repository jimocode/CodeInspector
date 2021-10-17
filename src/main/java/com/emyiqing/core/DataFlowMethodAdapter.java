package com.emyiqing.core;

import com.emyiqing.data.InheritanceMap;
import com.emyiqing.model.ClassReference;
import com.emyiqing.model.MethodReference;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AnalyzerAdapter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataFlowMethodAdapter extends CoreMethodAdapter {

    private final Map<ClassReference.Handle, ClassReference> classMap;
    private final InheritanceMap inheritanceMap;
    private final Map<MethodReference.Handle, Set<Integer>> passthroughDataflow;

    private final int access;
    private final String desc;
    private final Set<Integer> returnTaint;

    public DataFlowMethodAdapter(Map<ClassReference.Handle, ClassReference> classMap,
                                 InheritanceMap inheritanceMap,
                                 Map<MethodReference.Handle, Set<Integer>> passthroughDataflow,
                                 MethodVisitor mv, String owner, int access, String name,
                                 String desc, String signature, String[] exceptions) {
        super(inheritanceMap, passthroughDataflow,
                Opcodes.ASM6, mv, owner, access, name, desc, signature, exceptions);
        this.classMap = classMap;
        this.inheritanceMap = inheritanceMap;
        this.passthroughDataflow = passthroughDataflow;
        this.access = access;
        this.desc = desc;
        returnTaint = new HashSet<>();
    }

    public Set<Integer> getReturnTaint() {
        return returnTaint;
    }
}
