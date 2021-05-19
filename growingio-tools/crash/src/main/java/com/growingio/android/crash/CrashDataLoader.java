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
package com.growingio.android.crash;

import android.content.Context;

import com.growingio.android.sdk.track.log.Crash;
import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory;

/**
 * <p>
 *
 * @author cpacm 5/19/21
 */
public class CrashDataLoader implements ModelLoader<Crash, Void> {
    public CrashDataLoader(Context context) {
        CrashManager.register(context);
    }

    @Override
    public LoadData<Void> buildLoadData(Crash crash) {
        return new LoadData<>(new DataFetcher<Void>() {
            @Override
            public void loadData(DataCallback<? super Void> callback) {
                callback.onDataReady(null);
            }

            @Override
            public Void executeData() {
                return null;
            }

            @Override
            public void cleanup() {

            }

            @Override
            public void cancel() {

            }

            @Override
            public Class<Void> getDataClass() {
                return Void.class;
            }
        });
    }

    public static class Factory implements ModelLoaderFactory<Crash, Void> {
        private final Context context;

        public Factory(Context context) {
            this.context = context;
        }

        @Override
        public ModelLoader<Crash, Void> build() {
            return new CrashDataLoader(context);
        }
    }
}
