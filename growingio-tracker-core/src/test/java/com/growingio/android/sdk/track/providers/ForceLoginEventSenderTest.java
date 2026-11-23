/*
 * Copyright (C) 2026 Beijing Yishu Technology Co., Ltd.
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
package com.growingio.android.sdk.track.providers;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;
import com.growingio.android.database.DatabaseDataLoader;
import com.growingio.android.database.EventDataContentProvider;
import com.growingio.android.database.EventDataTable;
import com.growingio.android.json.JsonDataLoader;
import com.growingio.android.protobuf.EventV3Protocol;
import com.growingio.android.protobuf.ProtobufDataLoader;
import com.growingio.android.sdk.CoreConfiguration;
import com.growingio.android.sdk.Tracker;
import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.TrackMainThread;
import com.growingio.android.sdk.track.events.CustomEvent;
import com.growingio.android.sdk.track.middleware.EventDatabase;
import com.growingio.android.sdk.track.middleware.EventDbResult;
import com.growingio.android.sdk.track.middleware.SendResponse;
import com.growingio.android.sdk.track.middleware.format.EventByteArray;
import com.growingio.android.sdk.track.middleware.format.EventFormatData;
import com.growingio.android.sdk.track.modelloader.ModelLoader;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ContentProviderController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowConnectivityManager;
import org.robolectric.util.ReflectionHelpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ForceLoginEventSenderTest {

    private final ContentProviderController<TestEventDataContentProvider> controller =
            Robolectric.buildContentProvider(TestEventDataContentProvider.class);
    private final Application application = ApplicationProvider.getApplicationContext();

    private Tracker tracker;
    private TrackerContext context;
    private EventSenderProvider eventSenderProvider;
    private TestEventDataContentProvider contentProvider;

    @Before
    public void setup() {
        application.getSharedPreferences("growing3_force_login", Context.MODE_PRIVATE).edit().clear().commit();
        ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.authority = application.getPackageName() + "." + EventDataContentProvider.CONTENT_PROVIDER_NAME;
        contentProvider = controller.create(providerInfo).get();
        startTracker();
        eventSenderProvider.removeAllEvents();
    }

    private void startTracker() {
        CoreConfiguration configuration = new CoreConfiguration("ForceLoginEventSenderTest", "growingio://force-login");
        configuration.setForceLogin(true);
        configuration.setDataUploadInterval(0);
        configuration.setDataCollectionEnabled(false);
        TrackerLifecycleProviderFactory.create().createConfigurationProviderWithConfig(configuration, new HashMap<>());
        tracker = new Tracker(application);
        configuration.setDataCollectionEnabled(true);
        context = tracker.getContext();

        context.getRegistry().register(EventDatabase.class, EventDbResult.class, new DatabaseDataLoader.Factory(context));
        eventSenderProvider = context.getProvider(EventSenderProvider.class);
    }

    private void restartTracker() {
        tracker.shutdown();
        contentProvider.dbHelper.close();
        startTracker();
    }

    @After
    public void tearDown() {
        eventSenderProvider.removeAllEvents();
        tracker.shutdown();
        contentProvider.shutdown();
        application.getSharedPreferences("growing3_force_login", Context.MODE_PRIVATE).edit().clear().commit();
    }

    @Test
    public void identifyPersistsPendingJsonEventsWhileOffline() throws Exception {
        context.getRegistry().register(EventFormatData.class, EventByteArray.class, new JsonDataLoader.Factory());
        String generatedDeviceId = context.getDeviceInfoProvider().getDeviceId();
        // 老版本或此前启动已确认身份的事件：不带待替换标志
        CustomEvent legacy = legacyEvent("legacy-json");
        // 本次启动 identify 前的事件：自动生成的设备 ID + 待替换标志
        CustomEvent pending = customEvent("pending-json");
        Truth.assertThat(legacy.isForceLoginPending()).isFalse();
        Truth.assertThat(pending.isForceLoginPending()).isTrue();
        Truth.assertThat(pending.getDeviceId()).isEqualTo(generatedDeviceId);
        eventSenderProvider.cacheEvent(legacy);
        eventSenderProvider.cacheEvent(pending);
        List<byte[]> requests = new ArrayList<>();
        eventSenderProvider.setEventNetSender((events, mediaType) -> {
            requests.add(events);
            return new SendResponse(204, 1000L);
        });

        eventSenderProvider.sendEvents(false);
        Truth.assertThat(requests).isEmpty();
        Map<String, String> stored = jsonDeviceIds(queryEvents(pending).getData());
        Truth.assertThat(stored).containsExactly("legacy-json", generatedDeviceId, "pending-json", generatedDeviceId);
        Truth.assertThat(pendingFlags()).containsExactly(0, 1).inOrder();

        ConnectivityManager manager = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = manager.getActiveNetworkInfo();
        ShadowConnectivityManager connectivity = Shadows.shadowOf(manager);
        connectivity.setActiveNetworkInfo(null);
        tracker.identify("  visitor-json  ");
        Truth.assertThat(afterIdentifyWork().await(5, TimeUnit.SECONDS)).isTrue();
        Truth.assertThat(requests).isEmpty();
        Truth.assertThat(pendingFlags()).containsExactly(0, 0).inOrder();
        Truth.assertThat(jsonDeviceIds(queryEvents(pending).getData()))
                .containsExactly("legacy-json", generatedDeviceId, "pending-json", "  visitor-json  ");

        restartTracker();
        context.getRegistry().register(EventFormatData.class, EventByteArray.class, new JsonDataLoader.Factory());
        CustomEvent newPending = customEvent("next-launch-json");
        eventSenderProvider.cacheEvent(newPending);
        Truth.assertThat(pendingFlags()).containsExactly(0, 0, 1).inOrder();
        connectivity.setActiveNetworkInfo(network);
        eventSenderProvider.setEventNetSender((events, mediaType) -> {
            requests.add(events);
            return new SendResponse(204, 1000L);
        });
        tracker.identify("visitor-B");
        Truth.assertThat(afterIdentifyWork().await(5, TimeUnit.SECONDS)).isTrue();
        Truth.assertThat(requests).hasSize(1);
        Truth.assertThat(jsonDeviceIds(requests.get(0)))
                .containsExactly("legacy-json", generatedDeviceId, "pending-json", "  visitor-json  ",
                        "next-launch-json", "visitor-B");
        Truth.assertThat(queryEvents(pending).getSum()).isEqualTo(0);
    }

    @Test
    public void protobufRetryKeepsConfirmedDeviceIdAcrossRestart() throws Exception {
        context.getRegistry().register(EventFormatData.class, EventByteArray.class, new ProtobufDataLoader.Factory());
        String generatedDeviceId = context.getDeviceInfoProvider().getDeviceId();
        CustomEvent legacy = legacyEvent("legacy-protobuf");
        CustomEvent pending = customEvent("pending-protobuf");
        eventSenderProvider.cacheEvent(legacy);
        eventSenderProvider.cacheEvent(pending);
        List<byte[]> requests = new ArrayList<>();
        eventSenderProvider.setEventNetSender((events, mediaType) -> {
            requests.add(events);
            return new SendResponse(requests.size() == 1 ? 503 : 204, 1000L);
        });

        eventSenderProvider.sendEvents(false);
        Truth.assertThat(requests).isEmpty();
        ForceLoginProvider forceLoginProvider = context.getProvider(ForceLoginProvider.class);
        Truth.assertThat(forceLoginProvider.identify("visitor-protobuf")).isTrue();
        eventSenderProvider.sendEvents(false);

        Truth.assertThat(requests).hasSize(1);
        Truth.assertThat(protobufDeviceIds(requests.get(0)))
                .containsExactly("legacy-protobuf", generatedDeviceId, "pending-protobuf", "visitor-protobuf");
        EventDbResult cachedAfterFailure = queryEvents(pending);
        Truth.assertThat(cachedAfterFailure.getSum()).isEqualTo(2);
        Truth.assertThat(protobufDeviceIds(cachedAfterFailure.getData()))
                .containsExactly("legacy-protobuf", generatedDeviceId, "pending-protobuf", "visitor-protobuf");
        Truth.assertThat(pendingFlags()).containsExactly(0, 0).inOrder();

        restartTracker();
        context.getRegistry().register(EventFormatData.class, EventByteArray.class, new ProtobufDataLoader.Factory());
        eventSenderProvider.cacheEvent(customEvent("next-launch-protobuf"));
        Truth.assertThat(pendingFlags()).containsExactly(0, 0, 1).inOrder();
        eventSenderProvider.setEventNetSender((events, mediaType) -> {
            requests.add(events);
            return new SendResponse(204, 1000L);
        });
        ForceLoginProvider restarted = context.getProvider(ForceLoginProvider.class);
        Truth.assertThat(restarted.identify("visitor-B")).isTrue();
        eventSenderProvider.sendEvents(false);
        Map<String, String> retried = new HashMap<>();
        for (byte[] request : requests.subList(1, requests.size())) {
            retried.putAll(protobufDeviceIds(request));
        }
        Truth.assertThat(retried).containsExactly("legacy-protobuf", generatedDeviceId, "pending-protobuf", "visitor-protobuf",
                "next-launch-protobuf", "visitor-B");
        Truth.assertThat(queryEvents(pending).getSum()).isEqualTo(0);
    }

    @Test
    public void identifyWaitsForAnInFlightPendingInsert() throws Exception {
        context.getRegistry().register(EventFormatData.class, EventByteArray.class, new JsonDataLoader.Factory());
        CustomEvent pending = customEvent("in-flight");
        List<byte[]> requests = new ArrayList<>();
        eventSenderProvider.setEventNetSender((events, mediaType) -> {
            requests.add(events);
            return new SendResponse(204, 1000L);
        });
        contentProvider.pausePendingInsert = true;
        ExecutorService writer = Executors.newSingleThreadExecutor();
        try {
            Future<?> insertion = writer.submit(() -> eventSenderProvider.cacheEvent(pending));
            Truth.assertThat(contentProvider.insertStarted.await(5, TimeUnit.SECONDS)).isTrue();
            tracker.identify("visitor-A");
            CountDownLatch identifyFinished = afterIdentifyWork();
            Truth.assertThat(identifyFinished.await(200, TimeUnit.MILLISECONDS)).isFalse();

            contentProvider.finishInsert.countDown();
            insertion.get(5, TimeUnit.SECONDS);
            Truth.assertThat(identifyFinished.await(5, TimeUnit.SECONDS)).isTrue();
            Truth.assertThat(requests).hasSize(1);
            Truth.assertThat(jsonDeviceIds(requests.get(0))).containsExactly("in-flight", "visitor-A");
            Truth.assertThat(pendingFlags()).isEmpty();
        } finally {
            contentProvider.finishInsert.countDown();
            writer.shutdownNow();
        }
    }

    @Test
    public void confirmsEventsInsertedAfterIdentifyBeforeTheyCanBeSent() throws Exception {
        context.getRegistry().register(EventFormatData.class, EventByteArray.class, new JsonDataLoader.Factory());
        CustomEvent lateLocalEvent = customEvent("late-local");
        CustomEvent lateSharedEvent = customEvent("late-shared");
        context.getConfigurationProvider().core().setDataCollectionEnabled(false);
        tracker.identify("visitor-A");
        Truth.assertThat(afterIdentifyWork().await(5, TimeUnit.SECONDS)).isTrue();

        eventSenderProvider.cacheEvent(lateLocalEvent);
        Truth.assertThat(pendingFlags()).containsExactly(0);
        Truth.assertThat(jsonDeviceIds(queryEvents(lateLocalEvent).getData())).containsExactly("late-local", "visitor-A");

        // Simulate a pending row inserted by another process sharing the provider.
        contentProvider.dbHelper.getWritableDatabase().insertOrThrow(EventDataTable.TABLE_EVENTS, null,
                EventDataTable.putValues(EventBuilderProvider.toJson(lateSharedEvent).toString().getBytes(),
                        "TRACK", lateSharedEvent.getSendPolicy(), true));
        Truth.assertThat(pendingFlags()).containsExactly(0, 1).inOrder();
        List<byte[]> requests = new ArrayList<>();
        eventSenderProvider.setEventNetSender((events, mediaType) -> {
            Truth.assertThat(pendingFlags()).containsExactly(0, 0).inOrder();
            requests.add(events);
            return new SendResponse(204, 1000L);
        });
        eventSenderProvider.sendEvents(false);
        Truth.assertThat(requests).isEmpty();
        context.getConfigurationProvider().core().setDataCollectionEnabled(true);
        eventSenderProvider.sendEvents(false);
        Truth.assertThat(requests).hasSize(1);
        Truth.assertThat(jsonDeviceIds(requests.get(0)))
                .containsExactly("late-local", "visitor-A", "late-shared", "visitor-A");
    }

    @Test
    public void retriesFailedConfirmationWithTheSameDeviceId() throws Exception {
        context.getRegistry().register(EventFormatData.class, EventByteArray.class, new JsonDataLoader.Factory());
        CustomEvent first = customEvent("first");
        CustomEvent second = customEvent("second");
        eventSenderProvider.cacheEvent(first);
        eventSenderProvider.cacheEvent(second);
        contentProvider.dbHelper.getWritableDatabase().execSQL("CREATE TRIGGER fail_pending_update BEFORE UPDATE OF _data ON events "
                + "WHEN OLD._id = (SELECT MAX(_id) FROM events) BEGIN SELECT RAISE(ABORT, 'test failure'); END");

        List<byte[]> requests = new ArrayList<>();
        eventSenderProvider.setEventNetSender((events, mediaType) -> {
            requests.add(events);
            return new SendResponse(204, 1000L);
        });
        ForceLoginProvider forceLoginProvider = context.getProvider(ForceLoginProvider.class);
        Truth.assertThat(forceLoginProvider.identify("visitor-A")).isTrue();
        eventSenderProvider.sendEvents(false);
        Truth.assertThat(requests).isEmpty();
        Truth.assertThat(pendingFlags()).containsExactly(1, 1).inOrder();
        Truth.assertThat(jsonDeviceIds(queryEvents(first).getData()))
                .containsExactly("first", first.getDeviceId(), "second", second.getDeviceId());
        Truth.assertThat(forceLoginProvider.identify("visitor-B")).isFalse();

        contentProvider.dbHelper.getWritableDatabase().execSQL("DROP TRIGGER fail_pending_update");
        eventSenderProvider.sendEvents(false);
        Truth.assertThat(requests).hasSize(1);
        Truth.assertThat(jsonDeviceIds(requests.get(0))).containsExactly("first", "visitor-A", "second", "visitor-A");
        Truth.assertThat(pendingFlags()).isEmpty();
    }

    @Test
    public void confirmsAcrossDatabasePagesAndSkipsMalformedRecords() throws Exception {
        context.getRegistry().register(EventFormatData.class, EventByteArray.class, new JsonDataLoader.Factory());
        CustomEvent pending = customEvent("pending");
        byte[] data = EventBuilderProvider.toJson(pending).toString().getBytes();
        SQLiteDatabase db = contentProvider.dbHelper.getWritableDatabase();
        db.insertOrThrow(EventDataTable.TABLE_EVENTS, null,
                EventDataTable.putValues("{malformed}".getBytes(), "TRACK", pending.getSendPolicy(), true));
        for (int i = 0; i < 105; i++) {
            db.insertOrThrow(EventDataTable.TABLE_EVENTS, null, EventDataTable.putValues(data, "TRACK", pending.getSendPolicy(), true));
        }

        context.getConfigurationProvider().core().setDataCollectionEnabled(false);
        tracker.identify("visitor-A");
        Truth.assertThat(afterIdentifyWork().await(5, TimeUnit.SECONDS)).isTrue();

        try (Cursor cursor = db.query(EventDataTable.TABLE_EVENTS,
                new String[]{EventDataTable.COLUMN_FORCE_LOGIN_PENDING, EventDataTable.COLUMN_DATA},
                null, null, null, null, EventDataTable.COLUMN_ID)) {
            Truth.assertThat(cursor.getCount()).isEqualTo(106);
            Truth.assertThat(cursor.moveToFirst()).isTrue();
            Truth.assertThat(cursor.getInt(0)).isEqualTo(1);
            Truth.assertThat(new String(cursor.getBlob(1))).isEqualTo("{malformed}");
            while (cursor.moveToNext()) {
                Truth.assertThat(cursor.getInt(0)).isEqualTo(0);
                Truth.assertThat(new JSONObject(new String(cursor.getBlob(1))).getString("deviceId")).isEqualTo("visitor-A");
            }
        }
    }

    private CountDownLatch afterIdentifyWork() {
        CountDownLatch sent = new CountDownLatch(1);
        Handler sender = ReflectionHelpers.getField(eventSenderProvider, "sendHandler");
        TrackMainThread.trackMain().postActionToTrackMain(() -> sender.post(sent::countDown));
        return sent;
    }

    private List<Integer> pendingFlags() {
        List<Integer> flags = new ArrayList<>();
        try (Cursor cursor = contentProvider.dbHelper.getReadableDatabase().query(EventDataTable.TABLE_EVENTS,
                new String[]{EventDataTable.COLUMN_FORCE_LOGIN_PENDING}, null, null, null, null, EventDataTable.COLUMN_ID)) {
            while (cursor.moveToNext()) {
                flags.add(cursor.getInt(0));
            }
        }
        return flags;
    }

    public static class TestEventDataContentProvider extends EventDataContentProvider {
        final CountDownLatch insertStarted = new CountDownLatch(1);
        final CountDownLatch finishInsert = new CountDownLatch(1);
        volatile boolean pausePendingInsert;

        @Override
        public Uri insert(Uri uri, ContentValues values) {
            if (pausePendingInsert && values.containsKey(EventDataTable.COLUMN_FORCE_LOGIN_PENDING)) {
                insertStarted.countDown();
                try {
                    if (!finishInsert.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Timed out waiting to insert the pending event");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                }
            }
            return super.insert(uri, values);
        }
    }

    /**
     * 未开启强制登录时构建的事件，模拟老版本 SDK 或此前启动已确认身份的事件
     */
    private CustomEvent legacyEvent(String eventName) {
        CoreConfiguration core = context.getConfigurationProvider().core();
        core.setForceLogin(false);
        try {
            return customEvent(eventName);
        } finally {
            core.setForceLogin(true);
        }
    }

    private Map<String, String> jsonDeviceIds(byte[] data) throws Exception {
        Map<String, String> result = new HashMap<>();
        JSONArray events = new JSONArray(new String(data));
        for (int i = 0; i < events.length(); i++) {
            result.put(events.getJSONObject(i).getString("eventName"), events.getJSONObject(i).getString("deviceId"));
        }
        return result;
    }

    private Map<String, String> protobufDeviceIds(byte[] data) throws Exception {
        Map<String, String> result = new HashMap<>();
        EventV3Protocol.EventV3List list = EventV3Protocol.EventV3List.parseFrom(data);
        for (EventV3Protocol.EventV3Dto event : list.getValuesList()) {
            result.put(event.getEventName(), event.getDeviceId());
        }
        return result;
    }

    private CustomEvent customEvent(String eventName) {
        CustomEvent.Builder builder = new CustomEvent.Builder().setEventName(eventName);
        builder.readPropertyInTrackThread(context);
        return builder.build();
    }

    private EventDbResult queryEvents(CustomEvent event) {
        ModelLoader<EventDatabase, EventDbResult> modelLoader =
                context.getRegistry().getModelLoader(EventDatabase.class, EventDbResult.class);
        return modelLoader.buildLoadData(EventDatabase.query(event.getSendPolicy(), 10)).fetcher.executeData();
    }
}
