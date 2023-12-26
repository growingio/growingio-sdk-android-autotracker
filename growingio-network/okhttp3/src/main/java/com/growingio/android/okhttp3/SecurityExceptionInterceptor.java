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
package com.growingio.android.okhttp3;

import com.growingio.android.sdk.track.log.Logger;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * 1.用于OkHttp防止部分机型关闭网络权限导致崩溃（特定Rom对permission的管理问题或Root后关闭权限),转为IO异常回调失败
 * 2.加强保护，将所有异常转为io异常回调失败，避免部分okhttp内部错误导致异常
 * 3.捕获错误
 */
public class SecurityExceptionInterceptor implements Interceptor {
    private static final String TAG = "SecurityExceptionInterceptor";

    @Override
    public Response intercept(Chain chain) throws IOException {
        try {
            return chain.proceed(chain.request());
        } catch (Throwable e) {
            Logger.e(TAG, "Failed due to an Exception: " + e.getClass().getName() + " with message " + e.getMessage());
            throw new IOException(e.getMessage());
        }
    }
}
