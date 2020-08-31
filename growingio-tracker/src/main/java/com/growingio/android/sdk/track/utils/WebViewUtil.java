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

package com.growingio.android.sdk.track.utils;

import android.webkit.WebView;

import com.growingio.android.sdk.track.log.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class WebViewUtil {
    private static final String TAG = "GIO.WebViewUtil";

    private WebViewUtil() {
    }

    public static boolean isDestroyed(WebView webView) {
        try {
            Field providerField = WebView.class.getDeclaredField("mProvider");
            providerField.setAccessible(true);
            Object provider = providerField.get(webView);
            if ("android.webkit.WebViewClassic".equals(provider)) {
                return isDestroyedWebViewClassic(provider);
            }
            Field awContentField = provider.getClass().getDeclaredField("mAwContents");
            awContentField.setAccessible(true);
            Object awContent = awContentField.get(provider);

            Method isDestroyed = awContent.getClass().getDeclaredMethod("isDestroyed", int.class);
            isDestroyed.setAccessible(true);

            Object isDestroy = isDestroyed.invoke(awContent, 0);
            if (isDestroy instanceof Boolean) {
                return (Boolean) isDestroy;
            }
        } catch (Exception e) {
            // 有部分Chromium 代码被混淆, 反射必定报错, 另外在新版的WebView中, 内部有isDestroyed判断， 不会触发Bug， 可以安全忽略该异常
            Logger.d(TAG, "isDestroyed() should ignore: %s", e.getMessage());
        }

        return false;
    }

    private static boolean isDestroyedWebViewClassic(Object webViewClassic) throws Exception {
        Field field = webViewClassic.getClass().getDeclaredField("mWebViewCore");
        field.setAccessible(true);
        return field.get(webViewClassic) == null;
    }
}
