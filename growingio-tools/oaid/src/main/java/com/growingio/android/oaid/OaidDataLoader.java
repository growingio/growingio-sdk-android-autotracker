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
package com.growingio.android.oaid;


import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory;
import com.growingio.android.sdk.track.middleware.OaidHelper;

public class OaidDataLoader implements ModelLoader<OaidHelper, String> {
    private final TrackerContext mContext;
    private volatile IOaidHelper mOaidHelper;

    public OaidDataLoader(TrackerContext context) {
        this.mContext = context;
    }

    @Override
    public LoadData<String> buildLoadData(OaidHelper oaidHelper) {
        OaidConfig config = mContext.getConfigurationProvider().getConfiguration(OaidConfig.class);
        if (config == null) config = new OaidConfig();
        if (config.getProvideOaidCallback() != null || (config.getProvideOaid() != null && !config.getProvideOaid().isEmpty())) {
            if (mOaidHelper == null || mOaidHelper instanceof OaidCertHelper) {
                mOaidHelper = new OaidDirectlyHelper(mContext.getBaseContext(), config);
            }
        } else {
            if (mOaidHelper == null || mOaidHelper instanceof OaidDirectlyHelper) {
                mOaidHelper = new OaidCertHelper(mContext.getBaseContext(), config);
            }
        }
        return new LoadData<>(new OaidDataFetcher(mContext, mOaidHelper));
    }

    public static class Factory implements ModelLoaderFactory<OaidHelper, String> {
        private final TrackerContext mContext;

        public Factory(TrackerContext context) {
            this.mContext = context;
        }

        @Override
        public ModelLoader<OaidHelper, String> build() {
            return new OaidDataLoader(mContext);
        }
    }
}
