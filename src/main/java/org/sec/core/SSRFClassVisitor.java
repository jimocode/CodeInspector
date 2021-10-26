package org.sec.core;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.sec.model.MethodReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("all")
public class SSRFClassVisitor extends ClassVisitor {
    private final InheritanceMap inheritanceMap;
    private final Map<MethodReference.Handle, Set<Integer>> dataFlow;

    private String name;
    private String signature;
    private String superName;
    private String[] interfaces;

    private MethodReference.Handle methodHandle;
    private int methodArgIndex;
    private List<Boolean> pass;

    public SSRFClassVisitor(MethodReference.Handle targetMethod,
                            int targetIndex, InheritanceMap inheritanceMap,
                            Map<MethodReference.Handle, Set<Integer>> dataFlow) {
        super(Opcodes.ASM6);
        this.inheritanceMap = inheritanceMap;
        this.dataFlow = dataFlow;
        this.methodHandle = targetMethod;
        this.methodArgIndex = targetIndex;
        this.pass = new ArrayList<>();
    }

    public List<Boolean> getPass() {
        return pass;
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
    public MethodVisitor visitMethod(int access, String name, String descriptor,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (name.equals(this.methodHandle.getName())) {
            SSRFMethodAdapter ssrfMethodAdapter = new SSRFMethodAdapter(
                    this.methodArgIndex, this.pass,
                    inheritanceMap, dataFlow, Opcodes.ASM6, mv,
                    this.name, access, name, descriptor, signature, exceptions
            );
            return new JSRInlinerAdapter(ssrfMethodAdapter,
                    access, name, descriptor, signature, exceptions);
        }
        return mv;
    }
}
