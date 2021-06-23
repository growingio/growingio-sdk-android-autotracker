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

package com.growingio.android.sdk.track.http;


import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory;


class TestModelFactory<T, Y> implements ModelLoaderFactory<T, Y> {

    private final Y result;

    TestModelFactory(Y result) {
        this.result = result;
    }

    @Override
    public ModelLoader<T, Y> build() {
        return new ModelLoader<T, Y>() {
            @Override
            public LoadData<Y> buildLoadData(T t) {
                return new LoadData<>(new DataFetcher<Y>() {
                    @Override
                    public void loadData(DataCallback<? super Y> callback) {
                        callback.onDataReady(result);
                    }

                    @Override
                    public Y executeData() {
                        return result;
                    }

                    @Override
                    public void cleanup() {

                    }

                    @Override
                    public void cancel() {

                    }

                    @Override
                    public Class<Y> getDataClass() {
                        return (Class<Y>) result.getClass();
                    }
                });
            }
        };
    }
}
