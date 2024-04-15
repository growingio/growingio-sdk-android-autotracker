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
package com.growingio.android.urlconnection;

import static com.growingio.android.urlconnection.UrlConnectionConfig.DEFAULT_URL_CONNECTION_TIMEOUT;

import android.text.TextUtils;

import com.growingio.android.sdk.track.middleware.http.EventResponse;
import com.growingio.android.sdk.track.middleware.http.EventUrl;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.http.HttpDataFetcher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

/**
 * <p>
 *
 * @author cpacm 2021/5/12
 */
public class UrlConnectionFetcher implements HttpDataFetcher<EventResponse> {
    private static final String TAG = "UrlConnectionFetcher";

    private static final int MAXIMUM_REDIRECTS = 2;
    private static final String REDIRECT_HEADER_FIELD = "Location";
    private static final int INVALID_STATUS_CODE = -1;
    private static final HttpUrlConnectionFactory DEFAULT_CONNECTION_FACTORY =
            new DefaultHttpUrlConnectionFactory();

    private final EventUrl eventUrl;

    private HttpURLConnection urlConnection;
    private InputStream stream;
    private volatile boolean isCancelled;

    private final int connectTimeout;
    private final int readTimeout;

    public UrlConnectionFetcher(UrlConnectionConfig config, EventUrl eventUrl) {
        this.eventUrl = eventUrl;
        if (config != null) {
            connectTimeout = config.getConnectTimeout();
            readTimeout = config.getReadTimeout();
        } else {
            connectTimeout = DEFAULT_URL_CONNECTION_TIMEOUT;
            readTimeout = DEFAULT_URL_CONNECTION_TIMEOUT;
        }
    }

    @Override
    public void loadData(DataCallback<? super EventResponse> callback) {
        long startTime = LogTime.getLogTime();
        try {
            Map<String, String> headers = eventUrl.getHeaders();
            headers.put("content-type", eventUrl.getMediaType());
            EventResponse result = loadDataWithRedirects(new URL(eventUrl.toUrl()), 0, null, headers, eventUrl.getRequestBody());
            callback.onDataReady(result);
        } catch (HttpException httpException) {
            if (httpException.getStatusCode() == INVALID_STATUS_CODE) {
                callback.onLoadFailed(httpException);
            } else {
                callback.onDataReady(new EventResponse(httpException.getStatusCode()));
            }
        } catch (IOException e) {
            Logger.e(TAG, "Failed to load data for url", e);
            callback.onLoadFailed(e);
        } finally {
            cleanup();
            Logger.v(TAG, "Finished http url fetcher fetch in " + LogTime.getElapsedMillis(startTime));
        }
    }

    private EventResponse loadDataWithRedirects(URL url, int redirects, URL lastUrl, Map<String, String> headers, byte[] data) throws HttpException {
        if (redirects >= MAXIMUM_REDIRECTS) {
            throw new HttpException(
                    "Too many (> " + MAXIMUM_REDIRECTS + ") redirects!", INVALID_STATUS_CODE);
        } else {
            // Comparing the URLs using .equals performs additional network I/O and is generally broken.
            // See http://michaelscharf.blogspot.com/2006/11/javaneturlequals-and-hashcode-make.html.
            try {
                if (lastUrl != null && url.toURI().equals(lastUrl.toURI())) {
                    throw new HttpException("In re-direct loop", INVALID_STATUS_CODE);
                }
            } catch (URISyntaxException e) {
                // Do nothing, this is best effort.
            }
        }
        urlConnection = buildAndConfigureConnection(url, headers, data);

        try {
            // Connect explicitly to avoid errors in decoders if connection fails.
            urlConnection.connect();
            // Set the stream so that it's closed in cleanup to avoid resource leaks. See #2352.
            stream = urlConnection.getInputStream();
        } catch (IOException e) {
            throw new HttpException(
                    "Failed to connect or obtain data", getHttpStatusCodeOrInvalid(urlConnection), e);
        }
        if (isCancelled) {
            return null;
        }
        final int statusCode = getHttpStatusCodeOrInvalid(urlConnection);
        // giokit inject point
        // GioHttp.parseGiokitUrlConnection(urlConnection,headers,data)
        if (isHttpOk(statusCode)) {
            return getStreamForSuccessfulRequest(urlConnection);
        } else if (isHttpRedirect(statusCode)) {
            String redirectUrlString = urlConnection.getHeaderField(REDIRECT_HEADER_FIELD);
            if (TextUtils.isEmpty(redirectUrlString)) {
                throw new HttpException("Received empty or null redirect url", statusCode);
            }
            URL redirectUrl;
            try {
                redirectUrl = new URL(url, redirectUrlString);
            } catch (MalformedURLException e) {
                throw new HttpException("Bad redirect url: " + redirectUrlString, statusCode, e);
            }
            // Closing the stream specifically is required to avoid leaking ResponseBodys in addition
            // to disconnecting the url connection below. See #2352.
            cleanup();
            return loadDataWithRedirects(redirectUrl, redirects + 1, url, headers, data);
        } else if (statusCode == INVALID_STATUS_CODE) {
            throw new HttpException(statusCode);
        } else {
            throw new HttpException("Failed to get a response message", statusCode);
        }

    }

    private static int getHttpStatusCodeOrInvalid(HttpURLConnection urlConnection) {
        try {
            return urlConnection.getResponseCode();
        } catch (IOException e) {
            Logger.d(TAG, "Failed to get a response code", e);
        }
        return INVALID_STATUS_CODE;
    }

    private HttpURLConnection buildAndConfigureConnection(URL url, Map<String, String> headers, byte[] data)
            throws HttpException {
        HttpURLConnection urlConnection;
        try {
            urlConnection = DEFAULT_CONNECTION_FACTORY.build(url);
            for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
                urlConnection.addRequestProperty(headerEntry.getKey(), headerEntry.getValue());
            }
            if (data != null || eventUrl.getRequestMethod() == EventUrl.POST) {
                urlConnection.setRequestMethod("POST");
            } else {
                urlConnection.setRequestMethod("GET");
            }
            int callTimeout = readTimeout;
            if (eventUrl.getCallTimeout() > 0) {
                callTimeout = eventUrl.getCallTimeout();
            }
            urlConnection.setConnectTimeout(connectTimeout);
            urlConnection.setReadTimeout(callTimeout);
            urlConnection.setUseCaches(false);
            urlConnection.setDoInput(true);
            if (data != null) {
                urlConnection.setDoOutput(true);
                OutputStream out = urlConnection.getOutputStream();
                out.write(data);
                out.flush();
                out.close();
            }
            // Stop the urlConnection instance of HttpUrlConnection from following redirects so that
            // redirects will be handled by recursive calls to this method, loadDataWithRedirects.
            urlConnection.setInstanceFollowRedirects(false);
        } catch (IOException e) {
            throw new HttpException("URL.openConnection threw", /*statusCode=*/ 0, e);
        }
        return urlConnection;
    }

    // Referencing constants is less clear than a simple static method.
    private static boolean isHttpOk(int statusCode) {
        return statusCode / 100 == 2;
    }

    // Referencing constants is less clear than a simple static method.
    private static boolean isHttpRedirect(int statusCode) {
        return statusCode / 100 == 3;
    }

    private EventResponse getStreamForSuccessfulRequest(HttpURLConnection urlConnection)
            throws HttpException {
        try {
            int responseCode = urlConnection.getResponseCode();
            long contentLength = 0L;
            if (TextUtils.isEmpty(urlConnection.getContentEncoding())) {
                contentLength = urlConnection.getContentLength();
            } else {
                Logger.d(TAG, "Got non empty content encoding: " + urlConnection.getContentEncoding());
            }
            stream = urlConnection.getInputStream();
            return new EventResponse(responseCode, copyResponse(urlConnection.getInputStream()), contentLength);
        } catch (IOException e) {
            throw new HttpException("Failed to obtain InputStream", getHttpStatusCodeOrInvalid(urlConnection), e);
        }
    }

    private InputStream copyResponse(InputStream input) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = input.read(buffer)) > -1) {
            baos.write(buffer, 0, len);
        }
        baos.flush();
        return new ByteArrayInputStream(baos.toByteArray());
    }

    @Override
    public EventResponse executeData() {
        long startTime = LogTime.getLogTime();
        try {
            Map<String, String> headers = eventUrl.getHeaders();
            headers.put("content-type", eventUrl.getMediaType());
            return loadDataWithRedirects(new URL(eventUrl.toUrl()), 0, null, headers, eventUrl.getRequestBody());
        } catch (HttpException httpException) {
            if (httpException.getStatusCode() == INVALID_STATUS_CODE) {
                return new EventResponse(0);
            } else {
                return new EventResponse(httpException.getStatusCode());
            }
        } catch (IOException e) {
            Logger.d(TAG, "Failed to load data for url", e);
        } finally {
            cleanup();
            Logger.v(TAG, "Finished http url fetcher fetch in " + LogTime.getElapsedMillis(startTime));
        }
        return new EventResponse(0);
    }

    @Override
    public void cleanup() {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                // Ignore
            }
        }
        if (urlConnection != null) {
            urlConnection.disconnect();
        }
        urlConnection = null;
    }

    @Override
    public void cancel() {
        isCancelled = true;
    }

    @Override
    public Class<EventResponse> getDataClass() {
        return EventResponse.class;
    }

    interface HttpUrlConnectionFactory {
        HttpURLConnection build(URL url) throws IOException;
    }

    private static class DefaultHttpUrlConnectionFactory implements HttpUrlConnectionFactory {

        DefaultHttpUrlConnectionFactory() {
        }

        @Override
        public HttpURLConnection build(URL url) throws IOException {
            return (HttpURLConnection) url.openConnection();
        }
    }
}
