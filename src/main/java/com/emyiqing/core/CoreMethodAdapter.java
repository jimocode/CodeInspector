package com.emyiqing.core;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AnalyzerAdapter;

public class CoreMethodAdapter extends MethodVisitor {

    public CoreMethodAdapter(final int api, final MethodVisitor mv,
                             final String owner, int access, String name, String desc) {
        super(api, new AnalyzerAdapter(owner, access, name, desc, mv));
    }
}
