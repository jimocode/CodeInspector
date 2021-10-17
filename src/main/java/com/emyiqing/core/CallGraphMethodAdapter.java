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

public class CallGraphMethodAdapter extends CoreMethodAdapter<String> {
    private final Map<ClassReference.Handle, ClassReference> classMap;
    private final Set<CallGraph> discoveredCalls;
    private final InheritanceMap inheritanceMap;
    private final Decider serializableDecider;
    private final String owner;
    private final int access;
    private final String name;
    private final String desc;

    public CallGraphMethodAdapter(Map<ClassReference.Handle, ClassReference> classMap,
                                  InheritanceMap inheritanceMap,
                                  Map<MethodReference.Handle, Set<Integer>> passthroughDataflow,
                                  Decider serializableDecider, final int api,Set<CallGraph> discoveredCalls,
                                  final MethodVisitor mv, final String owner, int access, String name, String desc,
                                  String signature, String[] exceptions) {
        super(inheritanceMap, passthroughDataflow, api, mv, owner, access, name, desc, signature, exceptions);
        this.classMap = classMap;
        this.inheritanceMap = inheritanceMap;
        this.serializableDecider = serializableDecider;
        this.owner = owner;
        this.access = access;
        this.name = name;
        this.desc = desc;
        this.discoveredCalls = discoveredCalls;
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
            localVariables.set(localIndex, "arg" + argIndex);
            localIndex += 1;
            argIndex += 1;
        }
        for (Type argType : Type.getArgumentTypes(desc)) {
            localVariables.set(localIndex, "arg" + argIndex);
            localIndex += argType.getSize();
            argIndex += 1;
        }
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        switch (opcode) {
            case Opcodes.GETSTATIC:
                break;
            case Opcodes.PUTSTATIC:
                break;
            case Opcodes.GETFIELD:
                Type type = Type.getType(desc);
                if (type.getSize() != 1) {
                    break;
                }
                Boolean isTransient = null;
                if (!couldBeSerialized(serializableDecider, inheritanceMap,
                        new ClassReference.Handle(type.getInternalName()))) {
                    isTransient = Boolean.TRUE;
                } else {
                    ClassReference clazz = classMap.get(new ClassReference.Handle(owner));
                    while (clazz != null) {
                        for (ClassReference.Member member : clazz.getMembers()) {
                            if (member.getName().equals(name)) {
                                isTransient = (member.getModifiers() & Opcodes.ACC_TRANSIENT) != 0;
                                break;
                            }
                        }
                        if (isTransient != null) {
                            break;
                        }
                        clazz = classMap.get(new ClassReference.Handle(clazz.getSuperClass()));
                    }
                }
                Set<String> newTaint = new HashSet<>();
                if (!Boolean.TRUE.equals(isTransient)) {
                    for (String s : operandStack.get(0)) {
                        newTaint.add(s + "." + name);
                    }
                }
                super.visitFieldInsn(opcode, owner, name, desc);
                operandStack.set(0, newTaint);
                return;
            case Opcodes.PUTFIELD:
                break;
            default:
                throw new IllegalStateException("unsupported opcode: " + opcode);
        }
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
        switch (opcode) {
            case Opcodes.INVOKESTATIC:
            case Opcodes.INVOKEVIRTUAL:
            case Opcodes.INVOKESPECIAL:
            case Opcodes.INVOKEINTERFACE:
                int stackIndex = 0;
                for (int i = 0; i < argTypes.length; i++) {
                    int argIndex = argTypes.length-1-i;
                    Type type = argTypes[argIndex];
                    Set<String> taint = operandStack.get(stackIndex);
                    if (taint.size() > 0) {
                        for (String argSrc : taint) {
                            if (!argSrc.startsWith("arg")) {
                                throw new IllegalStateException("invalid taint arg: " + argSrc);
                            }
                            int dotIndex = argSrc.indexOf('.');
                            int srcArgIndex;
                            String srcArgPath;
                            if (dotIndex == -1) {
                                srcArgIndex = Integer.parseInt(argSrc.substring(3));
                                srcArgPath = null;
                            } else {
                                srcArgIndex = Integer.parseInt(argSrc.substring(3, dotIndex));
                                srcArgPath = argSrc.substring(dotIndex+1);
                            }
                            discoveredCalls.add(new CallGraph(
                                    new MethodReference.Handle(
                                            new ClassReference.Handle(this.owner), this.name, this.desc),
                                    new MethodReference.Handle(
                                            new ClassReference.Handle(owner), name, desc),
                                    srcArgIndex,
                                    srcArgPath,
                                    argIndex));
                        }
                    }
                    stackIndex += type.getSize();
                }
                break;
            default:
                throw new IllegalStateException("unsupported opcode: " + opcode);
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }
}
