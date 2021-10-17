package com.emyiqing.core;

import com.emyiqing.data.InheritanceMap;
import com.emyiqing.model.ClassReference;
import com.emyiqing.model.MethodReference;
import com.emyiqing.service.Decider;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataFlowMethodAdapter extends CoreMethodAdapter<Integer> {

    private final Map<ClassReference.Handle, ClassReference> classMap;
    private final InheritanceMap inheritanceMap;
    private final Decider decider;
    private final Map<MethodReference.Handle, Set<Integer>> passthroughDataflow;

    private final int access;
    private final String desc;
    private final Set<Integer> returnTaint;

    public DataFlowMethodAdapter(Map<ClassReference.Handle, ClassReference> classMap,
                                 InheritanceMap inheritanceMap, Decider decider,
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
        this.decider = decider;
        returnTaint = new HashSet<>();
    }

    private static boolean couldBeSerialized(Decider decider, InheritanceMap inheritanceMap,
                                               ClassReference.Handle clazz) {
        if (Boolean.TRUE.equals(decider.apply(clazz))) {
            return true;
        }
        Set<ClassReference.Handle> subClasses = inheritanceMap.getSubClasses(clazz);
        if (subClasses != null) {
            for (ClassReference.Handle subClass : subClasses) {
                if (Boolean.TRUE.equals(decider.apply(subClass))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        int localIndex = 0;
        int argIndex = 0;
        if ((this.access & Opcodes.ACC_STATIC) == 0) {
            localVariables.set(localIndex, argIndex);
            localIndex += 1;
            argIndex += 1;
        }
        for (Type argType : Type.getArgumentTypes(desc)) {
            localVariables.set(localIndex, argIndex);
            localIndex += argType.getSize();
            argIndex += 1;
        }
    }

    @Override
    public void visitInsn(int opcode) {
        switch (opcode) {
            case Opcodes.IRETURN:
            case Opcodes.FRETURN:
            case Opcodes.ARETURN:
                returnTaint.addAll(operandStack.get(0));
                break;
            case Opcodes.LRETURN:
            case Opcodes.DRETURN:
                returnTaint.addAll(operandStack.get(1));
                break;
            case Opcodes.RETURN:
                break;
            default:
                break;
        }
        super.visitInsn(opcode);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {

    }

    public Set<Integer> getReturnTaint() {
        return returnTaint;
    }
}
