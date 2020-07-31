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

import android.content.Context;

import com.growingio.android.sdk.track.interfaces.GMainThread;
import com.growingio.android.sdk.track.ipc.GrowingIOIPC;
import com.growingio.android.sdk.track.providers.SessionProvider;
import com.growingio.android.sdk.track.utils.PersistUtil;

/**
 * CoreAppState与GIOMainThread作为全局引用的入口而已
 */
public class CoreAppState {

    public static final String TAG = "GIO.AppState";

    private Context mGlobalContext;
    private GIOMainThread mGIOMain;
    private GrowingIOIPC mGrowingIOIPC;


    CoreAppState(Context context, GIOMainThread gioMain) {
        mGlobalContext = context;
        this.mGIOMain = gioMain;
    }

    public GrowingIOIPC getGrowingIOIPC() {
        return mGrowingIOIPC;
    }

    public Context getGlobalContext() {
        return mGlobalContext;
    }

    public GIOMainThread getGIOMain() {
        return mGIOMain;
    }

    @GMainThread
    void initInGIOMain() {
        PersistUtil.init(mGlobalContext);
        mGrowingIOIPC = new GrowingIOIPC();
        mGrowingIOIPC.init(mGlobalContext, GConfig.getInstance());
        SessionProvider.SessionPolicy.get(this).onGIOMainInitSDK();
    }
}
