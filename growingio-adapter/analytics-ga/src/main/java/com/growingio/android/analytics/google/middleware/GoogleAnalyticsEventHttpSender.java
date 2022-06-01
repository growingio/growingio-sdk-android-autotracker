package com.growingio.android.analytics.google.middleware;

import android.text.TextUtils;

import com.growingio.android.analytics.google.GoogleAnalyticsConfiguration;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.EventHttpSender;
import com.growingio.android.sdk.track.middleware.SendResponse;
import com.growingio.android.sdk.track.middleware.http.EventEncoder;
import com.growingio.android.sdk.track.middleware.http.EventResponse;
import com.growingio.android.sdk.track.middleware.http.EventUrl;
import com.growingio.android.sdk.track.modelloader.ModelLoader;

public class GoogleAnalyticsEventHttpSender extends EventHttpSender {
    private static final String TAG = "GoogleAnalyticsEventHttpSender";

    private final String mCollectId;
    private final String mServerHost;

    public GoogleAnalyticsEventHttpSender(GoogleAnalyticsConfiguration googleAnalyticsConfiguration) {
        mCollectId = googleAnalyticsConfiguration.getCollectId();
        mServerHost = googleAnalyticsConfiguration.getServerHost();
    }

    @Override
    public SendResponse send(byte[] events, String mediaType) {
        if (events == null || events.length == 0) {
            return new SendResponse(false, 0);
        }
        if (getNetworkModelLoader() == null) {
            Logger.e(TAG, "please register http request component first");
            return new SendResponse(false, 0);
        }
        long time = System.currentTimeMillis();
        EventUrl eventUrl = new EventUrl(mServerHost, time)
                .addPath("v3")
                .addPath("projects")
                .addPath(mCollectId)
                .addPath("collect")
                .addParam("stm", String.valueOf(time))
                .setBodyData(events);
        if (!TextUtils.isEmpty(mediaType)) eventUrl.setMediaType(mediaType);
        //data encoder - https://codes.growingio.com/w/api_v3_interface/
        EventEncoder encoder = TrackerContext.get().executeData(new EventEncoder(eventUrl), EventEncoder.class, EventEncoder.class);
        if (encoder != null) {
            eventUrl = encoder.getEventUrl();
        }

        byte[] data = eventUrl.getRequestBody();


        ModelLoader.LoadData<EventResponse> loadData = getNetworkModelLoader().buildLoadData(eventUrl);
        if (!loadData.fetcher.getDataClass().isAssignableFrom(EventResponse.class)) {
            Logger.e(TAG, new IllegalArgumentException("illegal data class for http response."));
            return new SendResponse(false, 0);
        }
        EventResponse response = loadData.fetcher.executeData();

        boolean successful = response != null && response.isSucceeded();
        if (successful) {
            Logger.d(TAG, "Send events successfully");
        } else {
            Logger.d(TAG, "Send events failed, response = " + response);
        }
        long totalUsed = data == null ? 0L : data.length;
        return new SendResponse(successful, totalUsed);
    }

    private ModelLoader<EventUrl, EventResponse> getNetworkModelLoader() {
        return TrackerContext.get().getRegistry().getModelLoader(EventUrl.class, EventResponse.class);
    }
}
