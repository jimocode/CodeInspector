package org.sec.core;

import org.sec.model.ClassReference;
import org.sec.model.MethodReference;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.*;

@SuppressWarnings("all")
public class DataFlowMethodAdapter extends CoreMethodAdapter<Integer> {

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
        super.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        Type[] argTypes = Type.getArgumentTypes(desc);
        if (opcode != Opcodes.INVOKESTATIC) {
            Type[] extendedArgTypes = new Type[argTypes.length+1];
            System.arraycopy(argTypes, 0, extendedArgTypes, 1, argTypes.length);
            extendedArgTypes[0] = Type.getObjectType(owner);
            argTypes = extendedArgTypes;
        }
        int retSize = Type.getReturnType(desc).getSize();
        Set<Integer> resultTaint;
        switch (opcode) {
            case Opcodes.INVOKESTATIC:
            case Opcodes.INVOKEVIRTUAL:
            case Opcodes.INVOKESPECIAL:
            case Opcodes.INVOKEINTERFACE:
                final List<Set<Integer>> argTaint = new ArrayList<>(argTypes.length);
                for (int i = 0; i < argTypes.length; i++) {
                    argTaint.add(null);
                }
                int stackIndex = 0;
                for (int i = 0; i < argTypes.length; i++) {
                    Type argType = argTypes[i];
                    if (argType.getSize() > 0) {
                        argTaint.set(argTypes.length - 1 - i,
                                operandStack.get(stackIndex+argType.getSize()-1));
                    }
                    stackIndex += argType.getSize();
                }
                if (name.equals("<init>")) {
                    resultTaint = argTaint.get(0);
                } else {
                    resultTaint = new HashSet<>();
                }
                Set<Integer> passthrough = passthroughDataflow.get(
                        new MethodReference.Handle(new ClassReference.Handle(owner), name, desc));
                if (passthrough != null) {
                    for (Integer passthroughDataflowArg : passthrough) {
                        resultTaint.addAll(argTaint.get(passthroughDataflowArg));
                    }
                }
                break;
            default:
                throw new IllegalStateException("unsupported opcode: " + opcode);
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
        if (retSize > 0) {
            operandStack.get(retSize-1).addAll(resultTaint);
        }
    }

    public Set<Integer> getReturnTaint() {
        return returnTaint;
    }
}
