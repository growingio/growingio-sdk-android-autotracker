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
package com.growingio.android.apm;

import android.util.Log;

import com.growingio.android.gmonitor.utils.ILogger;
import com.growingio.android.sdk.track.log.Logger;

/**
 * <p>
 *
 * @author cpacm 2022/9/27
 */
class ApmLogger implements ILogger {

    private static final String TAG = "TRACKER-APM";

    @Override
    public boolean isEnabled(int i) {
        return true;
    }

    @Override
    public void log(int i, String s, Object... objects) {
        log(i, null, s, objects);
    }

    @Override
    public void log(int i, String s, Throwable throwable) {
        log(i, throwable, s);
    }

    @Override
    public void log(int i, Throwable throwable, String s, Object... objects) {
        if (i == Log.DEBUG) {
            Logger.d(TAG, throwable, s, objects);
        } else if (i == Log.INFO) {
            Logger.i(TAG, throwable, s, objects);
        } else if (i == Log.ERROR) {
            Logger.e(TAG, throwable, s, objects);
        } else if (i == Log.WARN) {
            Logger.w(TAG, throwable, s, objects);
        } else {
            Logger.d(TAG, throwable, s, objects);
        }
    }
}
