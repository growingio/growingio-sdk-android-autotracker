/*
 *   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.growingio.android.analytics.firebase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Size;
import android.util.SizeF;
import android.util.SparseArray;

import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.TrackEventGenerator;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.ConfigurationProvider;
import com.growingio.android.sdk.track.providers.SessionProvider;
import com.growingio.android.sdk.track.providers.UserInfoProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *
 * @author cpacm 2022/5/19
 */
class FirebaseAnalyticsAdapter {

    private static final String TAG = "GrowingAdapter";
    private static final String DUEL_VECTOR_LINK = "_";
    private static volatile FirebaseAnalyticsAdapter sGaAdapter;
    private FirebaseAnalytics firebaseAnalytics;
    private String appInstanceId;
    private Bundle defaultBundle;

    public static FirebaseAnalyticsAdapter get() {
        if (sGaAdapter == null) {
            return new FirebaseAnalyticsAdapter();
        }
        synchronized (FirebaseAnalyticsAdapter.class) {
            if (sGaAdapter != null) {
                return sGaAdapter;
            }
            return new FirebaseAnalyticsAdapter();
        }
    }

    public static void init(Context context) {
        if (sGaAdapter == null) {
            sGaAdapter = new FirebaseAnalyticsAdapter(context);
        }
    }

    private FirebaseAnalyticsAdapter() {
        initFAdapter(TrackerContext.get());
    }

    private FirebaseAnalyticsAdapter(Context context) {
        initFAdapter(context);
    }

    @SuppressLint("MissingPermission")
    private void initFAdapter(Context context) {
        if (firebaseAnalytics == null) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        }
        firebaseAnalytics.getAppInstanceId().addOnCompleteListener(task -> {
            appInstanceId = task.getResult();
            Map<String, String> attr = new HashMap<>();
            attr.put("app_instance_id", appInstanceId);
            TrackEventGenerator.generateLoginUserAttributesEvent(new HashMap<>(attr));
        });

        try {
            setAnalyticsCollectionEnabled(FirebaseApp.getInstance().isDataCollectionDefaultEnabled());
        } catch (IllegalStateException ignored) {
        }

//        if (readManifestScreenTrackEnabled(context)) {
//            ActivityStateProvider.get().registerActivityLifecycleListener(event -> {
//                Bundle bundle = new Bundle();
//                bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, event.getActivity().getTitle().toString());
//                bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, event.getActivity().getLocalClassName());
//                logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
//            });
//        }
    }


    void logEvent(String name, Bundle bundle) {
        if (!checkAnalyticsValid()) return;
        if (TextUtils.isEmpty(name)) {
            Logger.e(TAG, "trackCustomEvent: eventName is NULL");
            return;
        }
        TrackEventGenerator.generateCustomEvent(name, parseBundle(bundle));
    }

    void setDefaultEventParameters(Bundle bundle) {
        //参数通过google manager remote service 发送，无法hook.只能通过每次设置来更新
        defaultBundle = bundle;
    }

    void setAnalyticsCollectionEnabled(boolean enabled) {
        TrackMainThread.trackMain().postActionToTrackMain(() -> {
            if (enabled == ConfigurationProvider.core().isDataCollectionEnabled()) {
                Logger.e(TAG, "当前数据采集开关 = " + enabled + ", 请勿重复操作");
            } else {
                ConfigurationProvider.core().setDataCollectionEnabled(enabled);
                if (enabled) {
                    SessionProvider.get().generateVisit();
                }
            }
        });
    }

    void setUserId(String userId) {
        if (!checkAnalyticsValid()) return;
        TrackMainThread.trackMain().postActionToTrackMain(() -> UserInfoProvider.get().setLoginUserId(userId));
    }

    void setUserProperty(String name, String value) {
        if (!checkAnalyticsValid()) return;
        Map<String, String> attr = new HashMap<>();
        if (value == null) value = "";
        attr.put(name, value);
        TrackEventGenerator.generateLoginUserAttributesEvent(new HashMap<>(attr));
    }

    /**
     * 将 bundle 转化为扁平化Map
     */
    Map<String, String> parseBundle(Bundle bundle) {
        if (defaultBundle != null) {
            Map<String, String> defaultMap = duelVectorFoil("", defaultBundle);
            defaultMap.putAll(duelVectorFoil("", bundle));
            return defaultMap;
        }
        return duelVectorFoil("", bundle);
    }

    private Map<String, String> duelVectorFoil(String prefix, Bundle bundle) {
        Map<String, String> attr = new HashMap<>();
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            String realKey = prefix + key;
            if (value instanceof Bundle) {
                Map<String, String> childMap = duelVectorFoil(realKey + DUEL_VECTOR_LINK, (Bundle) value);
                attr.putAll(childMap);
            } else if (value.getClass().isArray()) {
                if (value instanceof boolean[]) {
                    int index = 0;
                    for (Object child : (boolean[]) value) {
                        attr.put(realKey + DUEL_VECTOR_LINK + index, child.toString());
                        index += 1;
                    }
                } else if (value instanceof byte[]) {
                    int index = 0;
                    for (Object child : (byte[]) value) {
                        attr.put(realKey + DUEL_VECTOR_LINK + index, child.toString());
                        index += 1;
                    }
                } else if (value instanceof float[]) {
                    int index = 0;
                    for (Object child : (float[]) value) {
                        attr.put(realKey + DUEL_VECTOR_LINK + index, child.toString());
                        index += 1;
                    }
                } else if (value instanceof double[]) {
                    int index = 0;
                    for (Object child : (double[]) value) {
                        attr.put(realKey + DUEL_VECTOR_LINK + index, child.toString());
                        index += 1;
                    }
                } else if (value instanceof int[]) {
                    int index = 0;
                    for (Object child : (int[]) value) {
                        attr.put(realKey + DUEL_VECTOR_LINK + index, child.toString());
                        index += 1;
                    }
                } else if (value instanceof char[]) {
                    int index = 0;
                    for (Object child : (char[]) value) {
                        attr.put(realKey + DUEL_VECTOR_LINK + index, child.toString());
                        index += 1;
                    }
                } else if (value instanceof short[]) {
                    int index = 0;
                    for (Object child : (short[]) value) {
                        attr.put(realKey + DUEL_VECTOR_LINK + index, child.toString());
                        index += 1;
                    }
                } else {
                    int index = 0;
                    for (Object child : (Object[]) value) {
                        if (child instanceof Bundle) {
                            Map<String, String> childMap = duelVectorFoil(realKey + DUEL_VECTOR_LINK + index + DUEL_VECTOR_LINK, (Bundle) child);
                            attr.putAll(childMap);
                        } else {
                            attr.put(realKey + DUEL_VECTOR_LINK + index, child.toString());
                        }
                        index += 1;
                    }
                }
            } else if (value instanceof ArrayList) {
                int index = 0;
                for (Object child : (ArrayList) value) {
                    if (child instanceof Bundle) {
                        Map<String, String> childMap = duelVectorFoil(realKey + DUEL_VECTOR_LINK + index + DUEL_VECTOR_LINK, (Bundle) child);
                        attr.putAll(childMap);
                    } else {
                        attr.put(realKey + DUEL_VECTOR_LINK + index, child.toString());
                    }
                    index += 1;
                }
            } else if (value instanceof SparseArray) {
                SparseArray sa = (SparseArray) value;
                for (int i = 0; i < sa.size(); i++) {
                    int index = sa.keyAt(i);
                    Object child = sa.get(index);
                    if (child instanceof Bundle) {
                        Map<String, String> childMap = duelVectorFoil(realKey + DUEL_VECTOR_LINK + index + DUEL_VECTOR_LINK, (Bundle) child);
                        attr.putAll(childMap);
                    } else {
                        attr.put(realKey + DUEL_VECTOR_LINK + index, child.toString());
                    }
                }
            } else if (value instanceof Parcelable) {
                attr.put(realKey, value.toString());
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (value instanceof Size) {
                    attr.put(realKey, value.toString());
                } else if (value instanceof SizeF) {
                    attr.put(realKey, value.toString());
                } else {
                    attr.put(realKey, value.toString());
                }
            } else {
                attr.put(realKey, value.toString());
            }
        }
        return attr;
    }

    boolean checkAnalyticsValid() {
        if (firebaseAnalytics == null || TextUtils.isEmpty(appInstanceId)) return false;
        try {
            if (!FirebaseApp.getInstance().isDataCollectionDefaultEnabled()) return false;
        } catch (IllegalStateException e) {
            return false;
        }
        return TrackerContext.initializedSuccessfully();
    }

//    private boolean readManifestScreenTrackEnabled(Context context) {
//        try {
//            PackageManager packageManager = context.getPackageManager();
//            if (packageManager != null) {
//                ApplicationInfo applicationInfo =
//                        packageManager.getApplicationInfo(
//                                context.getPackageName(), PackageManager.GET_META_DATA);
//                if (applicationInfo != null
//                        && applicationInfo.metaData != null
//                        && applicationInfo.metaData.containsKey(DATA_SCREEN_TRACK_ENABLED)) {
//                    return applicationInfo.metaData.getBoolean(DATA_SCREEN_TRACK_ENABLED);
//                }
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//            // This shouldn't happen since it's this app's package, but fall through to default if so.
//        }
//        return true;
//    }
//
//    public static final String DATA_SCREEN_TRACK_ENABLED = "google_analytics_automatic_screen_reporting_enabled";

}
