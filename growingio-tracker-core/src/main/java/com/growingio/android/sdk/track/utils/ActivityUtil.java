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

package com.growingio.android.sdk.track.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

public final class ActivityUtil {
    private ActivityUtil() {
    }

    @Nullable
    public static Activity findActivity(@Nullable View view) {
        if (view == null) {
            return null;
        }
        return findActivity(view.getContext());
    }

    @Nullable
    public static Activity findActivity(@NonNull Context context) {
        if (!(context instanceof ContextWrapper)) {
            return null;
        }
        ContextWrapper current = (ContextWrapper) context;
        while (true) {
            if (current instanceof Activity) {
                return (Activity) current;
            }
            Context parent = current.getBaseContext();
            if (!(parent instanceof ContextWrapper)) {
                break;
            }
            current = (ContextWrapper) parent;
        }
        return null;
    }

    /**
     * @return context对应的activity是否被销毁
     */
    public static boolean isDestroy(Context context) {
        Activity activity = findActivity(context);
        if (activity != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return activity.isDestroyed();
            }
        }
        return false;
    }
}
