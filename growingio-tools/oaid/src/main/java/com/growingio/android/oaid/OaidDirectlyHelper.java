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

import android.content.Context;

import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.log.Logger;

/**
 * <p>
 * 直接提供Oaid
 *
 * @author cpacm 2022/1/14
 */
public class OaidDirectlyHelper implements IOaidHelper {

    private final OaidConfig config;
    private volatile String oaid;
    private volatile boolean needPreload = true;

    public OaidDirectlyHelper(Context context, OaidConfig oaidConfig) {
        this.config = oaidConfig;
        if (config.getProvideOaid() != null && !config.getProvideOaid().isEmpty()) {
            this.oaid = config.getProvideOaid();
            needPreload = false;
        } else {
            preloadOaid(context);
        }
    }

    @Override
    public void preloadOaid(Context context) {
        if (!needPreload || config.getProvideOaidCallback() == null) return;
        synchronized (this) {
            TrackMainThread.trackMain().postActionToTrackMain(() -> {
                this.oaid = config.getProvideOaidCallback().provideOaidJob(context);
                needPreload = false;
            });
        }
    }

    @Override
    public String getOaid() {
        if (this.oaid == null) {
            synchronized (this) {
                while (this.needPreload) {
                    try {
                        this.wait(3000L);
                    } catch (InterruptedException var4) {
                        Logger.e("OaidDirectlyHelper", var4, "waitCompleteAndGetOaid interrupted", new Object[0]);
                        Thread.currentThread().interrupt();
                    }

                    if (this.needPreload) {
                        Logger.d("OaidDirectlyHelper", "it's too long to get oaid, and reject get oaid", new Object[0]);
                        break;
                    }
                    this.needPreload = false;
                }
            }
        }
        return this.oaid;
    }
}
