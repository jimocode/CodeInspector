package org.sec.core;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.sec.model.ClassReference;
import org.sec.model.MethodReference;

import java.util.Map;
import java.util.Set;

public class ReflectionXssMethodAdapter extends CoreMethodAdapter<String>{
    private final int access;
    private final String desc;

    public ReflectionXssMethodAdapter(InheritanceMap inheritanceMap,
                                      Map<MethodReference.Handle, Set<Integer>> passthroughDataflow,
                                      int api, MethodVisitor mv, String owner, int access, String name,
                                      String desc, String signature, String[] exceptions) {
        super(inheritanceMap, passthroughDataflow, api, mv, owner, access, name, desc, signature, exceptions);
        this.access = access;
        this.desc = desc;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        int localIndex = 0;
        int argIndex = 0;
        if ((this.access & Opcodes.ACC_STATIC) == 0) {
            localIndex += 1;
            argIndex += 1;
        }
        for (Type argType : Type.getArgumentTypes(desc)) {
            if(argType.getClassName().equals("java.lang.String")){
                localVariables.set(localIndex, "xss" + argIndex);
            }
            localIndex += argType.getSize();
            argIndex += 1;
        }
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        Type[] argTypes = Type.getArgumentTypes(desc);
        if (opcode != Opcodes.INVOKESTATIC) {
            Type[] extendedArgTypes = new Type[argTypes.length + 1];
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
                    int argIndex = argTypes.length - 1 - i;
                    Type type = argTypes[argIndex];
                    Set<String> taint = operandStack.get(stackIndex);
                    if (taint.size() > 0) {
                        for (String argSrc : taint) {
                            if (argSrc.startsWith("xss")) {

                            }
                            int srcArgIndex = Integer.parseInt(argSrc.substring(3));
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
