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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ListenerContainer<L, A> {

    private Set<L> mListeners = new HashSet<>();

    public synchronized void register(L listener) {
        mListeners.add(listener);
    }

    public synchronized void unregister(L listener) {
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

    abstract protected void singleAction(L listener, A action);
}