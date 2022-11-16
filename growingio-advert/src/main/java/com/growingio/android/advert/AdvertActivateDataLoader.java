/*
 *   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.growingio.android.advert;

import android.app.Activity;

import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.listener.OnConfigurationChangeListener;
import com.growingio.android.sdk.track.middleware.advert.Activate;
import com.growingio.android.sdk.track.middleware.advert.AdvertResult;
import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory;
import com.growingio.android.sdk.track.providers.ActivityStateProvider;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;

/**
 * <p>
 *
 * @author cpacm 2022/8/2
 */
public class AdvertActivateDataLoader implements ModelLoader<Activate, AdvertResult>, OnConfigurationChangeListener {

    AdvertActivateDataLoader() {
        ConfigurationProvider.get().addConfigurationListener(this);
    }

    @Override
    public LoadData<AdvertResult> buildLoadData(Activate activate) {
        return new LoadData<>(new ActivateDataFetcher(activate));
    }

    @Override
    public void onDataCollectionChanged(boolean isEnable) {
        // check app whether activated
        if (isEnable) {
            ActivateDataFetcher fetcher = new ActivateDataFetcher(null);
            fetcher.checkActivateStatus(null);
        }
    }

    public static class Factory implements ModelLoaderFactory<Activate, AdvertResult> {

        @Override
        public ModelLoader<Activate, AdvertResult> build() {
            return new AdvertActivateDataLoader();
        }
    }

    public static class ActivateDataFetcher implements DataFetcher<AdvertResult> {

        private final Activate activate;

        public ActivateDataFetcher(Activate activate) {
            this.activate = activate;
        }

        @Override
        public AdvertResult executeData() {
            Activity activity = ActivityStateProvider.get().getForegroundActivity();
            if (activity != null) {
                checkActivateStatus(activity);
            }
            return new AdvertResult();
        }

        @Override
        public Class<AdvertResult> getDataClass() {
            return AdvertResult.class;
        }

        /**
         * 发送激活事件(ui主线程)
         * 剪贴板数据受隐私政策影响，支持隐私政策不申明时禁止读取剪切板数据
         */
        void checkActivateStatus(Activity activity) {
            if (!ConfigurationProvider.core().isDataCollectionEnabled()) {
                return;
            }

            if (AdvertUtils.isDeviceActivated()) {
                return;
            }

            // just send activate event
            submitActivateEvent();
        }


        private void submitActivateEvent() {

            //原有逻辑：若intent被消费，则不上传剪贴板的数据,即applink优先度高于activate
            //现有逻辑：一律上传剪贴板的数据。
            final ActivateEvent.Builder builder = new ActivateEvent.Builder();
            TrackMainThread.trackMain().postEventToTrackMain(builder);
            AdvertUtils.setDeviceActivated();
        }

        private final static String TAG = "Activate";

    }

}
