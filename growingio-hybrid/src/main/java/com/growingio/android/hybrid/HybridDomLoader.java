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

import android.view.View;
import android.webkit.WebView;

import com.growingio.android.sdk.track.async.Callback;
import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory;
import com.growingio.android.sdk.track.modelloader.data.HybridDom;
import com.growingio.android.sdk.track.modelloader.data.HybridJson;

import org.json.JSONObject;

/**
 * <p>
 *
 * @author cpacm 2021/5/13
 */
public class HybridDomLoader implements ModelLoader<HybridDom, HybridJson> {

    private HybridDom.OnDomChangedListener onDomChangedListener;

    @Override
    public LoadData<HybridJson> buildLoadData(HybridDom eventData) {
        if (eventData.getOnDomChangedListener() != null) {
            this.onDomChangedListener = eventData.getOnDomChangedListener();
            HybridBridgeProvider.get().registerDomChangedListener(() -> onDomChangedListener.onDomChanged());
        }
        return new LoadData<>(new HybridDataFetcher(eventData));
    }

    public static class Factory implements ModelLoaderFactory<HybridDom, HybridJson> {
        @Override
        public ModelLoader<HybridDom, HybridJson> build() {
            return new HybridDomLoader();
        }
    }


    public static class HybridDataFetcher implements DataFetcher<HybridJson> {
        private static final String TAG = "HybridDataFetcher";

        private final HybridDom dom;

        public HybridDataFetcher(HybridDom eventData) {
            this.dom = eventData;
        }

        @Override
        public void loadData(DataCallback<? super HybridJson> callback) {
            if (dom.getView() == null) {
                callback.onLoadFailed(new NullPointerException("webview is null"));
            }
            SuperWebView<? extends View> superWebView = getSuperWebView();
            if (superWebView == null) {
                callback.onLoadFailed(new IllegalArgumentException(dom.getView().getClass().getName() + "is not webView"));
                return;
            }
            HybridBridgeProvider.get().getWebViewDomTree(superWebView, new Callback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject result) {
                    callback.onDataReady(new HybridJson(result));
                }

                @Override
                public void onFailed() {
                    callback.onLoadFailed(new RuntimeException("getWebViewDomTree error"));
                }
            });
        }

        public SuperWebView<? extends View> getSuperWebView() {
            if (dom.getView() instanceof WebView) {
                return SuperWebView.make((WebView) dom.getView());
            } else if (dom.getView() instanceof com.tencent.smtt.sdk.WebView) {
                return SuperWebView.makeX5((com.tencent.smtt.sdk.WebView) dom.getView());
            } else if (dom.getView() instanceof com.uc.webview.export.WebView) {
                return SuperWebView.makeUC((com.uc.webview.export.WebView) dom.getView());
            } else {
                return null;
            }
        }

        @Override
        public HybridJson executeData() {
            // for register dom change listener
            return new HybridJson(new JSONObject());
        }

        @Override
        public void cleanup() {
        }

        @Override
        public void cancel() {
        }

        @Override
        public Class<HybridJson> getDataClass() {
            return HybridJson.class;
        }

    }
}
