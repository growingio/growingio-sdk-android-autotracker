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
package com.growingio.android.hybrid;

import android.content.Context;

import com.growingio.android.sdk.LibraryGioModule;
import com.growingio.android.sdk.track.modelloader.TrackerRegistry;
import com.growingio.android.sdk.track.middleware.hybrid.HybridBridge;
import com.growingio.android.sdk.track.middleware.hybrid.HybridDom;
import com.growingio.android.sdk.track.middleware.hybrid.HybridJson;
import com.growingio.sdk.annotation.GIOLibraryModule;

/**
 * <p>
 *
 * @author cpacm 5/19/21
 */
@GIOLibraryModule
public class HybridLibraryGioModule extends LibraryGioModule {
    @Override
    public void registerComponents(Context context, TrackerRegistry registry) {
        registry.register(HybridBridge.class, Boolean.class, new HybridBridgeLoader.Factory());
        registry.register(HybridDom.class, HybridJson.class, new HybridDomLoader.Factory());
    }
}
