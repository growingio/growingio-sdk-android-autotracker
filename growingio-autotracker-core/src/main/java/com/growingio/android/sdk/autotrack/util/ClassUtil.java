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

package com.growingio.android.sdk.autotrack.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;

import com.growingio.android.sdk.autotrack.R;


@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class ClassUtil {
    private static final String ANONYMOUS_CLASS_NAME = "Anonymous";

    private ClassUtil() {
    }

    public static String getSimpleClassName(Class<?> clazz) {
        String name = clazz.getSimpleName();
        if (TextUtils.isEmpty(name)) {
            name = ANONYMOUS_CLASS_NAME;
        }
        return name;
    }

    public static boolean isDuplicateClick(View view) {
        if (view == null) {
            return false;
        }
        try {
            String timeStamp = (String) view.getTag(R.id.growing_tracker_duplicate_click_timestamp);
            if (!TextUtils.isEmpty(timeStamp)) {
                long lastTime = Long.parseLong(timeStamp);
                if (SystemClock.elapsedRealtime() - lastTime <= 200) {
                    return true;
                }
            }
            view.setTag(R.id.growing_tracker_duplicate_click_timestamp, String.valueOf(SystemClock.elapsedRealtime()));
        } catch (Exception ignored) {
        }
        return false;
    }
}

