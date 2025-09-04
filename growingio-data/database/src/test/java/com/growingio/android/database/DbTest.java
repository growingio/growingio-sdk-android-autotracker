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
package com.growingio.android.database;

import android.app.Application;
import android.content.pm.ProviderInfo;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.json.JsonDataLoader;
import com.growingio.android.sdk.Tracker;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.events.AttributesBuilder;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.events.PageEvent;
import com.growingio.android.sdk.track.middleware.EventDatabase;
import com.growingio.android.sdk.track.middleware.EventDbResult;
import com.growingio.android.sdk.track.middleware.format.EventByteArray;
import com.growingio.android.sdk.track.middleware.format.EventFormatData;
import com.growingio.android.sdk.track.modelloader.DataFetcher;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.protobuf.ProtobufDataLoader;

import org.json.JSONArray;
import org.json.JSONException;
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

    private TrackerContext trackerContext;

    @Before
    public void setup() {
        Tracker tracker = new Tracker(application);
        trackerContext = tracker.getContext();
        providerInfo = new ProviderInfo();
        sqLite = new EventDataManager(trackerContext);
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
    public void dataModuleTest() {
        trackerContext.getRegistry().register(EventFormatData.class, EventByteArray.class, new ProtobufDataLoader.Factory());
        DatabaseLibraryModule module = new DatabaseLibraryModule();
        module.registerComponents(trackerContext);

        CustomEvent customEvent = new CustomEvent.Builder()
                .setEventName("databaseTest")
                .build();

        ModelLoader<EventDatabase, EventDbResult> modelLoader = trackerContext.getRegistry().getModelLoader(EventDatabase.class, EventDbResult.class);
        ModelLoader.LoadData<EventDbResult> loadData = modelLoader.buildLoadData(EventDatabase.insert(customEvent));
        DataFetcher<EventDbResult> dataFetcher = loadData.fetcher;
        Truth.assertThat(dataFetcher.getDataClass()).isAssignableTo(EventDbResult.class);
        EventDbResult data = dataFetcher.executeData();
        Truth.assertThat(data.getSum()).isEqualTo(1);
        Truth.assertThat(data.isSuccess()).isEqualTo(true);

        EventDbResult dbResult = modelLoader.buildLoadData(EventDatabase.query(customEvent.getSendPolicy(), 10)).fetcher.executeData();
        Truth.assertThat(dbResult.getSum()).isEqualTo(1);
        Truth.assertThat(dbResult.isSuccess()).isEqualTo(true);
        Truth.assertThat(dbResult.getEventType()).isEqualTo("TRACK");

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
        trackerContext.getRegistry().register(EventFormatData.class, EventByteArray.class, new JsonDataLoader.Factory());
        controller.create(providerInfo).get();
        sqLite.removeOverdueEvents(7);
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
            JSONArray array = new JSONArray(new String(dbResult.getData()));
            assertThat(dbResult.getSum()).isEqualTo(4);
            assertThat(array.length()).isEqualTo(4);
            assertThat(array.getJSONObject(0).optString("eventName")).isEqualTo("contentProvider");
        } catch (JSONException e) {
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

        String type = sqLite.getDatabaseEventType(customEvent);
        sqLite.removeEvents(dbResult.getLastId() + 4, customEvent.getSendPolicy(), type);

        sqLite.queryEvents(customEvent.getSendPolicy(), 10, dbResult);
        assertThat(dbResult.getSum()).isEqualTo(0);
    }

    @Test
    public void dbHelperTest() throws IOException {
        EventDataSQLiteOpenHelper dbHelper = new EventDataSQLiteOpenHelper(application, "growing3.db");
        dbHelper.onUpgrade(dbHelper.getWritableDatabase(), 1, 2);
        Truth.assertThat(dbHelper.getDatabaseName()).isEqualTo("growing3.db");
        Truth.assertThat(dbHelper.getWritableDatabase().getVersion()).isEqualTo(1);
        dbHelper.close();
    }

    @Test
    public void migrateTest() {
        trackerContext.getRegistry().register(EventFormatData.class, EventByteArray.class, new ProtobufDataLoader.Factory());
        EventDataManager dataManager = new EventDataManager(trackerContext);
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

    @Test
    public void insetLargeData() {
        trackerContext.getRegistry().register(EventFormatData.class, EventByteArray.class, new ProtobufDataLoader.Factory());
        controller.create(providerInfo).get();
        long dataSize = 0;
        long maxDataSize = 2 * 1000 * 1024;
        int key = 1;
        AttributesBuilder attrBuilder = new AttributesBuilder();
        String data = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam in scelerisque sem. Mauris volutpat, dolor id interdum ullamcorper, risus dolor egestas lectus, sit amet mattis purus dui nec risus. Maecenas non sodales nisi, vel dictum dolor. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Suspendisse blandit eleifend diam, vel rutrum tellus vulputate quis. Aliquam eget libero aliquet, imperdiet nisl a, ornare ex. Sed rhoncus est ut libero porta lobortis. Fusce in dictum tellus.\n" +
                "    \\n\\n\\t\\tSuspendisse interdum ornare ante. Aliquam nec cursus lorem. Morbi id magna felis. Vivamus egestas, est a condimentum egestas, turpis nisl iaculis ipsum, in dictum tellus dolor sed neque. Morbi tellus erat, dapibus ut sem a, iaculis tincidunt dui. Interdum et malesuada fames ac ante ipsum primis in faucibus. Curabitur et eros porttitor, ultricies urna vitae, molestie nibh. Phasellus at commodo eros, non aliquet metus. Sed maximus nisl nec dolor bibendum, vel congue leo egestas.\n" +
                "    \\n\\n\\t\\tSed interdum tortor nibh, in sagittis risus mollis quis. Curabitur mi odio, condimentum sit amet auctor at, mollis non turpis. Nullam pretium libero vestibulum, finibus orci vel, molestie quam. Fusce blandit tincidunt nulla, quis sollicitudin libero facilisis et. Integer interdum nunc ligula, et fermentum metus hendrerit id. Vestibulum lectus felis, dictum at lacinia sit amet, tristique id quam. Cras eu consequat dui. Suspendisse sodales nunc ligula, in lobortis sem porta sed. Integer id ultrices magna, in luctus elit. Sed a pellentesque est.\n" +
                "    \\n\\n\\t\\tAenean nunc velit, lacinia sed dolor sed, ultrices viverra nulla. Etiam a venenatis nibh. Morbi laoreet, tortor sed facilisis varius, nibh orci rhoncus nulla, id elementum leo dui non lorem. Nam mollis ipsum quis auctor varius. Quisque elementum eu libero sed commodo. In eros nisl, imperdiet vel imperdiet et, scelerisque a mauris. Pellentesque varius ex nunc, quis imperdiet eros placerat ac. Duis finibus orci et est auctor tincidunt. Sed non viverra ipsum. Nunc quis augue egestas, cursus lorem at, molestie sem. Morbi a consectetur ipsum, a placerat diam. Etiam vulputate dignissim convallis. Integer faucibus mauris sit amet finibus convallis.\n" +
                "    \\n\\n\\t\\tPhasellus in aliquet mi. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. In volutpat arcu ut felis sagittis, in finibus massa gravida. Pellentesque id tellus orci. Integer dictum, lorem sed efficitur ullamcorper, libero justo consectetur ipsum, in mollis nisl ex sed nisl. Donec maximus ullamcorper sodales. Praesent bibendum rhoncus tellus nec feugiat. In a ornare nulla. Donec rhoncus libero vel nunc consequat, quis tincidunt nisl eleifend. Cras bibendum enim a justo luctus vestibulum. Fusce dictum libero quis erat maximus, vitae volutpat diam dignissim.\n" +
                "  ";
        while (dataSize < maxDataSize) {
            attrBuilder.addAttribute(String.valueOf(key), data);
            key++;
            dataSize += data.getBytes().length + String.valueOf(key).getBytes().length;
        }
        CustomEvent customEvent = new CustomEvent.Builder()
                .setEventName("LargeData")
                .setAttributes(attrBuilder.build())
                .build();
        Uri result = sqLite.insertEvent(customEvent);
        assertThat(result).isNull();
    }


}
