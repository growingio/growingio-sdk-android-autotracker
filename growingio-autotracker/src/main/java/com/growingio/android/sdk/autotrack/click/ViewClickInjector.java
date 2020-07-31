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

package com.growingio.android.sdk.autotrack.click;

import android.util.Log;
import android.view.View;

import com.growingio.sdk.inject.annotation.AfterSuper;
import com.growingio.sdk.inject.annotation.BeforeSuper;

public class ViewClickInjector {
    private static final String TAG = "ViewClickInjector";

    private ViewClickInjector() {
    }

    @AfterSuper(clazz = View.OnClickListener.class, method = "onClick", parameterTypes = {View.class})
    public static void viewOnClick(View.OnClickListener listener, View view) {
        Log.e(TAG, "viewOnClick: listener = " + listener + ", view = " + view);
    }

    @BeforeSuper(clazz = View.OnClickListener.class, method = "onClick", parameterTypes = {View.class})
    public static void beforeViewOnClick(View.OnClickListener listener, View view) {
        Log.e(TAG, "beforeViewOnClick: listener = " + listener + ", view = " + view);
    }
}
