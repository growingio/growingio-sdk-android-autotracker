package com.growingio.android.analytics.google;

import android.content.Context;

import com.growingio.android.sdk.LibraryGioModule;
import com.growingio.android.sdk.track.modelloader.TrackerRegistry;
import com.growingio.sdk.annotation.GIOLibraryModule;

@GIOLibraryModule(config = GoogleAnalyticsConfiguration.class)
public class GoogleAnalyticsLibraryModule extends LibraryGioModule {

    @Override
    public void registerComponents(Context context, TrackerRegistry registry) {
        // 提前初始化
        GoogleAnalyticsAdapter.get();
    }
}