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

package com.growingio.android.sdk.track.providers;

import android.content.Intent;
import android.net.Uri;

import com.growingio.android.sdk.track.listener.IActivityLifecycle;
import com.growingio.android.sdk.track.listener.ListenerContainer;
import com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent;
import com.growingio.android.sdk.track.log.Logger;


import static com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent.EVENT_TYPE.ON_CREATED;
import static com.growingio.android.sdk.track.listener.event.ActivityLifecycleEvent.EVENT_TYPE.ON_NEW_INTENT;

/**
 * <p>
 * sdk 统一 url scheme 入口
 *
 * @author cpacm 2021/3/25
 */
public class DeepLinkProvider extends ListenerContainer<DeepLinkProvider.OnDeepLinkListener, Uri> implements IActivityLifecycle {

    private static final String TAG = "DeepLinkProvider";

    private static class SingleInstance {
        private static final DeepLinkProvider INSTANCE = new DeepLinkProvider();
    }

    private DeepLinkProvider() {
        ActivityStateProvider.get().registerActivityLifecycleListener(this);
    }

    public static DeepLinkProvider get() {
        return DeepLinkProvider.SingleInstance.INSTANCE;
    }

    @Override
    public void onActivityLifecycle(ActivityLifecycleEvent event) {
        if (event.eventType == ON_CREATED || event.eventType == ON_NEW_INTENT) {
            Intent intent = event.getIntent();
            if (intent == null) {
                return;
            }

            Uri data = intent.getData();
            if (data == null) {
                return;
            }
            String urlScheme = ConfigurationProvider.get().getTrackConfiguration().getUrlScheme();

            if (urlScheme.equals(data.getScheme())) {
                Logger.d(TAG, "enter growingio:" + data.toString());
                dispatchActions(data);
            }
        }
    }

    @Override
    protected void singleAction(OnDeepLinkListener listener, Uri action) {
        listener.onDeepLink(action);
    }


    public interface OnDeepLinkListener {
        void onDeepLink(Uri uri);
    }
}
