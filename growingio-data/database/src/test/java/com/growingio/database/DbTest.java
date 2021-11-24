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

package com.growingio.database;

import android.app.Application;
import android.content.pm.ProviderInfo;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.google.protobuf.InvalidProtocolBufferException;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.events.PageEvent;
import com.growingio.android.sdk.track.middleware.EventDatabase;
import com.growingio.android.sdk.track.middleware.EventDbResult;
import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.TrackerRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ContentProviderController;
import org.robolectric.annotation.Config;


import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class DbTest {

    private final ContentProviderController<EventDataContentProvider> controller =
            Robolectric.buildContentProvider(EventDataContentProvider.class);
    private final Application application = ApplicationProvider.getApplicationContext();
    private ProviderInfo providerInfo;
    private EventDataManager sqLite;

    @Before
    public void setup() {
        TrackerContext.init(application);
        providerInfo = new ProviderInfo();
        sqLite = new EventDataManager(application);
        providerInfo.authority = application.getPackageName() + "." + EventDataContentProvider.class.getSimpleName();
    }

    @Test
    public void shouldSetBaseContext() throws Exception {
        EventDataContentProvider eventsContentProvider = controller.create().get();
        assertThat(eventsContentProvider.getContext())
                .isEqualTo(((Application) ApplicationProvider.getApplicationContext()).getBaseContext());
    }

    @Test
    public void shouldInitializeFromInitializeProviderInfo() throws Exception {
        EventDataContentProvider eventsContentProvider = controller.create().get();
        assertThat(eventsContentProvider.getReadPermission()).isNull();
        assertThat(eventsContentProvider.getWritePermission()).isNull();
        assertThat(eventsContentProvider.getPathPermissions()).isNull();
    }

    @Test
    public void dataModuleTest() throws InvalidProtocolBufferException {
        DatabaseLibraryModule module = new DatabaseLibraryModule();
        TrackerRegistry trackerRegistry = new TrackerRegistry();
        module.registerComponents(application, trackerRegistry);

        CustomEvent customEvent = new CustomEvent.Builder()
                .setEventName("databaseTest")
                .build();

        ModelLoader<EventDatabase, EventDbResult> modelLoader = trackerRegistry.getModelLoader(EventDatabase.class, EventDbResult.class);
        ModelLoader.LoadData<EventDbResult> loadData = modelLoader.buildLoadData(EventDatabase.insert(customEvent));
        DataFetcher<EventDbResult> dataFetcher = loadData.fetcher;
        Truth.assertThat(dataFetcher.getDataClass()).isAssignableTo(EventDbResult.class);
        dataFetcher.loadData(new DataFetcher.DataCallback<EventDbResult>() {
            @Override
            public void onDataReady(EventDbResult data) {
                Truth.assertThat(data.getSum()).isEqualTo(1);
                Truth.assertThat(data.isSuccess()).isEqualTo(true);
            }

            @Override
            public void onLoadFailed(Exception e) {
                Truth.assertThat(true).isFalse();
            }
        });
        dataFetcher.cleanup();
        dataFetcher.cancel();

        EventDbResult dbResult = modelLoader.buildLoadData(EventDatabase.query(customEvent.getSendPolicy(), 10)).fetcher.executeData();
        Truth.assertThat(dbResult.getSum()).isEqualTo(1);
        Truth.assertThat(dbResult.isSuccess()).isEqualTo(true);
        EventV3Protocol.EventV3List eventV3List = EventV3Protocol.EventV3List.parseFrom(dbResult.getData());
        Truth.assertThat(eventV3List.getValues(0).toByteArray()).isEqualTo(EventProtocolTransfer.protocol(customEvent));
        Truth.assertThat(dbResult.getEventType()).isEqualTo(customEvent.getEventType());

        dbResult = modelLoader.buildLoadData(EventDatabase.delete(dbResult.getLastId(), customEvent.getSendPolicy(), dbResult.getEventType())).fetcher.executeData();
        Truth.assertThat(dbResult.getSum()).isEqualTo(1);
        Truth.assertThat(dbResult.isSuccess()).isEqualTo(true);

        PageEvent pageEvent = new PageEvent.Builder()
                .setTitle("databaseTest")
                .setPath("com.growingio.database.test")
                .setReferralPage("test")
                .setOrientation("vertical")
                .setTimestamp(System.currentTimeMillis())
                .build();

        modelLoader.buildLoadData(EventDatabase.insert(pageEvent)).fetcher.executeData();
        dbResult = modelLoader.buildLoadData(EventDatabase.queryAndDelete(pageEvent.getSendPolicy(), 10)).fetcher.executeData();
        Truth.assertThat(dbResult.getSum()).isEqualTo(1);
        Truth.assertThat(dbResult.isSuccess()).isEqualTo(true);

        modelLoader.buildLoadData(EventDatabase.outDated()).fetcher.executeData();
        modelLoader.buildLoadData(EventDatabase.clear()).fetcher.executeData();

        dbResult = modelLoader.buildLoadData(EventDatabase.query(pageEvent.getSendPolicy(), 10)).fetcher.executeData();
        Truth.assertThat(dbResult.getSum()).isEqualTo(0);
        Truth.assertThat(dbResult.isSuccess()).isEqualTo(true);


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
        EventDbResult dbResult = new EventDbResult();
        sqLite.queryEventsAndDelete(customEvent.getSendPolicy(), 10, dbResult);
        try {
            EventV3Protocol.EventV3List list = EventV3Protocol.EventV3List.parseFrom(dbResult.getData());
            assertThat(dbResult.getSum()).isEqualTo(4);
            assertThat(list.getValuesCount()).isEqualTo(4);
            assertThat(list.getValues(0).getEventName()).isEqualTo("contentProvider");
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        sqLite.removeAllEvents();
        sqLite.queryEventsAndDelete(customEvent.getSendPolicy(), 10, dbResult);
        assertThat(dbResult.getSum()).isEqualTo(0);

        dbResult = new EventDbResult();
        for (int i = 0; i < 5; i++) {
            CustomEvent ce = new CustomEvent.Builder()
                    .setEventName("contentProvider" + i)
                    .build();
            sqLite.insertEvent(ce);
        }
        sqLite.queryEvents(customEvent.getSendPolicy(), 1, dbResult);
        sqLite.removeEvents(dbResult.getLastId() + 4, customEvent.getSendPolicy(), customEvent.getEventType());

        sqLite.queryEvents(customEvent.getSendPolicy(), 10, dbResult);
        assertThat(dbResult.getSum()).isEqualTo(0);


    }

    @Test
    public void dbHelperTest() throws IOException {
        EventDataSQLiteOpenHelper dbHelper = new EventDataSQLiteOpenHelper(application, "growing3.db");
        dbHelper.onUpgrade(dbHelper.getWritableDatabase(), 1, 2);
        dbHelper.close();

    }

    @Test
    public void migrateTest() {
        EventDataManager dataManager = new EventDataManager(application);
        DeprecatedEventSQLite sqLite = new DeprecatedEventSQLite(application, dataManager);
        CustomEvent customEvent = new CustomEvent.Builder()
                .setEventName("databaseTest")
                .build();
        for (int i = 0; i < 150; i++) {
            sqLite.insert(customEvent);
        }

        DeprecatedEventSQLite sqLite2 = new DeprecatedEventSQLite(application, dataManager);
        long runTime = System.currentTimeMillis();
        sqLite2.migrateEvents();
        System.out.println(System.currentTimeMillis() - runTime);
        EventDbResult eventDbResult = new EventDbResult();
        dataManager.queryEvents(customEvent.getSendPolicy(), 200, eventDbResult);
        assertThat(eventDbResult.getSum()).isEqualTo(150);
        assertThat(eventDbResult.isSuccess()).isEqualTo(true);
    }


}
