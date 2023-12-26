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
package com.growingio.android.sdk.autotrack;

import com.growingio.android.sdk.AppGioModule;
import com.growingio.sdk.annotation.GIOAppModule;
import com.growingio.sdk.annotation.GIOTracker;

@GIOAppModule(name = "GrowingAutotracker", configName = "AutotrackConfiguration")
public class GrowingAutoAppModule extends AppGioModule {

    @GIOTracker(path = Autotracker.class)
    public void config(AutotrackConfiguration config) {
    }
}
