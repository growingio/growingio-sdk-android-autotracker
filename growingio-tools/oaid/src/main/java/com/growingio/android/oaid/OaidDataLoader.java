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

package com.growingio.android.oaid;

import android.content.Context;

import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.middleware.OaidHelper;

public class OaidDataLoader implements ModelLoader<OaidHelper, String> {
    private final Context mContext;
    private final IOaidHelper mOaidHelper;

    public OaidDataLoader(Context context, IOaidHelper oaidHelper) {
        this.mContext = context;
        this.mOaidHelper = oaidHelper;
    }

    @Override
    public LoadData<String> buildLoadData(OaidHelper oaidHelper) {
        return new LoadData<>(new OaidDataFetcher(mContext, mOaidHelper));
    }

    public static class Factory implements ModelLoaderFactory<OaidHelper, String> {
        private final Context mContext;
        private volatile IOaidHelper sOaidHelper;

        public Factory(Context context) {
            this.mContext = context;
            OaidConfig config = ConfigurationProvider.get().getConfiguration(OaidConfig.class);
            if (config == null) config = new OaidConfig();
            initOaidSdk(context, config);
        }

        protected Factory(Context context, OaidConfig config) {
            this.mContext = context;
            initOaidSdk(context, config);
        }

        private void initOaidSdk(Context context, OaidConfig config) {
            //提前初始化，以便oaid sdk 加载so包
            if (sOaidHelper == null) {
                synchronized (Factory.class) {
                    if (sOaidHelper == null) {
                        // 用户直接提供oaid值情况下
                        if (config.getProvideOaidCallback() != null || (config.getProvideOaid() != null && !config.getProvideOaid().isEmpty())) {
                            sOaidHelper = new OaidDirectlyHelper(context, config);
                        } else {
                            sOaidHelper = new OaidCertHelper(context, config);
                        }
                    }
                }
            }
        }

        @Override
        public ModelLoader<OaidHelper, String> build() {
            return new OaidDataLoader(mContext, sOaidHelper);
        }
    }
}
