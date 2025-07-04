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

import android.annotation.SuppressLint;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteFullException;
import android.net.Uri;
import android.os.RemoteException;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.events.AutotrackEventType;
import com.growingio.android.sdk.track.events.TrackEventType;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.format.EventByteArray;
import com.growingio.android.sdk.track.middleware.EventDbResult;
import com.growingio.android.sdk.track.middleware.format.EventFormatData;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.modelloader.ModelLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.growingio.android.database.EventDataTable.COLUMN_DATA;
import static com.growingio.android.database.EventDataTable.COLUMN_EVENT_TYPE;
import static com.growingio.android.database.EventDataTable.COLUMN_ID;
import static com.growingio.android.database.EventDataTable.COLUMN_POLICY;
import static com.growingio.android.database.EventDataTable.TABLE_EVENTS;

public class EventDataManager {
    private static final String TAG = "EventDataManager";

    private static final long EVENT_VALID_PERIOD_MILLS = 24 * 60 * 60_000L;
    private static final double EVENT_DATA_MAX_SIZE = 2 * 1000 * 1024; // 2M

    private final TrackerContext context;
    private final String eventsInfoAuthority;
    private boolean ignoreOperations = false;

    EventDataManager(TrackerContext context) {
        this.context = context;
        eventsInfoAuthority = context.getPackageName() + "." + EventDataContentProvider.CONTENT_PROVIDER_NAME;

        DeprecatedEventSQLite deprecatedEventSQLite = new DeprecatedEventSQLite(context, this);
        deprecatedEventSQLite.migrateEvents();

        // when sdk start,removeOverdueEvents
        int day = context.getConfigurationProvider().core().getDataValidityPeriod();
        removeOverdueEvents(day);
    }

    private EventByteArray formatData(EventFormatData data) {
        ModelLoader<EventFormatData, EventByteArray> modelLoader = context.getRegistry().getModelLoader(EventFormatData.class, EventByteArray.class);
        if (modelLoader == null) {
            Logger.e(TAG, "please register eventformat component first");
            return null;
        }
        ModelLoader.LoadData<EventByteArray> loadData = modelLoader.buildLoadData(data);
        return loadData.fetcher.executeData();
    }

    int insertEvents(List<GEvent> events) {
        int count = 0;
        for (GEvent event : events) {
            Uri uri = insertEvent(event);
            if (uri != null) count++;
        }
        return count;
    }

    Uri insertEvent(GEvent gEvent) {
        if (ignoreOperations) {
            return null;
        }

        try {
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = getContentUri();

            EventByteArray data = formatData(EventFormatData.format(gEvent));
            if (data != null && data.getBodyData() != null) {
                if (data.getBodyData().length > EVENT_DATA_MAX_SIZE) {
                    // SQLiteBlobTooBigException: Row too big to fit into CursorWindow
                    // cursor window default is 2M, so we should limit the data size
                    Logger.e(TAG, "event data is too large, ignore it.");
                    return null;
                }
                ContentValues contentValues = EventDataTable.putValues(data.getBodyData(), getDatabaseEventType(gEvent), gEvent.getSendPolicy());
                return contentResolver.insert(uri, contentValues);
            }
        } catch (SQLiteFullException e) {
            onDiskFull(e);
        } catch (Exception e) {
            // https://issuetracker.google.com/issues/37124513
            Logger.e(TAG, e, "insertEvent failed");
        }
        return null;
    }

    int removeOverdueEvents(int day) {
        if (ignoreOperations) {
            return -1;
        }
        try {
            long current = System.currentTimeMillis();
            if (day > 30) day = 30;
            if (day < 3) day = 3;
            long sevenDayAgo = current - day * EVENT_VALID_PERIOD_MILLS;

            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = getContentUri();
            return contentResolver.delete(uri, EventDataTable.COLUMN_CREATE_TIME + "<=" + sevenDayAgo, null);
        } catch (SQLiteFullException e) {
            onDiskFull(e);
            return -1;
        } catch (Exception e) {
            Logger.e(TAG, e, "removeOverdueEvents failed");
            return -1;
        }
    }

    void queryEvents(int policy, int limit, EventDbResult dbResult) {
        // query 判断磁盘空间是否已满，避免ignoreOperations的情况下，重复发送同一事件
        if (ignoreOperations) {
            dbResult.setSuccess(false);
            return;
        }
        List<byte[]> queryList = new ArrayList<>();
        ContentProviderClient client = context.getContentResolver().acquireContentProviderClient(eventsInfoAuthority);
        try (Cursor cursor = queryEvents(client, policy, limit)) {
            int count = 0;
            double dataSize = 0;
            while (cursor.moveToNext()) {
                count++;
                String eventType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_TYPE));
                dbResult.setEventType(eventType);
                byte[] data = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_DATA));
                if (data != null && data.length < EVENT_DATA_MAX_SIZE) {
                    dataSize += data.length;
                    if (dataSize > EVENT_DATA_MAX_SIZE) {
                        break;
                    }
                    queryList.add(data);
                    long lastId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    dbResult.setLastId(lastId);
                } else {
                    Logger.e(TAG, "event data is too large or null, delete it.");
                    // event data is illegal, delete it.
                    long delId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    removeEventById(client, delId);
                }
            }
            EventByteArray result = formatData(EventFormatData.merge(queryList));
            if (result != null && result.getBodyData() != null) {
                dbResult.setSum(count);
                dbResult.setSuccess(true);
                dbResult.setData(result.getBodyData());
                dbResult.setMediaType(result.getMediaType());
            }
        } catch (SQLiteFullException e) {
            dbResult.setSuccess(false);
            onDiskFull(e);
        } catch (Throwable t) {
            dbResult.setSuccess(false);
            Logger.e(TAG, t, t.getMessage());
        } finally {
            if (client != null) {
                try {
                    client.release();
                } catch (java.lang.NullPointerException e) {
                    // does nothing, Binder connection already null
                }
            }
        }
    }

    void queryEventsAndDelete(int policy, int limit, EventDbResult dbResult) {
        if (ignoreOperations) {
            dbResult.setSuccess(false);
            return;
        }
        ContentProviderClient client = context.getContentResolver().acquireContentProviderClient(eventsInfoAuthority);
        List<byte[]> queryList = new ArrayList<>();
        try (Cursor cursor = queryEvents(client, policy, limit)) {
            int count = 0;
            double dataSize = 0;
            while (cursor.moveToNext()) {
                count++;
                byte[] data = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_DATA));
                if (data != null && data.length < EVENT_DATA_MAX_SIZE) {
                    dataSize += data.length;
                    if (dataSize > EVENT_DATA_MAX_SIZE) {
                        break;
                    }
                    queryList.add(data);
                    long lastId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    dbResult.setLastId(lastId);
                }
                long delId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                removeEventById(client, delId);
            }
            EventByteArray result = formatData(EventFormatData.merge(queryList));
            if (result != null && result.getBodyData() != null) {
                dbResult.setSum(count);
                dbResult.setSuccess(true);
                dbResult.setData(result.getBodyData());
                dbResult.setMediaType(result.getMediaType());
            }
        } catch (SQLiteFullException e) {
            onDiskFull(e);
        } catch (Throwable t) {
            Logger.e(TAG, t, t.getMessage());
        } finally {
            if (client != null) {
                try {
                    client.release();
                } catch (java.lang.NullPointerException e) {
                    // does nothing, Binder connection already null
                }
            }
        }
    }

    int updateEventsWhenSendFailed(long lastId, String eventType) {
        if (ignoreOperations) {
            return -1;
        }
        if (eventType.equals(UNDELIVERED_EVENT_TYPE)) {
            //does not need to be updated
            return 0;
        }
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_EVENT_TYPE, UNDELIVERED_EVENT_TYPE);
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = getContentUri();
            return contentResolver.update(uri,
                    contentValues,
                    COLUMN_ID + "<=? AND " + COLUMN_EVENT_TYPE + "=?",
                    new String[]{String.valueOf(lastId), eventType});
        } catch (SQLiteFullException e) {
            onDiskFull(e);
            return -1;
        } catch (Exception e) {
            Logger.e(TAG, e, "updateEvents failed");
            return -1;
        }
    }

    @SuppressLint("Recycle")
    private Cursor queryEvents(ContentProviderClient client, int policy, int limit) throws RemoteException {
        Uri uri = getContentUri();
        String subSelect = "SELECT " + COLUMN_EVENT_TYPE
                + " FROM " + TABLE_EVENTS + " WHERE " + COLUMN_POLICY + "=" + policy
                + " LIMIT 1";
        String sql = "SELECT " + COLUMN_ID + ", " + COLUMN_DATA + ", "
                + COLUMN_EVENT_TYPE
                + " FROM " + TABLE_EVENTS
                + " WHERE " + COLUMN_EVENT_TYPE + "=(" + subSelect + ") AND " + COLUMN_POLICY + "=" + policy
                + " ORDER BY " + COLUMN_ID + " ASC "
                + " LIMIT " + limit + ";";
        if (client == null) {
            ContentResolver contentResolver = context.getContentResolver();
            return contentResolver.query(uri, null, sql, null, "rawQuery");
        } else {
            return client.query(uri, null, sql, null, "rawQuery");
        }
    }

    private void removeEventById(ContentProviderClient client, long id) throws RemoteException {
        Uri uri = getContentUri();
        if (client == null) {
            ContentResolver contentResolver = context.getContentResolver();
            contentResolver.delete(uri, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        } else {
            client.delete(uri, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        }
    }

    int removeEvents(long lastId, int policy, String eventType) {
        if (ignoreOperations) {
            return -1;
        }

        try {
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = getContentUri();
            return contentResolver.delete(uri,
                    COLUMN_ID + "<=? AND " + COLUMN_EVENT_TYPE + "=? AND " + COLUMN_POLICY + "=?",
                    new String[]{String.valueOf(lastId), eventType, String.valueOf(policy)});
        } catch (SQLiteFullException e) {
            onDiskFull(e);
            return -1;
        } catch (Exception e) {
            Logger.e(TAG, e, "removeEvents failed");
            return -1;
        }
    }

    // 清库
    void removeAllEvents() {
        if (ignoreOperations) {
            return;
        }

        try {
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = getContentUri();
            contentResolver.delete(uri, null, null);
            Logger.w(TAG, "removeAllEvents success");
        } catch (SQLiteFullException e) {
            onDiskFull(e);
        } catch (Exception e) {
            Logger.e(TAG, e, "removeAllEvents failed");
        }
    }

    public Uri getContentUri() {
        return Uri.parse("content://" + context.getPackageName() + "." + EventDataContentProvider.CONTENT_PROVIDER_NAME + File.separator + TABLE_EVENTS);
    }

    private void onDiskFull(SQLiteFullException e) {
        Logger.w(TAG, e, "Disk full, all operations will be ignored");
        ignoreOperations = true;
    }


    private String getDatabaseEventType(GEvent gEvent) {
        String eventType = gEvent.getEventType();
        switch (eventType) {
            case TrackEventType.VISIT:
            case TrackEventType.ACTIVATE:
            case TrackEventType.REENGAGE:
                return INSTANT_EVENT_TYPE;

            case AutotrackEventType.PAGE:
            case AutotrackEventType.PAGE_ATTRIBUTES:
            case AutotrackEventType.VIEW_CLICK:
            case AutotrackEventType.VIEW_CHANGE:
                return AUTOTRACK_EVENT_TYPE;

            case TrackEventType.CUSTOM:
            case TrackEventType.VISITOR_ATTRIBUTES:
            case TrackEventType.LOGIN_USER_ATTRIBUTES:
            case TrackEventType.CONVERSION_VARIABLES:
                return TRACK_EVENT_TYPE;
            default:
                return OTHER_EVENT_TYPE;
        }
    }

    private static final String INSTANT_EVENT_TYPE = "INSTANT";
    private static final String AUTOTRACK_EVENT_TYPE = "AUTOTRACK";
    private static final String TRACK_EVENT_TYPE = "TRACK";
    private static final String OTHER_EVENT_TYPE = "OTHER";
    private static final String UNDELIVERED_EVENT_TYPE = "UNDELIVERED";
}
