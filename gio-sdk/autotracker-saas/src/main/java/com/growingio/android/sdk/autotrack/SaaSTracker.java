package com.growingio.android.sdk.autotrack;

import android.content.Context;

import com.growingio.android.sdk.track.providers.PersistentDataProvider;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProvider;

import java.util.Map;

public class SaaSTracker extends Autotracker {
    private static final String TAG = "SaaSTracker";

    public SaaSTracker(Context context) {
        super(context);
    }

    @Override
    protected Map<Class<? extends TrackerLifecycleProvider>, TrackerLifecycleProvider> extraProviders() {
        Map<Class<? extends TrackerLifecycleProvider>, TrackerLifecycleProvider> providerMap = super.extraProviders();
        providerMap.put(UpgradeProvider.class, new UpgradeProvider());
        return providerMap;
    }

    public String getLoginUserId() {
        if (!isInited) return "";
        PersistentDataProvider persistentDataProvider = getContext().getProvider(PersistentDataProvider.class);
        return persistentDataProvider.getLoginUserId();
    }
}
