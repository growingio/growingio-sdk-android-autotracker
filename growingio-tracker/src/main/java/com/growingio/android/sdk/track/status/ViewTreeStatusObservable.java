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

package com.growingio.android.sdk.track.status;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.ViewTreeObserver;

import com.growingio.android.sdk.track.base.event.ViewTreeStatusChangeEvent;
import com.growingio.android.sdk.track.ListenerContainer;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
public class ViewTreeStatusObservable implements ViewTreeObserver.OnGlobalLayoutListener,
        ViewTreeObserver.OnScrollChangedListener,
        ViewTreeObserver.OnGlobalFocusChangeListener,
        ViewTreeObserver.OnDrawListener {

    public static volatile ViewTreeStatusObservable viewTreeStatusObservable;

    public static ViewTreeStatusObservable getInstance() {
        if (viewTreeStatusObservable == null) {
            synchronized (ViewTreeStatusObservable.class) {
                if (viewTreeStatusObservable == null) {
                    viewTreeStatusObservable = new ViewTreeStatusObservable();
                }
            }
        }
        return viewTreeStatusObservable;
    }

    @Override
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
        ListenerContainer.viewTreeStatusListeners().onViewTreeStatusChanged(new ViewTreeStatusChangeEvent(ViewTreeStatusChangeEvent.StatusType.FocusChanged, oldFocus, newFocus));
    }

    @Override
    public void onGlobalLayout() {
        ListenerContainer.viewTreeStatusListeners().onViewTreeStatusChanged(new ViewTreeStatusChangeEvent(ViewTreeStatusChangeEvent.StatusType.LayoutChanged));
    }

    @Override
    public void onScrollChanged() {
        ListenerContainer.viewTreeStatusListeners().onViewTreeStatusChanged(new ViewTreeStatusChangeEvent(ViewTreeStatusChangeEvent.StatusType.ScrollChanged));
    }

    @Override
    public void onDraw() {
        ListenerContainer.viewTreeStatusListeners().onViewTreeStatusChanged(new ViewTreeStatusChangeEvent(ViewTreeStatusChangeEvent.StatusType.Draw));
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static class FocusListener implements ViewTreeObserver.OnWindowFocusChangeListener {

        private static FocusListener sInstance = null;

        public static FocusListener getInstance() {
            if (sInstance == null) {
                sInstance = new FocusListener();
            }
            return sInstance;
        }

        @Override
        public void onWindowFocusChanged(boolean hasFocus) {
            ListenerContainer.viewTreeStatusListeners().onViewTreeStatusChanged(new ViewTreeStatusChangeEvent(ViewTreeStatusChangeEvent.StatusType.WindowFouchChanged));
        }
    }
}
