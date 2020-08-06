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

package com.growingio.sdk.plugin.autotrack.compile;

import com.growingio.sdk.plugin.autotrack.compile.visitor.ContextClassVisitor;
import com.growingio.sdk.plugin.autotrack.compile.visitor.DesugaredClassVisitor;
import com.growingio.sdk.plugin.autotrack.compile.visitor.DesugaringClassVisitor;
import com.growingio.sdk.plugin.autotrack.compile.visitor.InjectAroundClassVisitor;
import com.growingio.sdk.plugin.autotrack.compile.visitor.InjectSuperClassVisitor;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ClassRewriter {
    private final Log mLog;
    private final ClassLoader mClassLoader;
    private final String[] mUserExcludePackages;
    private static final String[] EXCLUDED_PACKAGES = new String[]{
            "com/growingio/android/sdk/",
            "com/alibaba/mobileim/extra/xblink/webview",
            "com/alibaba/sdk/android/feedback/xblink",
            "com/tencent/smtt",
            "com/baidu/location",
            "com/blueware/agent/android",
            "com/oneapm/agent/android",
            "com/networkbench/agent",
            "com/networkbench/agent",
            "android/taobao/windvane/webview",
    };


    public ClassRewriter(final Log log, ClassLoader classLoader, String[] userExcludePackages) {
        mLog = log;
        mClassLoader = classLoader;
        if (userExcludePackages == null) {
            mUserExcludePackages = new String[0];
        } else {
            mUserExcludePackages = new String[userExcludePackages.length];
            for (int i = 0; i < userExcludePackages.length; i++) {
                mUserExcludePackages[i] = userExcludePackages[i].replace(".", "/");
            }
        }
    }

    private boolean isExcludedPackage(String packageName) {
        for (String exPackage : EXCLUDED_PACKAGES) {
            if (packageName.startsWith(exPackage)) {
                return true;
            }
        }

        for (String exPackage : mUserExcludePackages) {
            if (packageName.startsWith(exPackage)) {
                return true;
            }
        }
        return false;
    }

    public boolean transformClass(InputStream from, OutputStream to) {
        try {
            byte[] bytes = IOUtils.toByteArray(from);
            byte[] modifiedClass = visitClassBytes(bytes);
            if (modifiedClass != null) {
                IOUtils.write(modifiedClass, to);
                return true;
            } else {
                IOUtils.write(bytes, to);
            }
        } catch (IOException e) {
            mLog.error(e.getMessage(), e);
        }
        return false;
    }

    public boolean transformClassFile(File from, File to) {
        boolean result;
        File toParent = to.getParentFile();
        toParent.mkdirs();
        try (FileInputStream fileInputStream = new FileInputStream(from); FileOutputStream fileOutputStream = new FileOutputStream(to)) {
            result = transformClass(fileInputStream, fileOutputStream);
        } catch (Exception e) {
            mLog.error(e.getMessage(), e);
            result = false;
        }
        return result;
    }

    private byte[] visitClassBytes(byte[] bytes) {
        String className = null;
        try {
            ClassReader classReader = new ClassReader(bytes);
            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
            Context context = new Context(mLog, mClassLoader);
            classReader.accept(new ContextClassVisitor(context), ClassReader.SKIP_DEBUG | ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);
            className = context.getClassName();
            ClassVisitor classVisitor;
            if (this.isExcludedPackage(context.getClassName())) {
                return null;
            }
            DesugaringClassVisitor desugaringClassVisitor = new DesugaringClassVisitor(
                    new InjectAroundClassVisitor(
                            new InjectSuperClassVisitor(classWriter, context),
                            context),
                    context);
            classVisitor = desugaringClassVisitor;
            classReader.accept(classVisitor, ClassReader.SKIP_FRAMES | ClassReader.EXPAND_FRAMES);
            if (!desugaringClassVisitor.getNeedInjectTargetMethods().isEmpty()) {
                // lambda 表达式需要特殊处理两次
                mLog.debug(String.format("GIO: deal with lambda second time:  %s", className));
                ClassReader lambdaReader = new ClassReader(classWriter.toByteArray());
                classWriter = new ClassWriter(lambdaReader, ClassWriter.COMPUTE_MAXS);
                lambdaReader.accept(new DesugaredClassVisitor(classWriter, context, desugaringClassVisitor.getNeedInjectTargetMethods()), ClassReader.SKIP_FRAMES | ClassReader.EXPAND_FRAMES);
            }
            if (context.isClassModified()) {
                return classWriter.toByteArray();
            }
        } catch (AutotrackBuildException e) {
            throw new RuntimeException(e);
        } catch (Throwable t) {
            this.mLog.error("Unfortunately, an error has occurred while processing " + className + ". Please copy your build logs and the jar containing this class and visit https://www.growingio.com, thanks!\n" + t.getMessage(), t);
        }
        return null;
    }
}
