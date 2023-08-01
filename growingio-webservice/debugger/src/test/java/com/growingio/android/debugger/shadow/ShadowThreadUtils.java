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
package com.growingio.android.debugger.shadow;


import com.growingio.android.sdk.track.TrackMainThread;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(TrackMainThread.class)
public class ShadowThreadUtils {

    public void doSomeThing() {
    }

    @Implementation
    public static void runOnUiThread(Runnable r) {
        r.run();
    }

    @Implementation
    public static void postOnUiThreadDelayed(Runnable task, long delayMillis) {
        task.run();
    }
}
