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

package com.growingio.android.sdk.autotrack.webservices.circle;

import com.growingio.android.sdk.autotrack.webservices.ScreenshotProvider;
import com.growingio.android.sdk.autotrack.webservices.circle.entity.CircleScreenshot;
import com.growingio.android.sdk.track.async.Callback;
import com.growingio.android.sdk.track.async.Disposable;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.webservices.BaseWebSocketService;
import com.growingio.android.sdk.track.webservices.log.MobileLogService;

import org.json.JSONException;
import org.json.JSONObject;

public class CircleService extends BaseWebSocketService implements ScreenshotProvider.OnScreenshotRefreshedListener {
    private static final String TAG = "CircleService";

    private static final String SERVICE_TYPE = "CircleService";

    private static final String LOGGER_OPEN = "logger_open";

    private static final String LOGGER_HOST = "wss://gta1.growingio.com/app/";

    private long mSnapshotKey = 0;
    private Disposable mCircleScreenshotDisposable;

    public CircleService(String wsUrl) {
        super(wsUrl);
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    protected void onReady() {
        registerScreenshotRefreshedListener();
    }

    @Override
    protected void onFailed() {
        Logger.e(TAG, "Start CircleService Failed");
    }

    @Override
    protected void onQuited() {
        destroy();
    }

    @Override
    protected void onMessage(String text) {
        try {
            JSONObject message = new JSONObject(text);
            String msgType = message.optString("msgType");
            if (MobileLogService.SERVICE_TYPE.equals(msgType)) {
                // TODO: 2020/8/31  启动MobileLog
            }
        } catch (JSONException e) {
            Logger.e(TAG, e);
        }
    }

    private void registerScreenshotRefreshedListener() {
        ScreenshotProvider.get().registerScreenshotRefreshedListener(this);
    }

    public void destroy() {
        ScreenshotProvider.get().unregisterScreenshotRefreshedListener(this);
    }

    @Override
    public void onScreenshotRefreshed(String screenshotBase64, float scale) {
        if (mCircleScreenshotDisposable != null) {
            mCircleScreenshotDisposable.dispose();
        }

        mCircleScreenshotDisposable = new CircleScreenshot.Builder()
                .setScale(scale)
                .setScreenshot(screenshotBase64)
                .setSnapshotKey(mSnapshotKey++)
                .build(new Callback<CircleScreenshot>() {
                    @Override
                    public void onSuccess(CircleScreenshot result) {
                        Logger.d(TAG, "Create circle screenshot successfully");
                        sendMessage(result.toJSONObject().toString());
                    }

                    @Override
                    public void onFailed() {
                        Logger.e(TAG, "Create circle screenshot failed");
                    }
                });
    }
}
