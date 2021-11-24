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

package com.growingio.database;

import android.content.Context;

import com.growingio.android.sdk.LibraryGioModule;
import com.growingio.android.sdk.track.middleware.EventDatabase;
import com.growingio.android.sdk.track.middleware.EventDbResult;
import com.growingio.android.sdk.track.modelloader.TrackerRegistry;
import com.growingio.sdk.annotation.GIOLibraryModule;

/**
 * <p>
 *
 * @author cpacm 11/26/21
 */
@GIOLibraryModule
public class DatabaseLibraryModule extends LibraryGioModule {
    @Override
    public void registerComponents(Context context, TrackerRegistry registry) {
        registry.register(EventDatabase.class, EventDbResult.class, new DatabaseDataLoader.Factory(context));
    }
}
