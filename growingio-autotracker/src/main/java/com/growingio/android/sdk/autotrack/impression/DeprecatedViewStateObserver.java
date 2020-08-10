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

package com.growingio.android.sdk.autotrack.impression;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewTreeObserver;

class DeprecatedViewStateObserver implements ViewTreeObserver.OnGlobalLayoutListener,
        ViewTreeObserver.OnScrollChangedListener,
        ViewTreeObserver.OnGlobalFocusChangeListener,
        ViewTreeObserver.OnDrawListener {

    protected final OnViewStateChangedListener mViewStateChangedListener;

    DeprecatedViewStateObserver(@NonNull OnViewStateChangedListener viewStateChangedListener) {
        mViewStateChangedListener = viewStateChangedListener;
    }

    @Override
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
        mViewStateChangedListener.onViewStateChanged(new ViewStateChangedEvent(ViewStateChangedEvent.StateType.FOCUS_CHANGED, oldFocus, newFocus));
    }

    @Override
    public void onGlobalLayout() {
        mViewStateChangedListener.onViewStateChanged(new ViewStateChangedEvent(ViewStateChangedEvent.StateType.LAYOUT_CHANGED));
    }

    @Override
    public void onScrollChanged() {
        mViewStateChangedListener.onViewStateChanged(new ViewStateChangedEvent(ViewStateChangedEvent.StateType.SCROLL_CHANGED));
    }

    @Override
    public void onDraw() {
        mViewStateChangedListener.onViewStateChanged(new ViewStateChangedEvent(ViewStateChangedEvent.StateType.DRAW));
    }
}
