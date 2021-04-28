package com.growingio.android.sdk.module;

import android.content.Context;

import com.growingio.android.sdk.track.modelloader.TrackerRegistry;

/**
 * Registers a set of components to use when initializing GrowingIO whthin an app when GrowingIO's
 * annotation processor is used.
 *
 * @author cpacm 4/23/21
 */
public abstract class LibraryGioModule {

    public void registerComponents(Context context, TrackerRegistry registry) {
        //Default empty impl;
    }
}
