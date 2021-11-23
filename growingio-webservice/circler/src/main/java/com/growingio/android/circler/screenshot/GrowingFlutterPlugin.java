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

package com.growingio.android.circler.screenshot;

import android.view.View;

import com.growingio.android.sdk.track.log.Logger;

import java.util.Map;

public class GrowingFlutterPlugin {
    private static GrowingFlutterPlugin Instance;
    private GrowingFlutterPlugin() { }

    public static GrowingFlutterPlugin getInstance() {
        if (Instance == null) {
            Instance = new GrowingFlutterPlugin();
        }
        return Instance;
    }
    public Boolean isStart;
    private OnNativeListener mlistener;
    private Map<String, Object> flutterData;

    public interface OnNativeListener {
        void onNativeCircleStart();
        void onNativeCircleStop();
    }
    public void addNativeListener(OnNativeListener listener) {
        mlistener = listener;
    }

    public void onNativeCircleStart() {
        isStart = true;
        if (this.mlistener != null) {
            this.mlistener.onNativeCircleStart();
        }
    }

    public void onNativeCircleStop() {
        isStart = false;
        flutterData = null;
        if (this.mlistener != null) {
            this.mlistener.onNativeCircleStop();
        }
    }
    public void onFlutterCircleData(Map<String, Object> data) {
        Logger.e("FlutterPlugin", "set flutterData " + data.toString());
        flutterData = data;
    }

    public Map<String, Object> getFlutterData() {
        if (flutterData != null) {
            Logger.e("FlutterPlugin", "get flutterData " + flutterData.toString());
        }
        Map<String, Object> data = flutterData;
        flutterData = null;
        return data;
    }


    public Boolean isFlutterView(View mview) {
        try {
            Logger.e("FlutterPlugin", "view is" + mview.toString());
            Class<?> clazz = Class.forName("io.flutter.embedding.android.FlutterSplashView");
            if (clazz != null && clazz.isInstance(mview)) {
                return true;
            }
            return false;
        } catch (Throwable e) {
            return false;
        }
    }
}
