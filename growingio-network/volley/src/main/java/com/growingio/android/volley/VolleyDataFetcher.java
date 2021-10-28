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

package com.growingio.android.volley;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.RequestFuture;
import com.growingio.android.sdk.track.http.EventResponse;
import com.growingio.android.sdk.track.http.EventUrl;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.modelloader.DataFetcher;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * <p>
 * custom volley request and request.
 *
 * @author cpacm 2021/3/31
 */
public class VolleyDataFetcher implements DataFetcher<EventResponse> {
    private static final String TAG = "VolleyDataFetcher";
    public static final VolleyRequestFactory DEFAULT_REQUEST_FACTORY = GioRequest::new;

    private final RequestQueue requestQueue;
    private final VolleyRequestFactory requestFactory;
    private final EventUrl url;
    private volatile com.android.volley.Request<byte[]> request;

    public VolleyDataFetcher(RequestQueue requestQueue, EventUrl eventUrl) {
        this(requestQueue, eventUrl, DEFAULT_REQUEST_FACTORY);
    }

    public VolleyDataFetcher(
            RequestQueue requestQueue, EventUrl url, VolleyRequestFactory requestFactory) {
        this.requestQueue = requestQueue;
        this.url = url;
        this.requestFactory = requestFactory;
    }


    @Override
    public void loadData(DataCallback<? super EventResponse> callback) {
        request = requestFactory.create(
                url.toUrl(),
                url.getHeaders(),
                url.getRequestBody(),
                callback::onDataReady,
                callback::onLoadFailed
        );
        requestQueue.add(request);
    }

    @Override
    public EventResponse executeData() {
        RequestFuture<EventResponse> future = RequestFuture.newFuture();
        request = requestFactory.create(url.toUrl(), url.getHeaders(), url.getRequestBody(), future, future);
        requestQueue.add(request);
        try {
            return future.get(5L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Logger.e(TAG, e, "executeData interrupted");
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            Logger.e(TAG, e);
        } catch (TimeoutException e) {
            Logger.e(TAG, e);
        }
        return new EventResponse(false);
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void cancel() {
        Request<byte[]> local = request;
        if (local != null) {
            local.cancel();
        }
    }

    @Override
    public Class<EventResponse> getDataClass() {
        return EventResponse.class;
    }

    /**
     * Default {@link com.android.volley.Request} implementation for GIO that receives errors and
     * results on volley's background thread.
     */
    // Public API.
    @SuppressWarnings("unused")
    public static class GioRequest extends Request<byte[]> {
        private final Response.Listener<EventResponse> callback;
        private final Response.ErrorListener listener;
        private final Map<String, String> headers;
        private final byte[] requestData;

        public GioRequest(String url, Response.Listener<EventResponse> callback, Response.ErrorListener errorListener) {
            this(url, Collections.emptyMap(), null, callback, errorListener);
        }

        public GioRequest(
                String url,
                Map<String, String> headers,
                byte[] data,
                Response.Listener<EventResponse> callback,
                Response.ErrorListener errorListener) {
            super(Method.GET, url, null);
            this.callback = callback;
            this.requestData = data;
            this.listener = errorListener;
            this.headers = headers;
        }

        @Override
        public Map<String, String> getHeaders() {
            return headers;
        }

        @Override
        protected VolleyError parseNetworkError(VolleyError volleyError) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Volley failed to retrieve response", volleyError);
            }
            if (!isCanceled()) {
                listener.onErrorResponse(volleyError);
            }
            return super.parseNetworkError(volleyError);
        }

        @Override
        public byte[] getBody() throws AuthFailureError {
            return requestData;
        }

        @Override
        protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
            //Giokit inject point
            //GioHttp.parseGioKitVolley
            if (!isCanceled()) {
                EventResponse eventResponse = new EventResponse(true, new ByteArrayInputStream(response.data), response.data.length);
                callback.onResponse(eventResponse);
            }
            return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
        }

        @Override
        protected void deliverResponse(byte[] response) {
            // Do nothing.
        }
    }
}
