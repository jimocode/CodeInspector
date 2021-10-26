package org.sec.core;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.sec.model.MethodReference;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class SSRFMethodAdapter extends CoreMethodAdapter<Boolean> {
    private final int access;
    private final String desc;
    private final int methodArgIndex;
    private final List<Boolean> pass;

    public SSRFMethodAdapter(int methodArgIndex, List<Boolean> pass, InheritanceMap inheritanceMap,
                             Map<MethodReference.Handle, Set<Integer>> passthroughDataflow,
                             int api, MethodVisitor mv, String owner, int access, String name,
                             String desc, String signature, String[] exceptions) {
        super(inheritanceMap, passthroughDataflow, api, mv, owner, access, name, desc, signature, exceptions);
        this.access = access;
        this.desc = desc;
        this.methodArgIndex = methodArgIndex;
        this.pass = pass;
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
            if (argIndex == this.methodArgIndex) {
                localVariables.set(localIndex, true);
            }
            localIndex += argType.getSize();
            argIndex += 1;
        }
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        super.visitVarInsn(opcode, var);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        boolean urlCondition = owner.equals("java/net/URL") && name.equals("<init>") &&
                desc.equals("(Ljava/lang/String;)V");
        boolean urlOpenCondition = owner.equals("java/net/URL") && name.equals("openConnection") &&
                desc.equals("()Ljava/net/URLConnection;");
        boolean urlInputCondition = owner.equals("java/net/HttpURLConnection") &&
                name.equals("getInputStream") && desc.equals("()Ljava/io/InputStream;");

        boolean isTaint = false;
        Type[] argTypes = Type.getArgumentTypes(desc);

        if (urlCondition) {
            int stackIndex = 0;
            for (int i = 0; i < argTypes.length; i++) {
                int argIndex = argTypes.length - 1 - i;
                Type type = argTypes[argIndex];
                Set<Boolean> taint = operandStack.get(stackIndex);
                if (taint.size() > 0 && taint.contains(true)) {
                    isTaint = true;
                    pass.add(true);
                    break;
                }
                stackIndex += type.getSize();
            }
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            if (isTaint) {
                operandStack.set(0, true);
            }
            return;
        }
        if (urlOpenCondition) {
            if (operandStack.get(0).contains(true)) {
                pass.add(true);
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                operandStack.set(0, true);
                return;
            }
        }
        if (urlInputCondition) {
            if (operandStack.get(0).contains(true)) {
                pass.add(true);
                return;
            }
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }
}
