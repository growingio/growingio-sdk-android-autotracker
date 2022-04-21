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

import android.webkit.WebView;

import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory;
import com.growingio.android.sdk.track.modelloader.data.HybridBridge;
import com.growingio.android.sdk.track.utils.ClassExistHelper;

/**
 * <p>
 *
 * @author cpacm 2021/5/13
 */
public class HybridBridgeLoader implements ModelLoader<HybridBridge, Boolean> {

    @Override
    public LoadData<Boolean> buildLoadData(HybridBridge eventData) {
        return new LoadData<>(new HybridDataFetcher(eventData));
    }

    public static class Factory implements ModelLoaderFactory<HybridBridge, Boolean> {
        @Override
        public ModelLoader<HybridBridge, Boolean> build() {
            return new HybridBridgeLoader();
        }
    }


    public static class HybridDataFetcher implements DataFetcher<Boolean> {
        private static final String TAG = "HybridDataFetcher";

        private final HybridBridge bridge;

        public HybridDataFetcher(HybridBridge eventData) {
            this.bridge = eventData;
        }


        @Override
        public Boolean executeData() {
            if (bridge.getView() instanceof WebView) {
                HybridBridgeProvider.get().bridgeForWebView(SuperWebView.make((WebView) bridge.getView()));
            } else if (ClassExistHelper.instanceOfX5WebView(bridge.getView())) {
                HybridBridgeProvider.get().bridgeForWebView(SuperWebView.makeX5((com.tencent.smtt.sdk.WebView) bridge.getView()));
            } else if (ClassExistHelper.instanceOfUcWebView(bridge.getView())) {
                HybridBridgeProvider.get().bridgeForWebView(SuperWebView.makeUC((com.uc.webview.export.WebView) bridge.getView()));
            } else {
                return false;
            }
            return true;
        }

        @Override
        public Class<Boolean> getDataClass() {
            return Boolean.class;
        }

    }
}
