/*
 *   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.growingio.android.apm;

import com.growingio.android.sdk.Configurable;

/**
 * <p>
 *
 * @author cpacm 2022/9/27
 */
public class ApmConfig implements Configurable {

    private boolean activityLifecycleTracing = true;
    private boolean fragmentXLifecycleTracing = true;
    private boolean fragmentSupportLifecycleTracing = false;
    private boolean fragmentSystemLifecycleTracing = false;
    private boolean uncaughtException = true;
    private boolean printUncaughtException = false;
    private final boolean anrTracing = false;
    private final long anrTimeoutIntervalMillis = 5000L;
    private final boolean anrInDebug = false;

    public boolean isActivityLifecycleTracing() {
        return activityLifecycleTracing;
    }

    public ApmConfig setActivityLifecycleTracing(boolean activityLifecycleTracing) {
        this.activityLifecycleTracing = activityLifecycleTracing;
        return this;
    }

    public boolean isFragmentXLifecycleTracing() {
        return fragmentXLifecycleTracing;
    }

    public ApmConfig setFragmentXLifecycleTracing(boolean fragmentXLifecycleTracing) {
        this.fragmentXLifecycleTracing = fragmentXLifecycleTracing;
        return this;
    }

    public boolean isFragmentSupportLifecycleTracing() {
        return fragmentSupportLifecycleTracing;
    }

    public ApmConfig setFragmentSupportLifecycleTracing(boolean fragmentSupportLifecycleTracing) {
        this.fragmentSupportLifecycleTracing = fragmentSupportLifecycleTracing;
        return this;
    }

    @Deprecated
    public boolean isFragmentSystemLifecycleTracing() {
        return fragmentSystemLifecycleTracing;
    }

    /**
     * don't suggest use System Fragment.
     * @deprecated Use {@link #setFragmentXLifecycleTracing(boolean)}
     */
    @Deprecated
    public ApmConfig setFragmentSystemLifecycleTracing(boolean fragmentSystemLifecycleTracing) {
        this.fragmentSystemLifecycleTracing = fragmentSystemLifecycleTracing;
        return this;
    }

    public boolean isUncaughtException() {
        return uncaughtException;
    }

    public ApmConfig setUncaughtException(boolean uncaughtException) {
        this.uncaughtException = uncaughtException;
        return this;
    }

    public boolean isPrintUncaughtException() {
        return printUncaughtException;
    }

    public ApmConfig setPrintUncaughtException(boolean printUncaughtException) {
        this.printUncaughtException = printUncaughtException;
        return this;
    }

    public boolean isAnrTracing() {
        return anrTracing;
    }

    public long getAnrTimeoutIntervalMillis() {
        return anrTimeoutIntervalMillis;
    }

    public boolean isAnrInDebug() {
        return anrInDebug;
    }


}
