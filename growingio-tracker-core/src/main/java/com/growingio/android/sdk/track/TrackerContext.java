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

import android.content.Context;
import android.content.ContextWrapper;

import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.modelloader.TrackerRegistry;

public class TrackerContext extends ContextWrapper {
    private final static String TAG = "ContextProvider";
    private static volatile TrackerContext INSTANCE = null;

    private TrackerContext(Context context) {
        super(context);
        registry = new TrackerRegistry();
    }

    public static void init(Context context) {
        synchronized (TrackerContext.class) {
            if (null == INSTANCE) {
                INSTANCE = new TrackerContext(context);
            }
        }
    }

    public static TrackerContext get() {
        if (null == INSTANCE) {
            Logger.e(TAG, new NullPointerException("you should init growingio sdk first"));
        }
        return INSTANCE;
    }

    private final TrackerRegistry registry;

    public TrackerRegistry getRegistry() {
        return registry;
    }

}
