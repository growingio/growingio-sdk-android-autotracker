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

package com.growingio.autotest;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.test.internal.runner.RunnerArgs;
import androidx.test.runner.AndroidJUnitRunner;

import com.growingio.autotest.help.BeforeAppOnCreate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class TrackAndroidJUnitRunner extends AndroidJUnitRunner {
    private static final String TAG = "TrackAndroidJUnitRunner";

    private ClassLoader mClassLoader;
    private String mTestClass;

    @Override
    public void onCreate(Bundle arguments) {
        Log.e(TAG, "arguments is " + arguments.toString());
        RunnerArgs runnerArgs = new RunnerArgs.Builder().fromManifest(this).fromBundle(this, arguments).build();
        if (runnerArgs.tests != null && !runnerArgs.tests.isEmpty()) {
            mTestClass = runnerArgs.tests.get(0).testClassName;
            Log.e(TAG, "TestClass is " + mTestClass);
        }
        super.onCreate(arguments);
    }

    @Override
    public void callApplicationOnCreate(Application app) {
        if (!TextUtils.isEmpty(mTestClass)) {
            try {
                Class<?> clazz = mClassLoader.loadClass(mTestClass);
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (method.getAnnotation(BeforeAppOnCreate.class) != null) {
                        if (!Modifier.isStatic(method.getModifiers())) {
                            IllegalArgumentException exception = new IllegalArgumentException(method.getName() + "() should be static");
                            Log.e(TAG, "callApplicationOnCreate: " + exception);
                            throw exception;
                        }
                        method.invoke(null);
                        break;
                    }
                }
            } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        super.callApplicationOnCreate(app);
    }

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        mClassLoader = cl;
        return super.newApplication(cl, className, context);
    }
}
