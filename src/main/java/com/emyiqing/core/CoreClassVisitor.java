package com.emyiqing.core;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.JSRInlinerAdapter;

import java.util.HashSet;
import java.util.Set;

public class CoreClassVisitor extends ClassVisitor {
    private String name;
    private String signature;
    private String superName;
    private String[] interfaces;
    private Set<String> annotations;

    public CoreClassVisitor(int api) {
        super(api);
    }

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.name = name;
        this.signature = signature;
        this.superName = superName;
        this.annotations = new HashSet<>();
        this.interfaces = interfaces;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        annotations.add(descriptor);
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        CoreMethodAdapter cmv = new CoreMethodAdapter(this.api, mv, name, access, name, desc);
        return new JSRInlinerAdapter(cmv,access,name,desc,signature,exceptions);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }
}
