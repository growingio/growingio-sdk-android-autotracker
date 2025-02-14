/*
 *  Copyright (C) 2025 Beijing Yishu Technology Co., Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.growingio.android.sdk.autotrack.page;

import androidx.annotation.NonNull;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.AttributesBuilder;
import com.growingio.android.sdk.track.events.CustomEvent;

import java.util.Map;

public class PageLeave implements Thread.UncaughtExceptionHandler {

    static final String PAGE_LEAVE_EVENT_NAME = "$page_leave";
    static final String PAGE_NAME = "$page_alias";
    static final String PAGE_DURATION = "$page_duration";
    static final String PAGE_PATH = "$page_path";

    public static void buildPageLeaveEvent(Page<?> page) {
        buildPageLeaveEvent(page, false);
    }

    public static void buildPageLeaveEvent(Page<?> page, boolean isSync) {
        if (!page.isAutotrack() || page.isLeave()) return;

        long duration = System.currentTimeMillis() - page.getShowTimestamp();
        Map<String, String> attributes = new AttributesBuilder()
                .addAttribute(PAGE_NAME, page.getName())
                .addAttribute(PAGE_DURATION, duration)
                .addAttribute(PAGE_PATH, page.pagePath())
                .build();
        CustomEvent.Builder builder = new CustomEvent.Builder()
                .setEventName(PAGE_LEAVE_EVENT_NAME);
        builder.setAttributes(attributes);
        page.setIsLeave(true);
        if (isSync) {
            TrackMainThread.trackMain().sendEventSync(builder);
        } else {
            TrackMainThread.trackMain().postEventToTrackMain(builder);
        }
    }

    private final Thread.UncaughtExceptionHandler lastExceptionHandler;
    private final PageProvider pageProvider;

    protected PageLeave(TrackerContext context) {
        pageProvider = context.getProvider(PageProvider.class);
        lastExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        try {
            pageProvider.sendAllPagesLeave();
            if (lastExceptionHandler != null) {
                lastExceptionHandler.uncaughtException(t, e);
            }
        } catch (Exception ignored) {
        }

    }
}
