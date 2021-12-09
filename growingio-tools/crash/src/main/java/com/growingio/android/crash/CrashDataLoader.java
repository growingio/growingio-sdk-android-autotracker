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
    public CrashDataLoader(Context context, String dsn, String alias) {
        CrashManager.register(context, dsn, alias);
    }

    @Override
    public LoadData<Void> buildLoadData(Crash crash) {
        return new LoadData<>(new DataFetcher<Void>() {

            @Override
            public Void executeData() {
                return null;
            }

            @Override
            public Class<Void> getDataClass() {
                return Void.class;
            }
        });
    }

    public static class Factory implements ModelLoaderFactory<Crash, Void> {
        private final Context context;
        private final String dsn;
        private final String alias;

        public Factory(Context context, String dsn, String alias) {
            this.context = context;
            this.dsn = dsn;
            this.alias = alias;
        }

        @Override
        public ModelLoader<Crash, Void> build() {
            return new CrashDataLoader(context, dsn, alias);
        }
    }
}
