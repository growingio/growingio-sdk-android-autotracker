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

package com.growingio.android.sdk.autotrack.view;

import android.view.View;

import androidx.annotation.Nullable;

public class ViewStateChangedEvent {

    private final StateType mStateType;
    private View mOldFocus;
    private View mNewFocus;

    public ViewStateChangedEvent(StateType stateType) {
        this.mStateType = stateType;
    }

    public ViewStateChangedEvent(StateType stateType, View oldFocus, View newFocus) {
        this.mStateType = stateType;
        this.mOldFocus = oldFocus;
        this.mNewFocus = newFocus;
    }

    public StateType getStateType() {
        return mStateType;
    }

    @Nullable
    public View getOldFocus() {
        return mOldFocus;
    }

    @Nullable
    public View getNewFocus() {
        return mNewFocus;
    }

    public enum StateType {
        FOCUS_CHANGED,
        LAYOUT_CHANGED,
        SCROLL_CHANGED,
        DRAW,
        WINDOW_FOCUS_CHANGED
    }
}
