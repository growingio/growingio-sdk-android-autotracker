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
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.growingio.android.sdk.track.log.Logger;

import java.io.IOException;
import java.util.List;

public class EventsSQLite {
    private static final String TAG = "EventsSQLite";

    private static final long EVENT_VALID_PERIOD_MILLS = 7 * 24 * 60 * 60_000;

    private final DBSQLiteOpenHelper mDbHelper;

    EventsSQLite(Context context) {
        mDbHelper = new DBSQLiteOpenHelper(context, "growing3.db");
    }

    void insertEvent(GEvent gEvent) {
        try {
            mDbHelper.insertEvents(Serializer.objectSerialize(gEvent), gEvent.getEventType(), gEvent.getSendPolicy());
        } catch (IOException e) {
            Logger.e(TAG, e, "insertEvent failed: %s", e.getMessage());
        }
    }

    void removeOverdueEvents() {
        long current = System.currentTimeMillis();
        long sevenDayAgo = current - EVENT_VALID_PERIOD_MILLS;
        mDbHelper.removeOverdueEvents(sevenDayAgo);
    }

    long queryEvents(int policy, int limit, List<GEvent> events) {
        Cursor cursor = null;
        long lastId = -1;
        try {
            cursor = mDbHelper.queryEvents(policy, limit);
            while (cursor.moveToNext()) {
                if (cursor.isLast()) {
                    lastId = cursor.getLong(cursor.getColumnIndex(DBSQLiteOpenHelper.COLUMN_ID));
                }
                byte[] data = cursor.getBlob(cursor.getColumnIndex(DBSQLiteOpenHelper.COLUMN_DATA));
                GEvent event = unpack(data);
                if (event != null) {
                    events.add(event);
                } else {
                    long delId = cursor.getLong(cursor.getColumnIndex(DBSQLiteOpenHelper.COLUMN_ID));
                    mDbHelper.removeEventById(delId);
                }
            }
        } catch (Throwable t) {
            Logger.e(TAG, t, t.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return lastId;
    }

    void removeEvents(long lastId, int policy, String eventType) {
        mDbHelper.removeEvents(lastId, policy, eventType);
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

    void removeAllEvents() {
        mDbHelper.removeAllEvents();
    }

    static class DBSQLiteOpenHelper extends SQLiteOpenHelper {
        private static final int DATABASE_VERSION = 1;

        private static final String TABLE_EVENTS = "events";
        private static final String COLUMN_ID = "_id";
        private static final String COLUMN_CREATE_TIME = "_created";
        private static final String COLUMN_LAST_MODIFIED = "_modified";
        private static final String COLUMN_DATA = "_data";
        private static final String COLUMN_EVENT_TYPE = "_event_type";
        private static final String COLUMN_POLICY = "_policy";

        private static final String CREATE_TABLE_EVENTS =
                "CREATE TABLE IF NOT EXISTS " + TABLE_EVENTS + "(\n"
                        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                        + COLUMN_CREATE_TIME + " INTEGER NOT NULL, \n"
                        + COLUMN_LAST_MODIFIED + " INTEGER NOT NULL, \n"
                        + COLUMN_DATA + " BLOB NOT NULL, \n"
                        + COLUMN_EVENT_TYPE + " TEXT NOT NULL, \n"
                        + COLUMN_POLICY + " INTEGER NOT NULL \n"
                        + ");";

        private static final String DROP_TABLE_EVENTS = "DROP TABLE IF EXISTS " + TABLE_EVENTS + ";";

        DBSQLiteOpenHelper(Context context, String databaseName) {
            super(context, databaseName, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_EVENTS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DROP_TABLE_EVENTS);
            onCreate(db);
        }

        public void insertEvents(byte[] data, String eventType, int policy) {
            long current = System.currentTimeMillis();
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_CREATE_TIME, current);
            contentValues.put(COLUMN_LAST_MODIFIED, current);
            contentValues.put(COLUMN_DATA, data);
            contentValues.put(COLUMN_EVENT_TYPE, eventType);
            contentValues.put(COLUMN_POLICY, policy);
            getWritableDatabase().insert(TABLE_EVENTS, null, contentValues);
        }

        @SuppressLint("Recycle")
        public Cursor queryEvents(int policy, int limit) {
            String subSelect = "SELECT " + COLUMN_EVENT_TYPE
                    + " FROM " + TABLE_EVENTS + " WHERE " + COLUMN_POLICY + "=" + policy
                    + " LIMIT 1";
            String sql = "SELECT " + COLUMN_ID + ", " + COLUMN_DATA + ", "
                    + COLUMN_EVENT_TYPE
                    + " FROM " + TABLE_EVENTS
                    + " WHERE " + COLUMN_EVENT_TYPE + "=(" + subSelect + ") AND " + COLUMN_POLICY + "=" + policy
                    + " LIMIT " + limit + ";";
            return getReadableDatabase().rawQuery(sql, null);
        }

        public void removeEvents(long id, int policy, String eventType) {
            getWritableDatabase().delete(TABLE_EVENTS,
                    COLUMN_ID + "<=? AND " + COLUMN_EVENT_TYPE + "=? AND " + COLUMN_POLICY + "=?",
                    new String[]{String.valueOf(id), eventType, String.valueOf(policy)});
        }

        public void removeEventById(long id) {
            getWritableDatabase().delete(TABLE_EVENTS,
                    COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        }

        public void removeOverdueEvents(long deadline) {
            int deleteNum = getWritableDatabase().delete(TABLE_EVENTS,
                    COLUMN_CREATE_TIME + "<=" + deadline, null);
            Logger.e(TAG, "removeOverdueEvents: deleteNum: %d", deleteNum);
        }

        // 清库
        public void removeAllEvents() {
            getWritableDatabase().delete(TABLE_EVENTS, null, null);
        }
    }
}
