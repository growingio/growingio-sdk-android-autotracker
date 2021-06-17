/*
 *
 *  Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
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
 *
 */

package com.growingio.android.sdk.track.middleware;

import android.app.Application;
import android.content.pm.ProviderInfo;

import androidx.test.core.app.ApplicationProvider;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.events.CustomEvent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ContentProviderController;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class DbTest {

    private final ContentProviderController<EventsContentProvider> controller =
            Robolectric.buildContentProvider(EventsContentProvider.class);
    private final Application application = ApplicationProvider.getApplicationContext();
    private ProviderInfo providerInfo;
    private EventsSQLite sqLite;

    @Before
    public void setup() {
        TrackerContext.init(application);
        providerInfo = new ProviderInfo();
        sqLite = new EventsSQLite(application);
        providerInfo.authority = application.getPackageName() + ".EventsContentProvider";
    }

    @Test
    public void shouldSetBaseContext() throws Exception {
        EventsContentProvider eventsContentProvider = controller.create().get();
        assertThat(eventsContentProvider.getContext())
                .isEqualTo(((Application) ApplicationProvider.getApplicationContext()).getBaseContext());
    }

    @Test
    public void shouldInitializeFromInitializeProviderInfo() throws Exception {
        EventsContentProvider eventsContentProvider = controller.create().get();
        assertThat(eventsContentProvider.getReadPermission()).isNull();
        assertThat(eventsContentProvider.getWritePermission()).isNull();
        assertThat(eventsContentProvider.getPathPermissions()).isNull();
    }


    @Test
    public void contentProviderTest() {
        controller.create(providerInfo).get();
        sqLite.removeOverdueEvents();
        CustomEvent customEvent = new CustomEvent.Builder()
                .setEventName("contentProvider")
                .build();
        sqLite.insertEvent(customEvent);
        sqLite.insertEvent(customEvent);
        sqLite.insertEvent(customEvent);
        sqLite.insertEvent(customEvent);
        List<GEvent> result = new ArrayList<>();
        sqLite.queryEventsAndDelete(customEvent.getSendPolicy(), 10, result);
        assertThat(result.size()).isEqualTo(4);
        CustomEvent event = (CustomEvent) result.get(0);
        assertThat(event.getEventName()).isEqualTo("contentProvider");

        result.clear();
        sqLite.removeAllEvents();
        sqLite.queryEventsAndDelete(customEvent.getSendPolicy(), 10, result);
        assertThat(result.size()).isEqualTo(0);


        for (int i = 0; i < 5; i++) {
            CustomEvent ce = new CustomEvent.Builder()
                    .setEventName("contentProvider" + i)
                    .build();
            sqLite.insertEvent(ce);
        }
        long id = sqLite.queryEvents(customEvent.getSendPolicy(), 1, result);
        sqLite.removeEvents(id + 4, customEvent.getSendPolicy(), customEvent.getEventType());

        result.clear();
        sqLite.queryEvents(customEvent.getSendPolicy(), 10, result);
        assertThat(result.size()).isEqualTo(0);


    }

    @Test
    public void dbHelperTest() {
        EventsSQLiteOpenHelper dbHelper = new EventsSQLiteOpenHelper(application, "growing3.db");
        dbHelper.onUpgrade(dbHelper.getWritableDatabase(), 1, 2);
        dbHelper.close();
    }

}
