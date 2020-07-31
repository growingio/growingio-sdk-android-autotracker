/*
 * Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.growingio.sdk.plugin.autotrack.compile.visitor;

import com.growingio.sdk.plugin.autotrack.compile.Context;
import com.growingio.sdk.plugin.autotrack.compile.Log;
import com.growingio.sdk.plugin.autotrack.hook.HookClassesConfig;
import com.growingio.sdk.plugin.autotrack.hook.InjectMethod;
import com.growingio.sdk.plugin.autotrack.hook.TargetClass;
import com.growingio.sdk.plugin.autotrack.hook.TargetMethod;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

public class InjectSuperClassVisitor extends ClassVisitor {
    private final Context mContext;
    private final Log mLog;
    private final List<TargetClass> mTargetClasses = new ArrayList<>();
    private final Set<TargetMethod> mOverrideMethods = new HashSet<>();
    private String mCurrentClass;

    public InjectSuperClassVisitor(ClassVisitor classVisitor, Context context) {
        super(context.getASMVersion(), classVisitor);
        mContext = context;
        mLog = context.getLog();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        mCurrentClass = name;
        TargetClass targetClass = HookClassesConfig.getSuperHookClasses().get(superName);
        if (targetClass != null) {
            mTargetClasses.add(targetClass);
        }
        for (String i : interfaces) {
            TargetClass targetInterface = HookClassesConfig.getSuperHookClasses().get(i);
            if (targetInterface != null) {
                mTargetClasses.add(targetInterface);
            }
        }

        if (!mTargetClasses.isEmpty()) {
            mContext.markModified();
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        for (TargetClass targetClass : mTargetClasses) {
            TargetMethod targetMethod = targetClass.getTargetMethod(name, desc);
            if (targetMethod != null) {
                mOverrideMethods.add(targetMethod);
                return new InjectSuperMethodVisitor(mv, access, name, desc, targetMethod.getInjectMethods());
            }
        }
        return mv;
    }

    @Override
    public void visitEnd() {
        for (TargetClass targetClass : mTargetClasses) {
            for (TargetMethod targetMethod : targetClass.getTargetMethods()) {
                if (!mOverrideMethods.contains(targetMethod)) {
                    Set<InjectMethod> injectMethods = targetMethod.getInjectMethods();
                    Method m = new Method(targetMethod.getName(), targetMethod.getDesc());
                    GeneratorAdapter mg = new GeneratorAdapter(ACC_PUBLIC, m, null, null, cv);
                    for (InjectMethod injectMethod : injectMethods) {
                        if (!injectMethod.isAfter()) {
                            mg.loadThis();
                            mg.loadArgs();
                            mg.invokeStatic(Type.getType(injectMethod.getClassName()), new Method(injectMethod.getMethodName(), injectMethod.getMethodDesc()));
                            mLog.debug("Method Add: " + injectMethod.getClassName() + "#" + injectMethod.getMethodName() + injectMethod.getMethodDesc() + " ===SuperBefore===> " + mCurrentClass + "#" + targetMethod.getName() + targetMethod.getDesc());
                        }
                    }
                    mg.loadThis();
                    mg.loadArgs();
                    mg.invokeConstructor(Type.getType(targetClass.getName()), new Method(targetMethod.getName(), targetMethod.getDesc()));
                    for (InjectMethod injectMethod : injectMethods) {
                        if (injectMethod.isAfter()) {
                            mg.loadThis();
                            mg.loadArgs();
                            mg.invokeStatic(Type.getType(injectMethod.getClassName()), new Method(injectMethod.getMethodName(), injectMethod.getMethodDesc()));
                            mLog.debug("Method Add: " + injectMethod.getClassName() + "#" + injectMethod.getMethodName() + injectMethod.getMethodDesc() + " ===SuperAfter===> " + mCurrentClass + "#" + targetMethod.getName() + targetMethod.getDesc());
                        }
                    }
                    mg.returnValue();
                    mg.endMethod();
                }
            }
        }
        super.visitEnd();
    }

    private final class InjectSuperMethodVisitor extends AdviceAdapter {
        private final String mTargetMethodName;
        private final String mTargetMethodDesc;
        private final Set<InjectMethod> mInjectMethods;

        protected InjectSuperMethodVisitor(MethodVisitor mv, int access, String name, String desc, Set<InjectMethod> injectMethods) {
            super(ASM5, mv, access, name, desc);
            mTargetMethodName = name;
            mTargetMethodDesc = desc;
            mInjectMethods = injectMethods;
        }

        @Override
        protected void onMethodEnter() {
            super.onMethodEnter();
            for (InjectMethod injectMethod : mInjectMethods) {
                if (!injectMethod.isAfter()) {
                    loadThis();
                    loadArgs();
                    invokeStatic(Type.getType(injectMethod.getClassName()), new Method(injectMethod.getMethodName(), injectMethod.getMethodDesc()));
                    mLog.debug("Method Insert: " + injectMethod.getClassName() + "#" + injectMethod.getMethodName() + injectMethod.getMethodDesc() + " ===SuperBefore===> " + mCurrentClass + "#" + mTargetMethodName + mTargetMethodDesc);
                }
            }
        }

        @Override
        protected void onMethodExit(int opcode) {
            for (InjectMethod injectMethod : mInjectMethods) {
                if (injectMethod.isAfter()) {
                    loadThis();
                    loadArgs();
                    invokeStatic(Type.getType(injectMethod.getClassName()), new Method(injectMethod.getMethodName(), injectMethod.getMethodDesc()));
                }
                mLog.debug("Method Insert: " + injectMethod.getClassName() + "#" + injectMethod.getMethodName() + injectMethod.getMethodDesc() + " ===SuperAfter===> " + mCurrentClass + "#" + mTargetMethodName + mTargetMethodDesc);
            }
            super.onMethodExit(opcode);
        }
    }
}
