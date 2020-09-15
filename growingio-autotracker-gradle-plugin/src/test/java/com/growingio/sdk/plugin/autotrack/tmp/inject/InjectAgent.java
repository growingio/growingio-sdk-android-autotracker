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

package com.growingio.sdk.plugin.autotrack.tmp.inject;

import com.growingio.sdk.plugin.autotrack.tmp.Callback;
import com.growingio.sdk.plugin.autotrack.tmp.SuperExample;

public class InjectAgent {
    private static Callback sCallback;

    private InjectAgent() {
    }

    public static void setsCallback(Callback sCallback) {
        InjectAgent.sCallback = sCallback;
    }

    public static void onExecute(SuperExample example) {
        System.out.println("InjectAgent = " + example);
        if (sCallback != null) {
            sCallback.onCallback(example);
        }
    }
}
