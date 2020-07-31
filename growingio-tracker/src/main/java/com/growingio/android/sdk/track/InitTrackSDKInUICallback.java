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

package com.growingio.android.sdk.track;

import android.app.Application;
import android.support.annotation.UiThread;

import com.growingio.android.sdk.track.utils.GIOProviders;

public interface InitTrackSDKInUICallback {
    /**
     * 初始化SDK, 对外保留的唯一接口
     */
    @UiThread
    void initSDK(GrowingTracker gio);

    // 一下接口均由initSDK调用
    // 初始化埋点SDK
    void initTrackSDK(GrowingTracker gio);

    void initCdp();

    void initDebug();

    class InitTrackSDKInUIPolicy implements InitTrackSDKInUICallback {

        private Application mApplication;

        public InitTrackSDKInUIPolicy(Application application) {
            this.mApplication = application;
        }

        public static InitTrackSDKInUICallback get(final Application application) {
            return GIOProviders.provider(InitTrackSDKInUICallback.class, new GIOProviders.DefaultCallback<InitTrackSDKInUICallback>() {
                @Override
                public InitTrackSDKInUICallback value() {
                    return new InitTrackSDKInUIPolicy(application);
                }
            });
        }

        @Override
        public void initSDK(GrowingTracker growingTracker) {
            initDebug();
            initTrackSDK(growingTracker);
            initCdp();
        }

        @Override
        public void initTrackSDK(GrowingTracker growingTracker) {
            GrowingTracker.initSDK(mApplication, growingTracker);
        }

        @Override
        public void initCdp() {

        }

        @Override
        public void initDebug() {

        }
    }
}
