package org.sec.core;

import org.sec.model.ClassReference;
import org.sec.model.MethodReference;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.JSRInlinerAdapter;

import java.util.Map;
import java.util.Set;

@SuppressWarnings("all")
public class DataFlowClassVisitor extends ClassVisitor {
    Map<ClassReference.Handle, ClassReference> classMap;
    private final MethodReference.Handle methodToVisit;
    private final InheritanceMap inheritanceMap;
    private final Map<MethodReference.Handle, Set<Integer>> passthroughDataflow;

    private String name;
    private DataFlowMethodAdapter dataFlowMethodAdapter;

    public DataFlowClassVisitor(Map<ClassReference.Handle, ClassReference> classMap,
                                InheritanceMap inheritanceMap,
                                Map<MethodReference.Handle, Set<Integer>> passthroughDataflow,
                                MethodReference.Handle methodToVisit) {
        super(Opcodes.ASM6);
        this.classMap = classMap;
        this.inheritanceMap = inheritanceMap;
        this.methodToVisit = methodToVisit;
        this.passthroughDataflow = passthroughDataflow;
    }

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.name = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        if (!name.equals(methodToVisit.getName()) || !desc.equals(methodToVisit.getDesc())) {
            return null;
        }
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        dataFlowMethodAdapter = new DataFlowMethodAdapter(classMap, inheritanceMap,
                this.passthroughDataflow, mv, this.name, access, name, desc, signature, exceptions);
        return new JSRInlinerAdapter(dataFlowMethodAdapter, access, name, desc, signature, exceptions);
    }

    public Set<Integer> getReturnTaint() {
        if (dataFlowMethodAdapter == null) {
            return null;
        } else {
            return dataFlowMethodAdapter.getReturnTaint();
        }
    }
}
