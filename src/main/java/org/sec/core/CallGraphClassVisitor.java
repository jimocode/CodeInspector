package org.sec.core;

import org.sec.model.ClassReference;
import org.sec.model.MethodReference;
import org.sec.decide.Decider;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.JSRInlinerAdapter;

import java.util.Map;
import java.util.Set;

@SuppressWarnings("all")
public class CallGraphClassVisitor extends ClassVisitor {
    private final Set<CallGraph> discoveredCalls;
    private final Map<ClassReference.Handle, ClassReference> classMap;
    private final InheritanceMap inheritanceMap;
    private final Map<MethodReference.Handle, Set<Integer>> passthroughDataflow;
    private final Decider decider;

    private String name;
    private String signature;
    private String superName;
    private String[] interfaces;

    public CallGraphClassVisitor(Map<ClassReference.Handle, ClassReference> classMap,
                                 InheritanceMap inheritanceMap,
                                 Map<MethodReference.Handle, Set<Integer>> passthroughDataflow,
                                 Decider decider,Set<CallGraph> discoveredCalls) {
        super(Opcodes.ASM6);
        this.classMap = classMap;
        this.inheritanceMap = inheritanceMap;
        this.passthroughDataflow = passthroughDataflow;
        this.discoveredCalls = discoveredCalls;
        this.decider = decider;
    }

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.name = name;
        this.signature = signature;
        this.superName = superName;
        this.interfaces = interfaces;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        CallGraphMethodAdapter modelGeneratorMethodVisitor = new CallGraphMethodAdapter(classMap,
                inheritanceMap, passthroughDataflow, decider, api,discoveredCalls,
                mv, this.name, access, name, desc, signature, exceptions);
        return new JSRInlinerAdapter(modelGeneratorMethodVisitor, access, name, desc, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }
}
