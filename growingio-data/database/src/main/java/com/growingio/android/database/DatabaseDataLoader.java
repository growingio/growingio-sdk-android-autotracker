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

package com.growingio.android.database;

import android.content.Context;

import com.growingio.android.sdk.track.middleware.EventDatabase;
import com.growingio.android.sdk.track.middleware.EventDbResult;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory;


/**
 * <p>
 *
 * @author cpacm 2021/11/26
 */
public class DatabaseDataLoader implements ModelLoader<EventDatabase, EventDbResult> {

    private final EventDataManager client;

    public DatabaseDataLoader(EventDataManager client) {
        this.client = client;
    }

    @Override
    public LoadData<EventDbResult> buildLoadData(EventDatabase database) {
        return new LoadData<>(new DatabaseDataFetcher(client, database));
    }

    public static class Factory implements ModelLoaderFactory<EventDatabase, EventDbResult> {

        private final Context context;

        public Factory(Context context) {
            this.context = context;
        }

        @Override
        public ModelLoader<EventDatabase, EventDbResult> build() {
            return new DatabaseDataLoader(new EventDataManager(context));
        }
    }
}
