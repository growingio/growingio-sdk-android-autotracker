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

package com.growingio.android.sdk.track.listener;

import com.growingio.android.sdk.track.log.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class ListenerContainer<L, A> {
    private static final String TAG = "ListenerContainer";

    private final List<L> mListeners = new ArrayList<>();

    public void register(L listener) {
        synchronized (mListeners) {
            boolean needsAdd = true;
            Iterator<L> refIter = mListeners.iterator();
            while (refIter.hasNext()) {
                L storedListener = refIter.next();
                if (null == storedListener) {
                    refIter.remove();
                } else if (storedListener == listener) {
                    needsAdd = false;
                }
            }
            if (needsAdd) {
                mListeners.add(listener);
            }
        }
    }

    public void unregister(L listener) {
        synchronized (mListeners) {
            Iterator<L> refIter = mListeners.iterator();
            while (refIter.hasNext()) {
                L storedListener = refIter.next();
                if (null == storedListener) {
                    refIter.remove();
                } else if (storedListener == listener) {
                    refIter.remove();
                }
            }
        }
    }

    protected void dispatchActions(A action) {
        synchronized (mListeners) {
            Iterator<L> refIter = mListeners.iterator();
            while (refIter.hasNext()) {
                L listener = refIter.next();
                if (null == listener) {
                    refIter.remove();
                } else {
                    try {
                        singleAction(listener, action);
                    } catch (Exception e) {
                        Logger.e(TAG, e);
                    }
                }
            }
        }
    }

    abstract protected void singleAction(L listener, A action);
}