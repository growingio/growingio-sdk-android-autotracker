/*
 * Copyright (C) 2023 Beijing Yishu Technology Co., Ltd.
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
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 *
 * @author cpacm 2022/10/26
 */
public abstract class ListenerDispatcher<L> {

    private static final String TAG = "ListenerDispatcher";

    protected final List<L> mListeners = new ArrayList<>();

    protected void register(L listener) {
        synchronized (mListeners) {
            boolean needsAdd = true;
            Iterator<L> refIter = mListeners.iterator();
            while (refIter.hasNext()) {
                try {
                    L storedListener = refIter.next();
                    if (null == storedListener) {
                        refIter.remove();
                    } else if (storedListener == listener) {
                        needsAdd = false;
                    }
                } catch (ConcurrentModificationException e) {
                    Logger.e(TAG, "Please avoid call register method in dispatchActions");
                    throw e;
                }
            }
            if (needsAdd) {
                mListeners.add(listener);
            }
        }
    }

    protected void unregister(L listener) {
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

    protected int getListenerCount() {
        return mListeners.size();
    }

}
