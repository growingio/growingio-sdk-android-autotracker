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

package com.growingio.android.sdk.autotrack.change;

import android.view.View;
import android.widget.TextView;

import com.growingio.android.sdk.autotrack.util.ViewHelper;
import com.growingio.android.sdk.track.utils.LogUtil;
import com.growingio.sdk.inject.annotation.BeforeSuper;

public class ChangeInjector {
    private static final String TAG = "ChangeInjector";

    private ChangeInjector() {
    }

    @BeforeSuper(clazz = View.OnFocusChangeListener.class, method = "onFocusChange", parameterTypes = {View.class, boolean.class})
    public static void beforeViewOnClick(View.OnClickListener listener, View view, boolean hasFocus) {
        if (view instanceof TextView) {
            LogUtil.d(TAG, "onFocusChanged");
            ViewHelper.changeOn(view);
        }
    }
}