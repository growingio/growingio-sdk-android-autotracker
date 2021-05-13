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
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.objectweb.asm.Opcodes.ACONST_NULL;

public class DesugaringClassVisitor extends ClassVisitor {
    private static final String TAG = "DesugaringClassVisitor";

    private final Context mContext;
    private final Log mLog;

    private final Set<TargetMethod> mNeedInjectTargetMethods = new HashSet<>();
    private final HashMap<String, GenerateMethodBlock> mGenerateMethodBlocks = new HashMap<>();
    private int mGenerateMethodIndex = 0;

    public DesugaringClassVisitor(ClassVisitor cv, Context context) {
        super(context.getASMVersion(), cv);
        mContext = context;
        mLog = context.getLog();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        return new DesugaringMethodVisitor(mContext.getASMVersion(), methodVisitor, access, name, desc);
    }

    @Override
    public void visitEnd() {
        if (mGenerateMethodBlocks.isEmpty()) {
            super.visitEnd();
            return;
        }

        for (GenerateMethodBlock methodBlock : mGenerateMethodBlocks.values()) {
            generateMethod(methodBlock);
        }

        super.visitEnd();
    }

    public Set<TargetMethod> getNeedInjectTargetMethods() {
        return mNeedInjectTargetMethods;
    }

    private void generateMethod(GenerateMethodBlock methodBlock) {
        mLog.debug(TAG + ": generateMethod: " + methodBlock.mMethodName + "#" + methodBlock.mMethodDesc);
        int access = Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_SYNTHETIC;
        MethodVisitor visitor = super.visitMethod(access, methodBlock.mMethodName, methodBlock.mMethodDesc, null, null);
        GeneratorAdapter adapter = new GeneratorAdapter(visitor, access, methodBlock.mMethodName, methodBlock.mMethodDesc);
        adapter.visitCode();
        Type[] arguments = Type.getArgumentTypes(methodBlock.mMethodDesc);
        boolean isStaticOrigin = methodBlock.mOriginHandle.getTag() == Opcodes.H_INVOKESTATIC;
        for (InjectMethod injectMethod : methodBlock.mTargetMethod.getInjectMethods()) {
            if (!injectMethod.isAfter()) {
                adapter.visitInsn(ACONST_NULL);
                if (isStaticOrigin) {
                    adapter.loadArgs();
                } else {
                    if (arguments.length > 1) {
                        adapter.loadArgs(1, arguments.length - 1);
                    }
                }
                adapter.invokeStatic(Type.getObjectType(injectMethod.getClassName()), new Method(injectMethod.getMethodName(), injectMethod.getMethodDesc()));
            }
        }

        adapter.loadArgs();
        Type owner = Type.getObjectType(methodBlock.mOriginHandle.getOwner());
        Method method = new Method(methodBlock.mOriginHandle.getName(), methodBlock.mOriginHandle.getDesc());
        switch (methodBlock.mOriginHandle.getTag()) {
            case Opcodes.H_INVOKEINTERFACE:
                adapter.invokeInterface(owner, method);
                break;
            case Opcodes.H_INVOKESPECIAL:
                throw new RuntimeException("should not has invoke special: " + methodBlock.mMethodName + "#" + methodBlock.mMethodDesc);
            case Opcodes.H_INVOKESTATIC:
                adapter.invokeStatic(owner, method);
                break;
            case Opcodes.H_INVOKEVIRTUAL:
                adapter.invokeVirtual(owner, method);
                break;
            default:
                break;
        }

        for (InjectMethod injectMethod : methodBlock.mTargetMethod.getInjectMethods()) {
            if (injectMethod.isAfter()) {
                adapter.visitInsn(ACONST_NULL);
                if (isStaticOrigin) {
                    adapter.loadArgs();
                } else {
                    if (arguments.length > 1) {
                        adapter.loadArgs(1, arguments.length - 1);
                    }
                }
                adapter.invokeStatic(Type.getObjectType(injectMethod.getClassName()), new Method(injectMethod.getMethodName(), injectMethod.getMethodDesc()));
            }
        }

        adapter.returnValue();
        adapter.visitMaxs(arguments.length, arguments.length);
        adapter.visitEnd();
    }

    private final class DesugaringMethodVisitor extends AdviceAdapter {
        private final String mName;

        private DesugaringMethodVisitor(int api, MethodVisitor mv, int access, String name, String desc) {
            super(api, mv, access, name, desc);
            mName = name;
        }

        @Override
        public void visitInvokeDynamicInsn(String lambdaMethodName, String desc, Handle bsm, Object... bsmArgs) {
            int index = desc.lastIndexOf(")L");
            if (index == -1) {
                super.visitInvokeDynamicInsn(lambdaMethodName, desc, bsm, bsmArgs);
                return;
            }
            String interfaceClazzName = desc.substring(index + 2, desc.length() - 1);
            TargetClass targetClass = HookClassesConfig.getSuperHookClasses().get(interfaceClazzName);
            if (targetClass == null) {
                super.visitInvokeDynamicInsn(lambdaMethodName, desc, bsm, bsmArgs);
                return;
            }
            String lambdaMethodDesc = ((Type) bsmArgs[0]).getDescriptor();
            TargetMethod targetMethod = targetClass.getTargetMethod(lambdaMethodName, lambdaMethodDesc);
            if (targetMethod == null) {
                super.visitInvokeDynamicInsn(lambdaMethodName, desc, bsm, bsmArgs);
                return;
            }

            Handle handle = (Handle) bsmArgs[1];
            if (lambdaMethodName.equals(handle.getName())) {
                // 校验实现方法是不是实现了对应接口的实现方法， 如果是则过滤，交给 InjectSuperClassVisitor 进行处理
                if (mContext.isAssignable(handle.getOwner(), interfaceClazzName)) {
                    mLog.debug(String.format("DesugaringClassVisitor(%s): skipped on method %s", mContext.getClassName(), mName));
                    super.visitInvokeDynamicInsn(lambdaMethodName, desc, bsm, bsmArgs);
                    return;
                }
            }

            if (handle.getOwner().equals(mContext.getClassName())) {
                // 实现方法在此类中
                super.visitInvokeDynamicInsn(lambdaMethodName, desc, bsm, bsmArgs);
                TargetMethod needInjectMethod = new TargetMethod(handle.getName(), handle.getDesc());
                needInjectMethod.addInjectMethods(targetMethod.getInjectMethods());
                mNeedInjectTargetMethods.add(needInjectMethod);
            } else {
                String key = interfaceClazzName + handle.getOwner() + handle.getName() + handle.getDesc();
                GenerateMethodBlock methodBlock = mGenerateMethodBlocks.get(key);
                if (methodBlock == null) {
                    String methodDesc;
                    if (handle.getTag() == Opcodes.H_INVOKESTATIC) {
                        methodDesc = "(" + handle.getDesc().replace("(", "");
                    } else {
                        methodDesc = "(L" + handle.getOwner() + ";" + handle.getDesc().replace("(", "");
                    }
                    methodBlock = new GenerateMethodBlock("lambda$GIO$" + mGenerateMethodIndex, methodDesc, targetMethod, handle);
                    mGenerateMethodBlocks.put(key, methodBlock);
                    mGenerateMethodIndex++;
                }

                bsmArgs[1] = new Handle(Opcodes.H_INVOKESTATIC, mContext.getClassName(), methodBlock.mMethodName, methodBlock.mMethodDesc);
                String newDesc;
                // Check for exact match on non-receiver captured arguments
                // (实例方法中this是receiver, 这里进行更改， 防止改为invoke_static 后Lambda Runtime校验失败 )
                if (handle.getTag() == Opcodes.H_INVOKESTATIC) {
                    newDesc = desc;
                } else {
                    newDesc = newDesc(desc, handle.getOwner());
                }
                mContext.markModified();
                super.visitInvokeDynamicInsn(lambdaMethodName, newDesc, bsm, bsmArgs);
            }
        }

        private String newDesc(String oldDesc, String realOwner) {
            int firstSemiColon = oldDesc.indexOf(';');
            return "(L" + realOwner + oldDesc.substring(firstSemiColon);
        }
    }

    private static final class GenerateMethodBlock {
        final String mMethodName;
        final String mMethodDesc;
        final TargetMethod mTargetMethod;
        final Handle mOriginHandle;

        GenerateMethodBlock(String methodName, String methodDesc, TargetMethod targetMethod, Handle originHandle) {
            this.mMethodName = methodName;
            this.mMethodDesc = methodDesc;
            this.mTargetMethod = targetMethod;
            this.mOriginHandle = originHandle;
        }
    }
}
