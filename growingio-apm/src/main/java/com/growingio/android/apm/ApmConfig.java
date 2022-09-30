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
class ApmConfig implements Configurable {

    private boolean activityLifecycleTracing = true;
    private boolean fragmentXLifecycleTracing = true;
    private boolean fragmentSupportLifecycleTracing = false;
    private boolean fragmentSystemLifecycleTracing = false;
    private boolean uncaughtException = true;
    private boolean printUncaughtException = false;
    private boolean anrTracing = false;
    private long anrTimeoutIntervalMillis = 5000L;
    private boolean anrInDebug = false;

    public boolean isActivityLifecycleTracing() {
        return activityLifecycleTracing;
    }

    public void setActivityLifecycleTracing(boolean activityLifecycleTracing) {
        this.activityLifecycleTracing = activityLifecycleTracing;
    }

    public boolean isFragmentXLifecycleTracing() {
        return fragmentXLifecycleTracing;
    }

    public void setFragmentXLifecycleTracing(boolean fragmentXLifecycleTracing) {
        this.fragmentXLifecycleTracing = fragmentXLifecycleTracing;
    }

    public boolean isFragmentSupportLifecycleTracing() {
        return fragmentSupportLifecycleTracing;
    }

    public void setFragmentSupportLifecycleTracing(boolean fragmentSupportLifecycleTracing) {
        this.fragmentSupportLifecycleTracing = fragmentSupportLifecycleTracing;
    }

    public boolean isFragmentSystemLifecycleTracing() {
        return fragmentSystemLifecycleTracing;
    }

    public void setFragmentSystemLifecycleTracing(boolean fragmentSystemLifecycleTracing) {
        this.fragmentSystemLifecycleTracing = fragmentSystemLifecycleTracing;
    }

    public boolean isUncaughtException() {
        return uncaughtException;
    }

    public void setUncaughtException(boolean uncaughtException) {
        this.uncaughtException = uncaughtException;
    }

    public boolean isPrintUncaughtException() {
        return printUncaughtException;
    }

    public void setPrintUncaughtException(boolean printUncaughtException) {
        this.printUncaughtException = printUncaughtException;
    }
}
