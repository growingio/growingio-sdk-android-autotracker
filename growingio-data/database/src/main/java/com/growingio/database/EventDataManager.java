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

import android.annotation.SuppressLint;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteFullException;
import android.net.Uri;
import android.os.RemoteException;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.format.EventByteArray;
import com.growingio.android.sdk.track.middleware.EventDbResult;
import com.growingio.android.sdk.track.middleware.format.EventFormatData;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.modelloader.ModelLoader;

import java.util.ArrayList;
import java.util.List;

import static com.growingio.database.EventDataTable.COLUMN_DATA;
import static com.growingio.database.EventDataTable.COLUMN_EVENT_TYPE;
import static com.growingio.database.EventDataTable.COLUMN_ID;
import static com.growingio.database.EventDataTable.COLUMN_POLICY;
import static com.growingio.database.EventDataTable.TABLE_EVENTS;
import static com.growingio.database.EventDataTable.getContentUri;

public class EventDataManager {
    private static final String TAG = "EventDataManager";

    private static final long EVENT_VALID_PERIOD_MILLS = 7L * 24 * 60 * 60_000;

    private final Context context;
    private final String eventsInfoAuthority;
    private boolean ignoreOperations = false;

    EventDataManager(Context context) {
        this.context = context;
        eventsInfoAuthority = context.getPackageName() + "." + EventDataContentProvider.CONTENT_PROVIDER_NAME;

        DeprecatedEventSQLite deprecatedEventSQLite = new DeprecatedEventSQLite(context, this);
        deprecatedEventSQLite.migrateEvents();
    }

    private EventByteArray formatData(EventFormatData data) {
        ModelLoader<EventFormatData, EventByteArray> modelLoader = TrackerContext.get().getRegistry().getModelLoader(EventFormatData.class, EventByteArray.class);
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
                ContentValues contentValues = EventDataTable.putValues(data.getBodyData(), gEvent.getEventType(), gEvent.getSendPolicy());
                //GioDatabase.insert(insert, gEvent);
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

    int removeOverdueEvents() {
        if (ignoreOperations) {
            return -1;
        }
        try {
            long current = System.currentTimeMillis();
            long sevenDayAgo = current - EVENT_VALID_PERIOD_MILLS;

            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = getContentUri();
            int deleteNum = contentResolver.delete(uri, EventDataTable.COLUMN_CREATE_TIME + "<=" + sevenDayAgo, null);
            Logger.e(TAG, "removeOverdueEvents: deleteNum: %d", deleteNum);
            return deleteNum;
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
        long lastId = -1;
        List<byte[]> queryList = new ArrayList<>();
        ContentProviderClient client = context.getContentResolver().acquireContentProviderClient(eventsInfoAuthority);
        try (Cursor cursor = queryEvents(client, policy, limit)) {
            int count = 0;
            while (cursor.moveToNext()) {
                count++;
                String eventType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_TYPE));
                dbResult.setEventType(eventType);
                if (cursor.isLast()) {
                    lastId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    dbResult.setLastId(lastId);
                }
                byte[] data = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_DATA));
                if (data != null) {
                    queryList.add(data);
                } else {
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
                client.release();
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
            while (cursor.moveToNext()) {
                count++;
                if (cursor.isLast()) {
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                }
                byte[] data = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_DATA));
                if (data != null) {
                    queryList.add(data);
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
                client.release();
            }
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
            int sum = contentResolver.delete(uri,
                    COLUMN_ID + "<=? AND " + COLUMN_EVENT_TYPE + "=? AND " + COLUMN_POLICY + "=?",
                    new String[]{String.valueOf(lastId), eventType, String.valueOf(policy)});
            Logger.d(TAG, "removeEvents: deleteNum: %d", sum);
            return sum;
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
        } catch (SQLiteFullException e) {
            onDiskFull(e);
        } catch (Exception e) {
            Logger.e(TAG, e, "removeAllEvents failed");
        }
    }

    private void onDiskFull(SQLiteFullException e) {
        Logger.e(TAG, e, "Disk full, all operations will be ignored");
        ignoreOperations = true;
    }
}
