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

package com.growingio.sdk.plugin.autotrack;

import com.android.build.gradle.AppExtension;
import com.growingio.sdk.plugin.autotrack.compile.AutotrackBuildException;
import com.growingio.sdk.plugin.autotrack.compile.AutotrackTransform;
import com.growingio.sdk.plugin.autotrack.compile.ClassRewriter;
import com.growingio.sdk.plugin.autotrack.utils.ReflectUtil;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutotrackPlugin implements Plugin<Project> {
    private Logger mLogger;

    @Override
    public void apply(Project project) {
        mLogger = project.getLogger();
        AutotrackExtension extension = project.getExtensions().create("growingAutotracker", AutotrackExtension.class);
        final AppExtension android = project.getExtensions().findByType(AppExtension.class);
        final AutotrackTransform transform = new AutotrackTransform(project);
        android.registerTransform(transform);

        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                checkJavaVersion();
                if (!extension.isDevelopment()) {
                    checkAutotrackDependency(project);
                }
                onGotAndroidJarFiles(android, transform);
            }
        });
    }

    private void checkJavaVersion() {
        String version = System.getProperty("java.version");
        Matcher matcher = Pattern.compile("^(1\\.[0-9]+)\\..*").matcher(version);
        if (matcher.find()) {
            String versionNum = matcher.group(1);
            try {
                int num = (int) (Float.parseFloat(versionNum) * 10);
                if (num < 18) {
                    throw new RuntimeException("GrowingIO autotracker gradle plugin 要求编译环境的JDK为1.8及以上");
                }
            } catch (NumberFormatException e) {
                // ignore
            }
            return;
        }
        mLogger.info("GIO: check java version failed");
    }

    private void checkAutotrackDependency(Project project) {
        for (Configuration configuration : project.getConfigurations()) {
            if ("releaseRuntimeClasspath".equals(configuration.getName())) {
                for (DependencyResult dependency : configuration.getIncoming().getResolutionResult().getRoot().getDependencies()) {
                    if (findAutotrackDependency(dependency)) {
                        return;
                    }
                }
            }
        }
        // TODO: 2020/7/8 完善提示信息
        throw new RuntimeException("未发现autotrack依赖，请参考文档添加依赖");
    }

    private boolean findAutotrackDependency(DependencyResult dependency) {
        String autotrackDependency = "com.growingio.android:autotrack:";
        if (dependency.getRequested().getDisplayName().startsWith(autotrackDependency)) {
            String sdkVersion = dependency.getRequested().getDisplayName().split(":")[2];
            String pluginVersion = getPluginVersion();
            if (sdkVersion.equals(pluginVersion)) {
                return true;
            } else {
                throw new AutotrackBuildException("您的autotracker-gradle-plugin版本号[" + pluginVersion + "]和autotracker版本号[" + sdkVersion + "]不一致，请在build.gradle文件中修改");
            }
        }

        if (dependency instanceof ResolvedDependencyResult) {
            for (DependencyResult result : ((ResolvedDependencyResult) dependency).getSelected().getDependencies()) {
                findAutotrackDependency(result);
            }
        }
        return false;
    }

    public String getPluginVersion() {
        try {
            String jarPath = URLDecoder.decode(new File(ClassRewriter.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getCanonicalPath());
            JarInputStream inputStream = new JarInputStream(new FileInputStream(jarPath));
            String pluginVersion = inputStream.getManifest().getMainAttributes().getValue("Gradle-Plugin-Version");
            if (pluginVersion == null) {
                throw new AutotrackBuildException("Cannot find GrowingIO autotrack gradle plugin version");
            }
            return pluginVersion;
        } catch (IOException e) {
            throw new AutotrackBuildException("Cannot find GrowingIO autotrack gradle plugin version");
        }
    }

    private void onGotAndroidJarFiles(AppExtension appExtension, AutotrackTransform transform) {
        checkJackStatus(appExtension);
        try {
            List<File> files = appExtension.getBootClasspath();
            if (files == null || files.isEmpty()) {
                throw new RuntimeException("GIO: get android.jar failed");
            }
            List<URL> androidJars = new ArrayList<>();
            for (File file : files) {
                androidJars.add(file.toURL());
            }
            transform.setAndroidJars(androidJars);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("GIO: get android.jar failed");
        }
    }

    private void checkJackStatus(AppExtension appExtension) {
        // 高版本Gradle没有这个特性了
        // config.getJackOptions().isEnabled();
        String errorMessage = null;
        try {
            Method configMethod = ReflectUtil.getMethod(AppExtension.class, "getDefaultConfig");
            if (configMethod == null)
                return;
            Object config = configMethod.invoke(appExtension);
            Object jackOptions = ReflectUtil.findField(config, "jackOptions");
            if (jackOptions == null)
                return;
            java.lang.reflect.Method isEnable = ReflectUtil.getMethod(jackOptions.getClass(), "isEnabled");
            if (isEnable == null)
                return;
            boolean jackEnabled = (boolean) isEnable.invoke(jackOptions);
            if (jackEnabled) {
                errorMessage = "\n========= GIO无埋点SDK不支持Jack编译器\n"
                        + "========= 由于TransformApi不支持Jack编译器且Jack项目已被Google废弃。请确保没有以下配置:\n"
                        + "========= jackOptions {\n"
                        + "=========       enabled true\n"
                        + "========= }\n";
            }
        } catch (Exception e) {
            // ignore
            System.out.println(e.getMessage());
        }
        if (errorMessage != null) {
            throw new IllegalStateException(errorMessage);
        }
    }
}
