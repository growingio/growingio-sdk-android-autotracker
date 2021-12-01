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
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteFullException;
import android.util.Log;

import com.growingio.android.sdk.track.events.base.BaseEvent;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.middleware.Serializer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.growingio.database.EventDataTable.COLUMN_DATA;
import static com.growingio.database.EventDataTable.COLUMN_ID;
import static com.growingio.database.EventDataTable.TABLE_EVENTS;


/**
 * 用来迁移v3.3.3版本之前的数据库数据
 */
@Deprecated
public class DeprecatedEventSQLite {
    private static final String TAG = "DeprecatedEventSQLite";

    private final static String DATABASE_NAME = "growing3.db";
    private final Context context;
    private boolean ignoreOperations = false;
    private final Object lock = new Object();
    public EventDataSQLiteOpenHelper dbHelper;
    private final EventDataManager dataManager;

    DeprecatedEventSQLite(Context context, EventDataManager dataManager) {
        this.dataManager = dataManager;
        this.context = context;
        //if app don't has deprecated db file, return;
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        if (!dbFile.exists()) {
            ignoreOperations = true;
        }
    }

    //超过20s后不再执行
    public void migrateEvents() {
        long runningTime = System.currentTimeMillis();
        while (migrateEvent()) {
            if (System.currentTimeMillis() - runningTime > 20000) {
                break;
            }
        }
    }

    void insert(GEvent gEvent) {
        if (this.dbHelper == null) {
            this.dbHelper = new EventDataSQLiteOpenHelper(context, DATABASE_NAME);
        }
        try {
            synchronized (lock) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.insert(EventDataTable.TABLE_EVENTS, null, EventDataTable.putValues((Serializer.objectSerialize(gEvent)), gEvent.getEventType(), gEvent.getSendPolicy()));
            }
        } catch (SQLiteFullException e) {
            onDiskFull(e);
        } catch (Throwable t) {
            Logger.e(TAG, t, t.getMessage());
        }
    }

    @SuppressLint("Recycle")
    private boolean migrateEvent() {
        if (ignoreOperations) return false;
        if (this.dbHelper == null) {
            this.dbHelper = new EventDataSQLiteOpenHelper(context, DATABASE_NAME);
        }
        String querySql = "SELECT * FROM " + TABLE_EVENTS + " ORDER BY " + COLUMN_ID + " DESC LIMIT " + 100 + ";";
        try {
            synchronized (lock) {
                List<GEvent> events = new ArrayList<>();
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                try (Cursor cursor = db.rawQuery(querySql, null)) {
                    long lastId = 0;
                    while (cursor.moveToNext()) {
                        if (cursor.isLast()) {
                            lastId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                        }
                        byte[] data = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_DATA));
                        GEvent event = Serializer.unpack(data);
                        if (event != null) {
                            events.add(event);
                            if (event instanceof BaseEvent) {
                                Logger.printJson(TAG, "------migrate data------", ((BaseEvent) event).toJSONObject().toString());
                            }
                        }
                    }
                    dataManager.insertEvents(events);
                    if (events.size() < 100) {
                        File dbFile = context.getDatabasePath(DATABASE_NAME);
                        if (dbFile.exists()) {
                            boolean result = dbFile.delete();
                            return !result;
                        }
                        return false;
                    } else {
                        if (lastId > 0) {
                            db.delete(EventDataTable.TABLE_EVENTS, COLUMN_ID + ">=?", new String[]{String.valueOf(lastId)});
                        }
                        return true;
                    }

                } catch (SQLiteFullException e) {
                    onDiskFull(e);
                } catch (Throwable t) {
                    Logger.e(TAG, t, t.getMessage());
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "query error: " + e.getMessage());
        }
        return false;
    }

    private void onDiskFull(SQLiteFullException e) {
        Logger.e(TAG, e, "Disk full, all operations will be ignored");
        ignoreOperations = true;
    }

}
