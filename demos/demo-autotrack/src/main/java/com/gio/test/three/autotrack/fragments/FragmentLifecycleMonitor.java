/*
 * Copyright (C) 2023 Beijing Yishu Technology Co., Ltd.
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
package com.gio.test.three.autotrack.fragments;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FragmentLifecycleMonitor {
    private static final String TAG = "FragmentLifecycleMonitor";

    private final List<WeakReference<FragmentLifecycleCallback>> mCallbacks = new ArrayList<>();

    private static final class SingleInstance {
        private static final FragmentLifecycleMonitor INSTANCE = new FragmentLifecycleMonitor();
    }

    private FragmentLifecycleMonitor() {
    }

    public static FragmentLifecycleMonitor get() {
        return SingleInstance.INSTANCE;
    }

    public void addLifecycleCallback(FragmentLifecycleCallback callback) {
        synchronized (mCallbacks) {
            boolean needsAdd = true;
            Iterator<WeakReference<FragmentLifecycleCallback>> refIter = mCallbacks.iterator();
            while (refIter.hasNext()) {
                FragmentLifecycleCallback storedCallback = refIter.next().get();
                if (null == storedCallback) {
                    refIter.remove();
                } else if (storedCallback == callback) {
                    needsAdd = false;
                }
            }
            if (needsAdd) {
                mCallbacks.add(new WeakReference<>(callback));
            }
        }
    }

    public void removeLifecycleCallback(FragmentLifecycleCallback callback) {
        synchronized (mCallbacks) {
            Iterator<WeakReference<FragmentLifecycleCallback>> refIter = mCallbacks.iterator();
            while (refIter.hasNext()) {
                FragmentLifecycleCallback storedCallback = refIter.next().get();
                if (null == storedCallback) {
                    refIter.remove();
                } else if (storedCallback == callback) {
                    refIter.remove();
                }
            }
        }
    }

    @SuppressLint("LongLogTag")
    public void signalLifecycleChange(Fragment fragment, FragmentLifecycleCallback.Stage stage) {
        synchronized (mCallbacks) {
            Iterator<WeakReference<FragmentLifecycleCallback>> refIter = mCallbacks.iterator();
            while (refIter.hasNext()) {
                FragmentLifecycleCallback callback = refIter.next().get();
                if (null == callback) {
                    refIter.remove();
                } else {
                    try {
                        Log.d(TAG, "running callback: " + callback);
                        callback.onFragmentLifecycleChanged(fragment, stage);
                        Log.d(TAG, "callback completes: " + callback);
                    } catch (RuntimeException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            }
        }
    }
}
