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

package com.growingio.android.sdk.track.view;

import android.graphics.Rect;
import android.view.View;
import android.view.WindowManager;

public class DecorView {
    private final View mView;
    private final Rect mRect;
    private final WindowManager.LayoutParams mLayoutParams;

    public DecorView(View view, Rect rect, WindowManager.LayoutParams layoutParams) {
        mView = view;
        mRect = rect;
        mLayoutParams = layoutParams;
    }

    public View getView() {
        return mView;
    }

    public Rect getRect() {
        return mRect;
    }

    public WindowManager.LayoutParams getLayoutParams() {
        return mLayoutParams;
    }

    public boolean isDialog() {
        return mLayoutParams.type == WindowManager.LayoutParams.TYPE_APPLICATION;
    }
}
