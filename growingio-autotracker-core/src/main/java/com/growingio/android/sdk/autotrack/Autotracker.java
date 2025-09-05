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
package com.growingio.android.sdk.autotrack;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.growingio.android.sdk.autotrack.inject.InjectorProvider;
import com.growingio.android.sdk.autotrack.impression.ImpressionProvider;
import com.growingio.android.sdk.autotrack.page.PageProvider;
import com.growingio.android.sdk.autotrack.page.SuperFragment;
import com.growingio.android.sdk.autotrack.view.ViewAttributeUtil;
import com.growingio.android.sdk.Tracker;
import com.growingio.android.sdk.autotrack.view.ViewNodeProvider;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProvider;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("DeprecatedIsStillUsed")
public class Autotracker extends Tracker {
    private static final String TAG = "Autotracker";

    public Autotracker(Context context) {
        super(context);
    }

    @Override
    protected Map<Class<? extends TrackerLifecycleProvider>, TrackerLifecycleProvider> extraProviders() {

        Map<Class<? extends TrackerLifecycleProvider>, TrackerLifecycleProvider> providerMap = super.extraProviders();
        InjectorProvider.injectProviders(providerMap);
        providerMap.put(PageProvider.class, PageProvider.get());
        providerMap.put(ViewNodeProvider.class, new ViewNodeProvider());
        providerMap.put(ImpressionProvider.class, new ImpressionProvider());

        return providerMap;
    }

    /**
     * Generate an event for the current page
     */
    private void autotrackPage(Activity activity) {
        Logger.w(TAG, "please set a specific alias for the page:" + activity.getClass().getSimpleName());
        autotrackPage(activity, activity.getClass().getSimpleName());
    }

    /**
     * Generate a page event for the current activity
     *
     * @param activity page
     * @param alias    page's name
     */
    public void autotrackPage(Activity activity, String alias) {
        autotrackPage(activity, alias, null);
    }

    /**
     * Generate a page event for the current activity
     *
     * @param activity   page
     * @param alias      page's name
     * @param attributes page's attributes. Will be carried to the autotracker event.
     */
    public void autotrackPage(Activity activity, String alias,
                              final Map<String, String> attributes) {
        if (!isInited) return;
        if (activity == null || TextUtils.isEmpty(alias)) {
            Logger.e(TAG, "activity or alias is NULL");
            return;
        }
        Logger.d(TAG, "make activity page: " + activity.getClass() + " active.");
        TrackMainThread.trackMain().runOnUiThread(() -> PageProvider.get().autotrackActivity(activity, alias, attributes));
    }

    private void autotrackPage(androidx.fragment.app.Fragment fragment) {
        autotrackPage(fragment, fragment.getClass().getSimpleName());
    }

    /**
     * Generate a page event for the current Fragment
     *
     * @param fragment page
     * @param alias    page's name
     */
    public void autotrackPage(androidx.fragment.app.Fragment fragment, String alias) {
        autotrackPage(fragment, alias, null);
    }

    /**
     * Generate a page event for the current Fragment
     *
     * @param fragment   page
     * @param alias      page's name
     * @param attributes page's attributes. Will be carried to the autotracker event.
     */
    public void autotrackPage(androidx.fragment.app.Fragment fragment, String alias,
                              final Map<String, String> attributes) {
        if (!isInited) return;
        if (fragment == null || TextUtils.isEmpty(alias)) {
            Logger.e(TAG, "fragment or alias is NULL");
            return;
        }
        Logger.d(TAG, "make fragment page: " + fragment.getClass() + " active.");
        TrackMainThread.trackMain().runOnUiThread(() -> PageProvider.get().autotrackFragment(SuperFragment.makeX(fragment), alias, attributes));
    }

    private void autotrackSystemPage(android.app.Fragment fragment) {
        autotrackSystemPage(fragment, fragment.getClass().getSimpleName());
    }

    /**
     * Generate a page event for the current Fragment
     *
     * @param fragment page
     * @param alias    page's name
     */
    public void autotrackSystemPage(android.app.Fragment fragment, String alias) {
        autotrackSystemPage(fragment, alias, null);
    }

    /**
     * Generate a page event for the current Fragment
     *
     * @param fragment   page
     * @param alias      page's name
     * @param attributes page's attributes. Will be carried to the autotracker event.
     */
    public void autotrackSystemPage(android.app.Fragment fragment, String alias,
                                    final Map<String, String> attributes) {
        if (!isInited) return;
        if (fragment == null || TextUtils.isEmpty(alias)) {
            Logger.e(TAG, "fragment or alias is NULL");
            return;
        }
        Logger.d(TAG, "make fragment page: " + fragment.getClass() + " active.");
        TrackMainThread.trackMain().runOnUiThread(() -> PageProvider.get().autotrackFragment(SuperFragment.make(fragment), alias, attributes));
    }

    private void autotrackSupportPage(android.support.v4.app.Fragment fragment) {
        autotrackSupportPage(fragment, fragment.getClass().getSimpleName());
    }

    /**
     * Generate a page event for the current Fragment
     *
     * @param fragment page
     * @param alias    page's name
     */
    public void autotrackSupportPage(android.support.v4.app.Fragment fragment, String alias) {
        autotrackSupportPage(fragment, alias, null);
    }

    /**
     * Generate a page event for the current Fragment
     *
     * @param fragment   page
     * @param alias      page's name
     * @param attributes page's attributes. Will be carried to the autotracker event.
     */
    public void autotrackSupportPage(android.support.v4.app.Fragment fragment, String alias,
                                     final Map<String, String> attributes) {
        if (!isInited) return;
        if (fragment == null || TextUtils.isEmpty(alias)) {
            Logger.e(TAG, "fragment or alias is NULL");
            return;
        }
        Logger.d(TAG, "make fragment page: " + fragment.getClass() + " active.");
        TrackMainThread.trackMain().runOnUiThread(() -> PageProvider.get().autotrackFragment(SuperFragment.makeSupport(fragment), alias, attributes));
    }

    /**
     * Generate a unique identifier for the current View.
     * eg: setUniqueTag(button,"note"), when the button is clicked,the resulting path is "/note".
     */
    public void setUniqueTag(final View view, final String tag) {
        if (!isInited) return;
        if (view == null || TextUtils.isEmpty(tag)) {
            Logger.e(TAG, "setUniqueTag: view or tag is NULL");
            return;
        }
        Logger.d(TAG, "setUniqueTag: " + tag + " for " + view.getClass().getSimpleName());
        TrackMainThread.trackMain().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ViewAttributeUtil.setCustomId(view, tag);
            }
        });
    }

    /**
     * will be carried to the autotracker event's attribute.
     * note: please use autotrackPage generate page first. @see {@link #autotrackPage(Activity, String, Map)}
     */
    public void setPageAttributes(final Activity activity,
                                  final Map<String, String> attributes) {
        if (!isInited) return;
        if (activity == null) {
            Logger.e(TAG, "page or attributes is NULL");
            return;
        }
        TrackMainThread.trackMain().runOnUiThread(() -> {
            PageProvider.get().setPageAttributes(activity, attributes);
        });
    }

    /**
     * will be carried to the autotracker event's attribute.
     * note: please use autotrackSystemPage generate page first. @see {@link #autotrackSystemPage(android.app.Fragment, String, Map)}
     */
    public void setPageAttributesSystem(final android.app.Fragment fragment,
                                        final Map<String, String> attributes) {
        if (!isInited) return;
        if (fragment == null) {
            Logger.e(TAG, "page or attributes is NULL");
            return;
        }
        TrackMainThread.trackMain().runOnUiThread(() -> PageProvider.get().setPageAttributes(SuperFragment.make(fragment), attributes == null ? new HashMap<>() : new HashMap<>(attributes)));
    }

    /**
     * will be carried to the autotracker event's attribute.
     * note: please use autotrackSupportPage generate page first. @see {@link #autotrackSupportPage(android.support.v4.app.Fragment, String, Map)}
     */
    public void setPageAttributesSupport(final android.support.v4.app.Fragment page,
                                         final Map<String, String> attributes) {
        if (!isInited) return;
        if (page == null) {
            Logger.e(TAG, "page or attributes is NULL");
            return;
        }
        TrackMainThread.trackMain().runOnUiThread(() -> PageProvider.get().setPageAttributes(SuperFragment.makeSupport(page), attributes == null ? new HashMap<>() : new HashMap<>(attributes)));
    }

    /**
     * will be carried to the autotracker event's attribute.
     * note: please use autotrackPage generate page first. @see {@link #autotrackPage(androidx.fragment.app.Fragment, String, Map)}
     */
    public void setPageAttributes(final androidx.fragment.app.Fragment page,
                                  final Map<String, String> attributes) {
        if (!isInited) return;
        if (page == null) {
            Logger.e(TAG, "page or attributes is NULL");
            return;
        }
        TrackMainThread.trackMain().runOnUiThread(() -> PageProvider.get().setPageAttributes(SuperFragment.makeX(page), attributes == null ? new HashMap<>() : new HashMap<>(attributes)));
    }

    @Deprecated
    public void setPageAttributesX(final androidx.fragment.app.Fragment page,
                                   final Map<String, String> attributes) {
        setPageAttributes(page, attributes);
    }

    public void setPageTitle(final Activity activity, final String title) {
        if (!isInited) return;
        if (activity == null) {
            Logger.e(TAG, "activity is NULL");
            return;
        }
        TrackMainThread.trackMain().runOnUiThread(() -> PageProvider.get().setPageTitle(activity, title));
    }

    public void setPageTitle(final androidx.fragment.app.Fragment page, final String title) {
        if (!isInited) return;
        if (page == null) {
            Logger.e(TAG, "page is NULL");
            return;
        }
        TrackMainThread.trackMain().runOnUiThread(() -> PageProvider.get().setPageTitle(SuperFragment.makeX(page), title));
    }

    public void setPageTitleSystem(final android.app.Fragment page, final String title) {
        if (!isInited) return;
        if (page == null) {
            Logger.e(TAG, "page is NULL");
            return;
        }
        TrackMainThread.trackMain().runOnUiThread(() -> PageProvider.get().setPageTitle(SuperFragment.make(page), title));
    }

    public void setPageTitleSupport(final android.support.v4.app.Fragment page, final String title) {
        if (!isInited) return;
        if (page == null) {
            Logger.e(TAG, "page NULL");
            return;
        }
        TrackMainThread.trackMain().runOnUiThread(() -> PageProvider.get().setPageTitle(SuperFragment.makeSupport(page), title));
    }

    public void trackViewImpression(View view, String impressionEventName) {
        if (!isInited) return;
        trackViewImpression(view, impressionEventName, null);
    }

    public void trackViewImpression(final View view, final String impressionEventName,
                                    final Map<String, String> attributes) {
        if (!isInited) return;
        if (view == null || TextUtils.isEmpty(impressionEventName)) {
            Logger.e(TAG, "view or impressionEventName is NULL");
            return;
        }
        final HashMap<String, String> attributesCopy;
        if (attributes == null) {
            attributesCopy = null;
        } else {
            attributesCopy = new HashMap<>(attributes);
        }
        TrackMainThread.trackMain().runOnUiThread(() -> {
            ImpressionProvider impressionProvider = getContext().getProvider(ImpressionProvider.class);
            impressionProvider.trackViewImpression(view, impressionEventName, attributesCopy);
        });
    }

    public void stopTrackViewImpression(final View trackedView) {
        if (!isInited) return;
        if (trackedView == null) {
            Logger.e(TAG, "trackedView is NULL");
            return;
        }

        TrackMainThread.trackMain().runOnUiThread(() -> {
            ImpressionProvider impressionProvider = getContext().getProvider(ImpressionProvider.class);
            impressionProvider.stopTrackViewImpression(trackedView);
        });
    }

    /**
     * please use autotrackPage replace. @see {@link #autotrackPage(Activity, String)}
     */
    @Deprecated
    public void setPageAlias(final Activity page, final String alias) {
        autotrackPage(page, alias);
    }

    /**
     * please use autotrackSystemPage replace. @see {@link #autotrackSystemPage(android.app.Fragment, String)}
     */
    @Deprecated
    public void setPageAlias(final android.app.Fragment page, final String alias) {
        autotrackSystemPage(page, alias);
    }

    /**
     * please use autotrackSupportPage replace. @see {@link #autotrackSupportPage(android.support.v4.app.Fragment, String)}
     */
    @Deprecated
    public void setPageAliasSupport(final android.support.v4.app.Fragment page,
                                    final String alias) {
        autotrackSupportPage(page, alias);
    }

    /**
     * please use autotrackPage replace. @see {{@link #autotrackPage(androidx.fragment.app.Fragment, String)}}
     */
    @Deprecated
    public void setPageAliasX(final androidx.fragment.app.Fragment page, final String alias) {
        autotrackPage(page, alias);
    }

    public void ignoreView(final View view, final IgnorePolicy policy) {
        if (!isInited) return;
        if (view == null || policy == null) {
            Logger.e(TAG, "view or policy is NULL");
            return;
        }
        TrackMainThread.trackMain().runOnUiThread(() -> ViewAttributeUtil.setIgnorePolicy(view, policy));
    }

    public void ignoreViewClick(final View view, boolean isIgnore) {
        if (!isInited) return;
        if (view == null) {
            Logger.e(TAG, "ignoreViewClick failed: view is NULL");
            return;
        }
        TrackMainThread.trackMain().runOnUiThread(() -> ViewAttributeUtil.setIgnoreViewClick(view, isIgnore));
    }

    /**
     * collect the content on EditText
     */
    public void trackEditText(EditText editText, boolean track) {
        ViewAttributeUtil.setTrackText(editText, track);
    }
}
