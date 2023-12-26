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
package com.growingio.android.debugger;

import com.growingio.android.sdk.LibraryGioModule;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.middleware.webservice.Debugger;
import com.growingio.android.sdk.track.middleware.webservice.WebService;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProvider;
import com.growingio.sdk.annotation.GIOLibraryModule;

import java.util.Map;

/**
 * <p>
 *
 * @author cpacm 5/19/21
 */
@GIOLibraryModule
public class DebuggerLibraryGioModule extends LibraryGioModule {
    @Override
    public void registerComponents(TrackerContext context) {
        context.getRegistry().register(Debugger.class, WebService.class, new DebuggerDataLoader.Factory(context));
    }

    @Override
    protected void setupProviders(Map<Class<? extends TrackerLifecycleProvider>, TrackerLifecycleProvider> providerStore) {
        providerStore.put(ScreenshotProvider.class, new ScreenshotProvider());
        providerStore.put(DebuggerEventProvider.class, new DebuggerEventProvider());
    }
}
