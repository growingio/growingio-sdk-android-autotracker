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
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.growingio.android.sdk.track.log.Logger;

import static com.growingio.android.sdk.track.middleware.EventsInfoTable.COLUMN_CREATE_TIME;
import static com.growingio.android.sdk.track.middleware.EventsInfoTable.COLUMN_DATA;
import static com.growingio.android.sdk.track.middleware.EventsInfoTable.COLUMN_EVENT_TYPE;
import static com.growingio.android.sdk.track.middleware.EventsInfoTable.COLUMN_ID;
import static com.growingio.android.sdk.track.middleware.EventsInfoTable.COLUMN_POLICY;
import static com.growingio.android.sdk.track.middleware.EventsInfoTable.TABLE_EVENTS;
import static com.growingio.android.sdk.track.middleware.EventsInfoTable.getContentUri;
import static com.growingio.android.sdk.track.middleware.EventsInfoTable.putValues;


public class EventsManager {

    private Context mContext;
    private static final String TAG = "EventManager";

    public EventsManager(Context context) {
        mContext = context;
    }

    public void insertEvents(byte[] data, String eventType, int policy) {
        ContentResolver contentResolver = mContext.getContentResolver();
        Uri uri = getContentUri();

        EventsInfo eventsInfo = new EventsInfo(eventType, policy, data);
        ContentValues contentValues = putValues(eventsInfo);
        contentResolver.insert(uri, contentValues);
    }

    @SuppressLint("Recycle")
    public Cursor queryEvents(int policy, int limit) {

        ContentResolver contentResolver = mContext.getContentResolver();
        Uri uri = getContentUri();

        String subSelect = "SELECT " + COLUMN_EVENT_TYPE
                + " FROM " + TABLE_EVENTS + " WHERE " + COLUMN_POLICY + "=" + policy
                + " LIMIT 1";
        String sql = "SELECT " + COLUMN_ID + ", " + COLUMN_DATA + ", "
                + COLUMN_EVENT_TYPE
                + " FROM " + TABLE_EVENTS
                + " WHERE " + COLUMN_EVENT_TYPE + "=(" + subSelect + ") AND " + COLUMN_POLICY + "=" + policy
                + " LIMIT " + limit + ";";

        return contentResolver.query(uri, null, sql, null, "rawQuery");
    }

    public void removeEvents(long id, int policy, String eventType) {

        ContentResolver contentResolver = mContext.getContentResolver();
        Uri uri = getContentUri();
        contentResolver.delete(uri,
                COLUMN_ID + "<=? AND " + COLUMN_EVENT_TYPE + "=? AND " + COLUMN_POLICY + "=?",
                new String[]{String.valueOf(id), eventType, String.valueOf(policy)});
    }

    public void removeEventById(long id) {
        ContentResolver contentResolver = mContext.getContentResolver();
        Uri uri = getContentUri();
        contentResolver.delete(uri, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void removeOverdueEvents(long deadline) {
        ContentResolver contentResolver = mContext.getContentResolver();
        Uri uri = getContentUri();
        int deleteNum = contentResolver.delete(uri, COLUMN_CREATE_TIME + "<=" + deadline, null);

        Logger.e(TAG, "removeOverdueEvents: deleteNum: %d", deleteNum);
    }

    // 清库
    public void removeAllEvents() {

        ContentResolver contentResolver = mContext.getContentResolver();
        Uri uri = getContentUri();
        contentResolver.delete(uri, null, null);
    }
}
