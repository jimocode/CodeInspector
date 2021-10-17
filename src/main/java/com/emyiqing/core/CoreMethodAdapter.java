package com.emyiqing.core;

import com.emyiqing.data.InheritanceMap;
import com.emyiqing.model.MethodReference;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AnalyzerAdapter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("all")
public class CoreMethodAdapter<T> extends MethodVisitor {
    private final InheritanceMap inheritanceMap;
    private final AnalyzerAdapter analyzerAdapter;
    private final int access;
    private final String name;
    private final String desc;
    private final String signature;
    private final String[] exceptions;

    private final Map<MethodReference.Handle, Set<Integer>> passthroughDataflow;
    private Set<Label> exceptionHandlerLabels = new HashSet<>();

    private OperandStack<T> operandStack;
    private LocalVariables<T> localVariables;

    public CoreMethodAdapter(InheritanceMap inheritanceMap,
                             Map<MethodReference.Handle, Set<Integer>> passthroughDataflow,
                             final int api, final MethodVisitor mv, final String owner, int access,
                             String name, String desc, String signature, String[] exceptions) {
        super(api, new AnalyzerAdapter(owner, access, name, desc, mv));
        this.inheritanceMap = inheritanceMap;
        this.passthroughDataflow = passthroughDataflow;
        this.analyzerAdapter = (AnalyzerAdapter) this.mv;
        this.access = access;
        this.name = name;
        this.desc = desc;
        this.signature = signature;
        this.exceptions = exceptions;
        operandStack = new OperandStack<>();
        localVariables = new LocalVariables<>();
    }

    private void sanityCheck() {
        if (analyzerAdapter.stack != null && operandStack.size() != analyzerAdapter.stack.size()) {
            throw new IllegalStateException("bad stack size");
        }
    }

    @Override
    public void visitCode() {
        super.visitCode();
        localVariables.clear();
        operandStack.clear();

        if ((this.access & Opcodes.ACC_STATIC) == 0) {
            localVariables.add(new HashSet<T>());
        }
        for (Type argType : Type.getArgumentTypes(desc)) {
            for (int i = 0; i < argType.getSize(); i++) {
                localVariables.add(new HashSet<T>());
            }
        }
    }

    @Override
    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
        int stackSize = 0;
        for (int i = 0; i < nStack; i++) {
            Object typ = stack[i];
            int objectSize = 1;
            if (typ.equals(Opcodes.LONG) || typ.equals(Opcodes.DOUBLE)) {
                objectSize = 2;
            }
            for (int j = operandStack.size(); j < stackSize + objectSize; j++) {
                operandStack.add(new HashSet<>());
            }
            stackSize += objectSize;
        }
        int localSize = 0;
        for (int i = 0; i < nLocal; i++) {
            Object typ = local[i];
            int objectSize = 1;
            if (typ.equals(Opcodes.LONG) || typ.equals(Opcodes.DOUBLE)) {
                objectSize = 2;
            }
            for (int j = localVariables.size(); j < localSize + objectSize; j++) {
                localVariables.add(new HashSet<>());
            }
            localSize += objectSize;
        }
        for (int i = operandStack.size() - stackSize; i > 0; i--) {
            operandStack.remove(operandStack.size() - 1);
        }
        for (int i = localVariables.size() - localSize; i > 0; i--) {
            localVariables.remove(localVariables.size() - 1);
        }
        super.visitFrame(type, nLocal, local, nStack, stack);
        sanityCheck();
    }

    @Override
    public void visitInsn(int opcode) {
        Set<T> saved0, saved1, saved2, saved3;
        sanityCheck();
        switch(opcode) {
            case Opcodes.NOP:
                break;
            case Opcodes.ACONST_NULL:
            case Opcodes.ICONST_M1:
            case Opcodes.ICONST_0:
            case Opcodes.ICONST_1:
            case Opcodes.ICONST_2:
            case Opcodes.ICONST_3:
            case Opcodes.ICONST_4:
            case Opcodes.ICONST_5:
            case Opcodes.FCONST_0:
            case Opcodes.FCONST_1:
            case Opcodes.FCONST_2:
                operandStack.push();
                break;
            case Opcodes.LCONST_0:
            case Opcodes.LCONST_1:
            case Opcodes.DCONST_0:
            case Opcodes.DCONST_1:
                operandStack.push();
                operandStack.push();
                break;
            case Opcodes.IALOAD:
            case Opcodes.FALOAD:
            case Opcodes.AALOAD:
            case Opcodes.BALOAD:
            case Opcodes.CALOAD:
            case Opcodes.SALOAD:
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.LALOAD:
            case Opcodes.DALOAD:
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                operandStack.push();
                break;
            case Opcodes.IASTORE:
            case Opcodes.FASTORE:
            case Opcodes.AASTORE:
            case Opcodes.BASTORE:
            case Opcodes.CASTORE:
            case Opcodes.SASTORE:
                operandStack.pop();
                operandStack.pop();
                operandStack.pop();
                break;
            case Opcodes.LASTORE:
            case Opcodes.DASTORE:
                operandStack.pop();
                operandStack.pop();
                operandStack.pop();
                operandStack.pop();
                break;
            case Opcodes.POP:
                operandStack.pop();
                break;
            case Opcodes.POP2:
                operandStack.pop();
                operandStack.pop();
                break;
            case Opcodes.DUP:
                operandStack.push(operandStack.get(0));
                break;
            case Opcodes.DUP_X1:
                saved0 = operandStack.pop();
                saved1 = operandStack.pop();
                operandStack.push(saved0);
                operandStack.push(saved1);
                operandStack.push(saved0);
                break;
            case Opcodes.DUP_X2:
                saved0 = operandStack.pop();
                saved1 = operandStack.pop();
                saved2 = operandStack.pop();
                operandStack.push(saved0);
                operandStack.push(saved2);
                operandStack.push(saved1);
                operandStack.push(saved0);
                break;
            case Opcodes.DUP2:
                operandStack.push(operandStack.get(1));
                operandStack.push(operandStack.get(1));
                break;
            case Opcodes.DUP2_X1:
                saved0 = operandStack.pop();
                saved1 = operandStack.pop();
                saved2 = operandStack.pop();
                operandStack.push(saved1);
                operandStack.push(saved0);
                operandStack.push(saved2);
                operandStack.push(saved1);
                operandStack.push(saved0);
                break;
            case Opcodes.DUP2_X2:
                saved0 = operandStack.pop();
                saved1 = operandStack.pop();
                saved2 = operandStack.pop();
                saved3 = operandStack.pop();
                operandStack.push(saved1);
                operandStack.push(saved0);
                operandStack.push(saved3);
                operandStack.push(saved2);
                operandStack.push(saved1);
                operandStack.push(saved0);
                break;
            case Opcodes.SWAP:
                saved0 = operandStack.pop();
                saved1 = operandStack.pop();
                operandStack.push(saved0);
                operandStack.push(saved1);
                break;
            case Opcodes.IADD:
            case Opcodes.FADD:
            case Opcodes.ISUB:
            case Opcodes.FSUB:
            case Opcodes.IMUL:
            case Opcodes.FMUL:
            case Opcodes.IDIV:
            case Opcodes.FDIV:
            case Opcodes.IREM:
            case Opcodes.FREM:
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.LADD:
            case Opcodes.DADD:
            case Opcodes.LSUB:
            case Opcodes.DSUB:
            case Opcodes.LMUL:
            case Opcodes.DMUL:
            case Opcodes.LDIV:
            case Opcodes.DDIV:
            case Opcodes.LREM:
            case Opcodes.DREM:
                operandStack.pop();
                operandStack.pop();
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                operandStack.push();
                break;
            case Opcodes.INEG:
            case Opcodes.FNEG:
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.LNEG:
            case Opcodes.DNEG:
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                operandStack.push();
                break;
            case Opcodes.ISHL:
            case Opcodes.ISHR:
            case Opcodes.IUSHR:
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.LSHL:
            case Opcodes.LSHR:
            case Opcodes.LUSHR:
                operandStack.pop();
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                operandStack.push();
                break;
            case Opcodes.IAND:
            case Opcodes.IOR:
            case Opcodes.IXOR:
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.LAND:
            case Opcodes.LOR:
            case Opcodes.LXOR:
                operandStack.pop();
                operandStack.pop();
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                operandStack.push();
                break;
            case Opcodes.I2B:
            case Opcodes.I2C:
            case Opcodes.I2S:
            case Opcodes.I2F:
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.I2L:
            case Opcodes.I2D:
                operandStack.pop();
                operandStack.push();
                operandStack.push();
                break;
            case Opcodes.L2I:
            case Opcodes.L2F:
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.D2L:
            case Opcodes.L2D:
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                operandStack.push();
                break;
            case Opcodes.F2I:
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.F2L:
            case Opcodes.F2D:
                operandStack.pop();
                operandStack.push();
                operandStack.push();
                break;
            case Opcodes.D2I:
            case Opcodes.D2F:
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.LCMP:
                operandStack.pop();
                operandStack.pop();
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.FCMPL:
            case Opcodes.FCMPG:
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.DCMPL:
            case Opcodes.DCMPG:
                operandStack.pop();
                operandStack.pop();
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.IRETURN:
            case Opcodes.FRETURN:
            case Opcodes.ARETURN:
                operandStack.pop();
                break;
            case Opcodes.LRETURN:
            case Opcodes.DRETURN:
                operandStack.pop();
                operandStack.pop();
                break;
            case Opcodes.RETURN:
                break;
            case Opcodes.ARRAYLENGTH:
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.ATHROW:
                operandStack.pop();
                break;
            case Opcodes.MONITORENTER:
            case Opcodes.MONITOREXIT:
                operandStack.pop();
                break;
            default:
                throw new IllegalStateException("unsupported opcode: " + opcode);
        }
        super.visitInsn(opcode);
        sanityCheck();
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        switch(opcode) {
            case Opcodes.BIPUSH:
            case Opcodes.SIPUSH:
                operandStack.push();
                break;
            case Opcodes.NEWARRAY:
                operandStack.pop();
                operandStack.push();
                break;
            default:
                throw new IllegalStateException("unsupported opcode: " + opcode);
        }
        super.visitIntInsn(opcode, operand);
        sanityCheck();
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        for (int i = localVariables.size(); i <= var; i++) {
            localVariables.add(new HashSet<T>());
        }
        Set<T> saved0;
        switch(opcode) {
            case Opcodes.ILOAD:
            case Opcodes.FLOAD:
                operandStack.push();
                break;
            case Opcodes.LLOAD:
            case Opcodes.DLOAD:
                operandStack.push();
                operandStack.push();
                break;
            case Opcodes.ALOAD:
                operandStack.push(localVariables.get(var));
                break;
            case Opcodes.ISTORE:
            case Opcodes.FSTORE:
                operandStack.pop();
                localVariables.set(var, new HashSet<T>());
                break;
            case Opcodes.DSTORE:
            case Opcodes.LSTORE:
                operandStack.pop();
                operandStack.pop();
                localVariables.set(var, new HashSet<T>());
                break;
            case Opcodes.ASTORE:
                saved0 = operandStack.pop();
                localVariables.set(var, saved0);
                break;
            case Opcodes.RET:
                break;
            default:
                throw new IllegalStateException("unsupported opcode: " + opcode);
        }
        super.visitVarInsn(opcode, var);
        sanityCheck();
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        switch(opcode) {
            case Opcodes.NEW:
                operandStack.push();
                break;
            case Opcodes.ANEWARRAY:
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.CHECKCAST:
                break;
            case Opcodes.INSTANCEOF:
                operandStack.pop();
                operandStack.push();
                break;
            default:
                throw new IllegalStateException("unsupported opcode: " + opcode);
        }
        super.visitTypeInsn(opcode, type);
        sanityCheck();
    }
}
