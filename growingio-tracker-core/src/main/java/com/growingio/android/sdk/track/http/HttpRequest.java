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

package com.growingio.android.sdk.track.http;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.growingio.android.sdk.track.log.Logger;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpRequest {
    private static final String TAG = "HttpRequest";

    private static final int DEFAULT_CONNECT_TIMEOUT = 5;
    private static final int DEFAULT_READ_TIMEOUT = 10;

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(new GzipRequestInterceptor())
            .addInterceptor(new RetryInterceptor())
            .addInterceptor(new SecurityExceptionInterceptor())
            .build();

    private final Handler mUiHandler;
    private final Request mRequest;
    private Call mCall;
    private volatile boolean mIsCancel = false;

    HttpRequest(Request request) {
        mRequest = request;
        mUiHandler = new Handler(Looper.getMainLooper());
    }

    public static GetRequestBuilder get(String url) {
        return new GetRequestBuilder(url);
    }

    public static DataPostRequestBuilder postData(String url) {
        return new DataPostRequestBuilder(url);
    }

    public static JsonPostRequestBuilder postJson(String url) {
        return new JsonPostRequestBuilder(url);
    }

    void cancel() {
        if (mIsCancel) {
            return;
        }
        mIsCancel = true;
        mUiHandler.removeCallbacksAndMessages(null);
        if (mCall != null) {
            mCall.cancel();
        }
    }

    private boolean isCancel() {
        return mIsCancel;
    }

    public Response execute() {
        try {
            return HTTP_CLIENT.newCall(mRequest).execute();
        } catch (IOException e) {
            Logger.e(TAG, "execute ERROR", e);
        }
        return null;
    }

    public <T> HttpRequestTask enqueue(final DataCallback<T> callback) {
        Call call;
        call = HTTP_CLIENT.newCall(mRequest);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Logger.e(TAG, e);
                if (callback != null) {
                    onFailedInUiThread(callback, 0);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        if (callback != null) {
                            onSuccessInUiThread(callback, body.string(), response.headers().toMultimap());
                        }
                    } else {
                        Logger.e(TAG, "call is ERROR, body is NULL");
                        if (callback != null) {
                            onFailedInUiThread(callback, response.code());
                        }
                    }
                } else {
                    String bodyStr = response.body() == null ? "" : response.body().string();
                    Logger.e(TAG, "call is ERROR, Code = " + response.code() + ", body = " + bodyStr);
                    if (callback != null) {
                        onFailedInUiThread(callback, response.code());
                    }
                }
            }
        });
        mCall = call;
        return new HttpRequestTask(this);
    }

    private void onFailedInUiThread(final DataCallback callback, final int errorCode) {
        if (isCancel()) {
            return;
        }
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isCancel()) {
                    return;
                }
                callback.onFailed(errorCode);
            }
        });
    }

    private <T> void onSuccessInUiThread(final DataCallback<T> callback, final String body, final Map<String, List<String>> headers) {
        if (isCancel()) {
            return;
        }

        try {
            T result = null;
            if (!TextUtils.isEmpty(body)) {
                Type[] interfacesTypes = callback.getClass().getGenericInterfaces();
                Type[] genericType2 = ((ParameterizedType) interfacesTypes[0]).getActualTypeArguments();
                Class clazz = (Class) genericType2[0];
                if (clazz == String.class) {
                    result = (T) body;
                } else {
                    Method method = clazz.getMethod("fromJson", String.class);
                    result = (T) method.invoke(null, body);
                }
            }

            final T finalResult = result;
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (isCancel()) {
                        return;
                    }
                    callback.onSuccess(finalResult, headers);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (isCancel()) {
                        return;
                    }
                    callback.onSuccess(null, headers);
                }
            });
        }
    }
}
