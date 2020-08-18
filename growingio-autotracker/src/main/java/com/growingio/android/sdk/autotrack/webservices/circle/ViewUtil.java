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

package com.growingio.android.sdk.autotrack.webservices.circle;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebView;
import android.widget.AdapterView;

import androidx.annotation.Nullable;

import com.growingio.android.sdk.autotrack.models.ViewNode;
import com.growingio.android.sdk.autotrack.view.ViewHelper;

public class ViewUtil {
    private ViewUtil() {
    }

    @Nullable
    public static ViewNode getClickableParentViewNode(View view) {
        if (view == null) {
            return null;
        }
        ViewParent parent = view.getParent();
        if (parent == null) {
            return null;
        }
        while (parent instanceof ViewGroup) {
            if (canCircle((View) parent)) {
                return ViewHelper.getViewNode((View) parent);
            }
            parent = parent.getParent();
        }
        return null;
    }

    public static boolean canCircle(View view) {
        return view instanceof WebView ||
                view.getParent() instanceof AdapterView ||
                (view.isClickable() && view.hasOnClickListeners());
    }
}
