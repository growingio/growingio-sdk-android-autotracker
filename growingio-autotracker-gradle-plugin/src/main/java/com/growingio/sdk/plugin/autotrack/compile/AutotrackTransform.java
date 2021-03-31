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

import com.android.build.api.transform.Context;
import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Status;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.growingio.sdk.plugin.autotrack.AutotrackExtension;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.api.Project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class AutotrackTransform extends Transform {

    private Log mLog;
    private List<URL> mAndroidJars;

    private TransformOutputProvider mOutputProvider;
    private DirectoryInput mDirectoryInput;
    private ClassRewriter mClassRewriter;
    private BuildExecutor mExecutor;
    private final AutotrackExtension mAutotrackExtension;

    public AutotrackTransform(final Project project) {
        mAutotrackExtension = project.getExtensions().getByType(AutotrackExtension.class);
    }

    public void setAndroidJars(List<URL> androidJars) {
        this.mAndroidJars = androidJars;
    }

    @Override
    public String getName() {
        return "growingAutotracker";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return true;
    }

    private void log(String msg) {
        mLog.info(msg);
    }

    @Override
    public void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, final TransformOutputProvider outputProvider, final boolean isIncremental) throws IOException, InterruptedException {
        if (mAutotrackExtension.isLogEnabled()) {
            mLog = new SystemLog();
        } else {
            mLog = new ErrorLog();
        }

        mLog.info("transform task start: isIncremental = " + isIncremental);
        mExecutor = BuildExecutor.createExecutor();
        ArrayList<URL> urlList = new ArrayList<>();
        for (TransformInput input : inputs) {
            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                urlList.add(directoryInput.getFile().toURL());
            }

            for (JarInput jarInput : input.getJarInputs()) {
                urlList.add(jarInput.getFile().toURL());
            }
        }
        urlList.addAll(mAndroidJars);
        URL[] urlArray = new URL[urlList.size()];
        urlList.toArray(urlArray);
        URLClassLoader classLoader = new URLClassLoader(urlArray);
        mOutputProvider = outputProvider;
        mClassRewriter = new ClassRewriter(mLog, classLoader, mAutotrackExtension.getExcludePackages());

        if (!isIncremental) {
            // 1. 非增量模式下删除上次所有的编译产物
            try {
                outputProvider.deleteAll();
            } catch (IOException e) {
                mLog.error("删除上次的编译产物失败: " + e.getMessage());
                throw e;
            }
        }
        for (TransformInput transformInput : inputs) {
            for (DirectoryInput directoryInput : transformInput.getDirectoryInputs()) {
                mDirectoryInput = directoryInput;
                if (isIncremental) {
                    // 2. 增量模式下处理directory
                    transformInputDirectoryIncrement();
                } else {
                    // 3. 非增量模式处理directory
                    transformInputDirectoryNoIncrement();
                }
            }
            mDirectoryInput = null;
            for (final JarInput jarInput : transformInput.getJarInputs()) {
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        // 4. 处理jar包, 整个jar包暂时在同一个线程中处理, jar包更改的概率很小, 不会影响instant run
                        transformJar(jarInput, isIncremental);
                    }
                });
            }
        }

        mLog.info("has submit all gio task, and wait for all task complete");
        mExecutor.waitAllTaskComplete();
        mLog.info("transform task completed");

        // reset tmp variable
        if (classLoader != null) {
            classLoader.close();
        }
        this.mOutputProvider = null;
        this.mDirectoryInput = null;
        this.mClassRewriter = null;
        this.mExecutor = null;
    }

    private void transformJar(JarInput jarInput, boolean isIncremental) {
        File out = mOutputProvider.getContentLocation(
                jarInput.getName(), jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);
        out.getParentFile().mkdirs();
        if (isIncremental && jarInput.getStatus() == Status.NOTCHANGED) {
            return;
        }
        if (out.exists()) {
            out.delete();
        }
        if (isIncremental && jarInput.getStatus() == Status.REMOVED) {
            return;
        }
        log("transforming " + jarInput.getFile() + " to jar: " + out);
        try (ZipOutputStream outJar = new ZipOutputStream(new FileOutputStream(out))) {
            try (ZipInputStream jar = new ZipInputStream(new FileInputStream(jarInput.getFile()))) {
                ZipEntry entry;
                while ((entry = jar.getNextEntry()) != null) {
                    ZipEntry outEntry = copyEntry(entry);
                    outJar.putNextEntry(outEntry);
                    if (!entry.isDirectory()
                            && !entry.getName().equals("module-info.class")
                            && entry.getName().endsWith(".class")) {
                        if (mClassRewriter.transformClass(jar, outJar)) {
                            log("transforming jar entry: " + entry.getName());
                        }
                    } else {
                        IOUtils.copy(jar, outJar);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void transformInputDirectoryIncrement() {
        Map<File, Status> changedFiles = mDirectoryInput.getChangedFiles();
        for (File file : changedFiles.keySet()) {
            Status status = changedFiles.get(file);
            actionOnFile(file, status);
        }
    }

    // 5. 处理directoryInput中的文件, 可以单个在子线程中处理
    private void actionOnFile(final File file, final Status added) {
        File outDir = mOutputProvider.getContentLocation(mDirectoryInput.getName(),
                mDirectoryInput.getContentTypes(), mDirectoryInput.getScopes(), Format.DIRECTORY);
        outDir.mkdirs();
        final String outDirPath = outDir.getAbsolutePath();
        final String inputDirPath = mDirectoryInput.getFile().getAbsolutePath();
        final String relativeClassPath = file.getAbsolutePath().substring(inputDirPath.length());
        mExecutor.execute(() -> {
            File outFile = new File(outDirPath, relativeClassPath);
            if (added==Status.REMOVED) {
                FileUtils.deleteQuietly(outFile);
            } else if(added == Status.NOTCHANGED){
                log("class file no change" + file);
            }else { // ADDED or CHANGED
                if (relativeClassPath.endsWith(".class")) {
                    if (mClassRewriter.transformClassFile(file, outFile)) {
                        log("transformed class file " + file + " to " + outFile);
                        return;
                    }
                }
                try {
                    FileUtils.copyFile(file, outFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void transformInputDirectoryNoIncrement() {
        for (File classFile : com.android.utils.FileUtils.getAllFiles(mDirectoryInput.getFile())) {
            actionOnFile(classFile, Status.ADDED);
        }
    }

    private ZipEntry copyEntry(ZipEntry entry) {
        ZipEntry newEntry = new ZipEntry(entry.getName());
        newEntry.setComment(entry.getComment());
        newEntry.setExtra(entry.getExtra());
        return newEntry;
    }

}