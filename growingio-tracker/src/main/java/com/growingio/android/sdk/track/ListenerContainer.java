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

package com.growingio.android.sdk.track;

import com.growingio.android.sdk.track.base.event.ViewTreeStatusChangeEvent;
import com.growingio.android.sdk.track.interfaces.IViewTreeStatus;
import com.growingio.android.sdk.track.interfaces.OnTrackMainInitSDKCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ListenerContainer<L, A> {

    private final Set<L> mListeners = new HashSet<>();

    public static ViewTreeStatusListeners viewTreeStatusListeners() {
        return ViewTreeStatusListeners.Instance.VIEW_TREE_STATUS_LISTENERS;
    }

    public static OnGIOMainInitSDKListeners gioMainInitSDKListeners() {
        return OnGIOMainInitSDKListeners.Instance.MAIN_INIT_SDK_LISTENERS;
    }

    public synchronized void register(L listener) {
        mListeners.add(listener);
    }

    public synchronized void unRegister(L listener) {
        mListeners.remove(listener);
    }

    private synchronized List<L> copyListener() {
        if (mListeners.size() == 0) {
            return Collections.emptyList();
        } else {
            return new ArrayList<>(mListeners);
        }
    }

    protected void dispatchActions(A action) {
        List<L> listeners = copyListener();
        for (L listener : listeners) {
            singleAction(listener, action);
        }
    }

    abstract void singleAction(L listener, A action);

    public static class OnGIOMainInitSDKListeners extends ListenerContainer<OnTrackMainInitSDKCallback, Void> {

        @Override
        void singleAction(OnTrackMainInitSDKCallback listener, Void action) {
            listener.onTrackMainInitSDK();
        }

        private static class Instance {
            private static final OnGIOMainInitSDKListeners MAIN_INIT_SDK_LISTENERS = new OnGIOMainInitSDKListeners();
        }
    }

    public static class ViewTreeStatusListeners extends ListenerContainer<IViewTreeStatus, ViewTreeStatusChangeEvent> implements IViewTreeStatus {
        @Override
        void singleAction(IViewTreeStatus listener, ViewTreeStatusChangeEvent action) {
            listener.onViewTreeStatusChanged(action);
        }

        @Override
        public void onViewTreeStatusChanged(ViewTreeStatusChangeEvent action) {
            dispatchActions(action);
        }

        private static class Instance {
            private static final ViewTreeStatusListeners VIEW_TREE_STATUS_LISTENERS = new ViewTreeStatusListeners();
        }
    }
}
