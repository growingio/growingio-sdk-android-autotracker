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

package com.growingio.android.sdk.track.middleware;

import android.annotation.SuppressLint;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import com.growingio.android.sdk.track.log.Logger;

import java.io.IOException;
import java.util.List;

import static com.growingio.android.sdk.track.middleware.EventsInfoTable.COLUMN_CREATE_TIME;
import static com.growingio.android.sdk.track.middleware.EventsInfoTable.COLUMN_DATA;
import static com.growingio.android.sdk.track.middleware.EventsInfoTable.COLUMN_EVENT_TYPE;
import static com.growingio.android.sdk.track.middleware.EventsInfoTable.COLUMN_ID;
import static com.growingio.android.sdk.track.middleware.EventsInfoTable.COLUMN_POLICY;
import static com.growingio.android.sdk.track.middleware.EventsInfoTable.TABLE_EVENTS;
import static com.growingio.android.sdk.track.middleware.EventsInfoTable.getContentUri;
import static com.growingio.android.sdk.track.middleware.EventsInfoTable.putValues;

public class EventsSQLite {
    private static final String TAG = "EventsSQLite";

    private static final long EVENT_VALID_PERIOD_MILLS = 7L * 24 * 60 * 60_000;

    private final Context context;
    private final String eventsInfoAuthority;

    EventsSQLite(Context context) {
        this.context = context;
        eventsInfoAuthority = context.getPackageName() + ".EventsContentProvider";
    }

    void insertEvent(GEvent gEvent) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = getContentUri();

            EventsInfo eventsInfo = new EventsInfo(gEvent.getEventType(), gEvent.getSendPolicy(), Serializer.objectSerialize(gEvent));
            ContentValues contentValues = putValues(eventsInfo);
            contentResolver.insert(uri, contentValues);
        } catch (Exception e) {
            // https://issuetracker.google.com/issues/37124513
            Logger.e(TAG, e, "insertEvent failed: %s", e.getMessage());
        }
    }

    void removeOverdueEvents() {
        long current = System.currentTimeMillis();
        long sevenDayAgo = current - EVENT_VALID_PERIOD_MILLS;

        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = getContentUri();
        int deleteNum = contentResolver.delete(uri, COLUMN_CREATE_TIME + "<=" + sevenDayAgo, null);

        Logger.e(TAG, "removeOverdueEvents: deleteNum: %d", deleteNum);
    }

    long queryEvents(int policy, int limit, List<GEvent> events) {
        Cursor cursor = null;
        long lastId = -1;
        ContentProviderClient client = context.getContentResolver().acquireContentProviderClient(eventsInfoAuthority);
        try {
            cursor = queryEvents(client, policy, limit);
            while (cursor.moveToNext()) {
                if (cursor.isLast()) {
                    lastId = cursor.getLong(cursor.getColumnIndex(EventsInfoTable.COLUMN_ID));
                }
                byte[] data = cursor.getBlob(cursor.getColumnIndex(EventsInfoTable.COLUMN_DATA));
                GEvent event = unpack(data);
                if (event != null) {
                    events.add(event);
                } else {
                    long delId = cursor.getLong(cursor.getColumnIndex(EventsInfoTable.COLUMN_ID));
                    removeEventById(client, delId);
                }
            }
        } catch (Throwable t) {
            Logger.e(TAG, t, t.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (client != null) {
                client.release();
            }
        }
        return lastId;
    }


    long queryEventsAndDelete(int policy, int limit, List<GEvent> events) {
        Cursor cursor = null;
        long lastId = -1;
        ContentProviderClient client = context.getContentResolver().acquireContentProviderClient(eventsInfoAuthority);
        try {
            cursor = queryEvents(client, policy, limit);
            while (cursor.moveToNext()) {
                if (cursor.isLast()) {
                    lastId = cursor.getLong(cursor.getColumnIndex(EventsInfoTable.COLUMN_ID));
                }
                byte[] data = cursor.getBlob(cursor.getColumnIndex(EventsInfoTable.COLUMN_DATA));
                GEvent event = unpack(data);
                if (event != null) {
                    events.add(event);
                }
                long delId = cursor.getLong(cursor.getColumnIndex(EventsInfoTable.COLUMN_ID));
                removeEventById(client, delId);
            }
        } catch (Throwable t) {
            Logger.e(TAG, t, t.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (client != null) {
                client.release();
            }
        }
        return lastId;
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


    void removeEvents(long lastId, int policy, String eventType) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = getContentUri();
        contentResolver.delete(uri,
                COLUMN_ID + "<=? AND " + COLUMN_EVENT_TYPE + "=? AND " + COLUMN_POLICY + "=?",
                new String[]{String.valueOf(lastId), eventType, String.valueOf(policy)});
    }

    private GEvent unpack(byte[] data) {
        try {
            return Serializer.objectDeserialization(data);
        } catch (IOException e) {
            Logger.e(TAG, e, e.getMessage());
        } catch (ClassNotFoundException e) {
            Logger.e(TAG, e, e.getMessage());
        }
        return null;
    }

    // 清库
    void removeAllEvents() {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = getContentUri();
        contentResolver.delete(uri, null, null);
    }
}
