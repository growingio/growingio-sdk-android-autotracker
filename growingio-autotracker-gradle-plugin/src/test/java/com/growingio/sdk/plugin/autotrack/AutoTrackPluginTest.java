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

import org.codehaus.groovy.runtime.InvokerHelper;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

@RunWith(PowerMockRunner.class)
public class AutoTrackPluginTest {
    @Rule
    TemporaryFolder mTestProjectDir = new TemporaryFolder();

    @Test
    public void testAutoTrackPlugin() throws IOException {
        File buildFile = mTestProjectDir.newFile("build.gradle");

        append(buildFile, "buildscript {\n" +
                "    repositories {\n" +
                "        mavenLocal()\n" +
                "        google()\n" +
                "        jcenter()\n" +
                "        maven { url'http://oss.jfrog.org/artifactory/oss-snapshot-local'}\n" +
                "    }\n" +
                "    dependencies {\n" +
                "        classpath 'com.android.tools.build:gradle:3.5.0'\n" +
                "    }\n" +
                "}\n");

        append(buildFile, "plugins {\n" +
                "    id 'com.android.application'\n" +
                "    id 'com.growingio.android.autotracker'\n" +
                "}\n");

        append(buildFile, "android {\n" +
                "    compileSdkVersion 29\n" +
                "    buildToolsVersion \"29.0.2\"\n" +
                "\n" +
                "    defaultConfig {\n" +
                "        applicationId \"com.growingio.demo\"\n" +
                "        minSdkVersion 21\n" +
                "        targetSdkVersion 29\n" +
                "        versionCode 1\n" +
                "        versionName \"1.0\"\n" +
                "\n" +
                "        resValue(\"string\", \"growingio_project_id\", \"ProjectId\")\n" +
                "        resValue(\"string\", \"growingio_url_scheme\", \"URLScheme\")\n" +
                "\n" +
                "        testInstrumentationRunner \"androidx.test.runner.AndroidJUnitRunner\"\n" +
                "\n" +
                "        compileOptions {\n" +
                "            sourceCompatibility JavaVersion.VERSION_1_8\n" +
                "            targetCompatibility JavaVersion.VERSION_1_8\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    buildTypes {\n" +
                "        release {\n" +
                "            minifyEnabled false\n" +
                "            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'\n" +
                "        }\n" +
                "        debug {\n" +
                "            minifyEnabled false\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    repositories {\n" +
                "        flatDir {\n" +
                "            dirs 'libs'\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "}\n");

        append(buildFile, "growingAutotracker {\n" +
                "    development true\n" +
                "    logEnabled true\n" +
                "//    excludePackages \"com.gio.test.three.autotrack.fragments\", \"com.example\"\n" +
                "}");

        append(buildFile, "repositories {\n" +
                "    mavenLocal()\n" +
                "    jcenter()\n" +
                "    google()\n" +
                "    maven { url'http://oss.jfrog.org/artifactory/oss-snapshot-local'}\n" +
                "}\n");

        append(buildFile, "dependencies {\n" +
                "    implementation 'com.growingio.android:autotracker:3.0.1-SNAPSHOT'\n" +
                "}");

        // 需要构建一个android工程， 或者mock
        try {
            GradleRunner.create()
                    .withProjectDir(mTestProjectDir.getRoot())
                    .withPluginClasspath()
                    .withDebug(true)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void append(File file, Object text) throws IOException {
        OutputStreamWriter writer = null;

        try {
            FileOutputStream out = new FileOutputStream(file, true);

            writer = new OutputStreamWriter(out, Charset.defaultCharset().name());
            InvokerHelper.write(writer, text);
            writer.flush();
            Writer temp = writer;
            writer = null;
            temp.close();
        } finally {
            closeWithWarning(writer);
        }

    }

    public static void closeWithWarning(Closeable closeable) {
        tryClose(closeable, true);
    }

    static Throwable tryClose(AutoCloseable closeable, boolean logWarning) {
        Throwable thrown = null;
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception exception) {
                thrown = exception;
                if (logWarning) {
                    System.out.println("Caught exception during close(): " + exception);
                }
            }
        }

        return thrown;
    }
}
