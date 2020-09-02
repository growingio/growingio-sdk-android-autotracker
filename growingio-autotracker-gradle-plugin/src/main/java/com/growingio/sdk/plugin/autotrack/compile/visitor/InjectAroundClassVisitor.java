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
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.util.Map;

public class InjectAroundClassVisitor extends ClassVisitor {
    private final Context mContext;
    private final Log mLog;
    private String mCurrentClass;

    public InjectAroundClassVisitor(ClassVisitor classVisitor, Context context) {
        super(context.getASMVersion(), classVisitor);
        mContext = context;
        mLog = mContext.getLog();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        mCurrentClass = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        return new AroundMethodVisitor(mv, access, name, desc);
    }

    private final class AroundMethodVisitor extends GeneratorAdapter {

        AroundMethodVisitor(MethodVisitor mv, int access, String name, String desc) {
            super(mContext.getASMVersion(), mv, access, name, desc);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            TargetMethod targetMethod = findTargetMethod(owner, name, desc);

            if (targetMethod != null) {
                Method originalMethod = new Method(name, desc);
                int callObject = -1;
                int[] locals = new int[originalMethod.getArgumentTypes().length];
                for (int i = locals.length - 1; i >= 0; i--) {
                    locals[i] = newLocal(originalMethod.getArgumentTypes()[i]);
                    storeLocal(locals[i]);
                }
                if (opcode != Opcodes.INVOKESTATIC) {
                    callObject = newLocal(Type.getObjectType(owner));
                    storeLocal(callObject);
                }
                for (InjectMethod injectMethod : targetMethod.getInjectMethods()) {
                    if (!injectMethod.isAfter()) {
                        if (callObject >= 0) {
                            loadLocal(callObject);
                        }
                        for (int tmpLocal : locals) {
                            loadLocal(tmpLocal);
                        }
                        invokeStatic(Type.getType(injectMethod.getClassName()), new Method(injectMethod.getMethodName(), injectMethod.getMethodDesc()));
                        mLog.debug(mCurrentClass + ": " + injectMethod.getClassName() + "#" + injectMethod.getMethodName() + injectMethod.getMethodDesc() + " ===Before===> " + owner + "#" + name + desc);
                    }
                }

                if (callObject >= 0) {
                    loadLocal(callObject);
                }

                for (int tmpLocal : locals) {
                    loadLocal(tmpLocal);
                }
                super.visitMethodInsn(opcode, owner, name, desc, itf);

                for (InjectMethod injectMethod : targetMethod.getInjectMethods()) {
                    if (injectMethod.isAfter()) {
                        if (callObject >= 0) {
                            loadLocal(callObject);
                        }
                        for (int tmpLocal : locals) {
                            loadLocal(tmpLocal);
                        }
                        invokeStatic(Type.getType(injectMethod.getClassName()), new Method(injectMethod.getMethodName(), injectMethod.getMethodDesc()));
                        mLog.debug(mCurrentClass + ": " + injectMethod.getClassName() + "#" + injectMethod.getMethodName() + injectMethod.getMethodDesc() + " ===After===> " + owner + "#" + name + desc);
                    }
                }
                mContext.markModified();
            } else {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }
        }
    }

    private TargetMethod findTargetMethod(String className, String methodName, String methodDesc) {
        TargetClass targetClass = findTargetClass(className);
        if (targetClass != null) {
            return targetClass.getTargetMethod(methodName, methodDesc);
        }
        return null;
    }

    private TargetClass findTargetClass(String className) {
        Map<String, TargetClass> aroundHookClasses = HookClassesConfig.getAroundHookClasses();
        for (String clazz : aroundHookClasses.keySet()) {
            if (isAssignable(className, clazz)) {
                return aroundHookClasses.get(clazz);
            }
        }
        return null;
    }

    private boolean isAssignable(String subClassName, String superClassName) {
        if (subClassName.contains("/")) {
            subClassName = subClassName.replace("/", ".");
        }
        if (superClassName.contains("/")) {
            superClassName = superClassName.replace("/", ".");
        }
        try {
            Class<?> subClass = mContext.getClassLoader().loadClass(subClassName);
            Class<?> superClass = mContext.getClassLoader().loadClass(superClassName);
            return superClass.isAssignableFrom(subClass);
        } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
        }
        return false;
    }
}
