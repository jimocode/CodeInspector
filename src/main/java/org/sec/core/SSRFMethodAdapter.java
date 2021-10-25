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
    private int methodArgIndex;
    private List<Boolean> pass;

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
    public void visitInsn(int opcode) {
        if (opcode == Opcodes.ARETURN) {
            if (operandStack.get(0).contains(true)) {

            }
        }
        super.visitInsn(opcode);
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
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (opcode == Opcodes.INVOKESTATIC) {
            if ((owner.equals("org/apache/commons/lang/StringEscapeUtils") &&
                    (name.equals("escapeHtml") || name.equals("escapeJavaScript"))) ||
                    (owner.equals("org/owasp/esapi/ESAPI") && name.equals("encodeForHTML"))) {
                Type[] argTypes = Type.getArgumentTypes(desc);
                int stackIndex = 0;
                for (int i = 0; i < argTypes.length; i++) {
                    int argIndex = argTypes.length - 1 - i;
                    Type type = argTypes[argIndex];
                    Set<Boolean> taint = operandStack.get(stackIndex);
                    if (taint.size() > 0) {
                        for (boolean arg : taint) {
                            if (arg) {
                                operandStack.set(stackIndex, false);
                            }
                        }
                    }
                    stackIndex += type.getSize();
                }
            }
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }
}
