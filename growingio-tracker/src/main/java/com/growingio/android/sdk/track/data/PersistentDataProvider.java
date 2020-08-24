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

package com.growingio.android.sdk.track.data;

import android.content.Context;

import com.growingio.android.sdk.track.ContextProvider;
import com.growingio.android.sdk.track.ipc.GrowingIOIPC;

public class PersistentDataProvider {
    private final GrowingIOIPC mIPC;
    private final EventSequenceIdPolicy mEventSequenceIdPolicy;

    private static class SingleInstance {
        private static final PersistentDataProvider INSTANCE = new PersistentDataProvider();
    }

    private PersistentDataProvider() {
        Context context = ContextProvider.getApplicationContext();
        mIPC = new GrowingIOIPC(context);
        mEventSequenceIdPolicy = new EventSequenceIdPolicy(context);
    }

    public static PersistentDataProvider get() {
        return SingleInstance.INSTANCE;
    }

    public GrowingIOIPC getIPC() {
        return mIPC;
    }

    public EventSequenceId getAndIncrement(String eventType) {
        return mEventSequenceIdPolicy.getAndIncrement(eventType);
    }
}