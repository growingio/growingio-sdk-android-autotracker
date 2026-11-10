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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.growingio.android.sdk.track.log.Logger;

public class EventDataTable {

    private static final String TAG = "EventDataTable";
    public static final String TABLE_EVENTS = "events";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CREATE_TIME = "_created";
    public static final String COLUMN_LAST_MODIFIED = "_modified";
    public static final String COLUMN_DATA = "_data";
    public static final String COLUMN_EVENT_TYPE = "_event_type";
    public static final String COLUMN_POLICY = "_policy";
    // 强制登录待替换标志：1 表示 identify 前构建、上传时需替换 deviceId；默认 0
    public static final String COLUMN_FORCE_LOGIN_PENDING = "_force_login_pending";


    private EventDataTable() {
    }

    public static final String CREATE_TABLE_EVENTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_EVENTS + "(\n"
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                    + COLUMN_CREATE_TIME + " INTEGER NOT NULL, \n"
                    + COLUMN_LAST_MODIFIED + " INTEGER NOT NULL, \n"
                    + COLUMN_DATA + " BLOB NOT NULL, \n"
                    + COLUMN_EVENT_TYPE + " TEXT NOT NULL, \n"
                    + COLUMN_POLICY + " INTEGER NOT NULL, \n"
                    + COLUMN_FORCE_LOGIN_PENDING + " INTEGER NOT NULL DEFAULT 0 \n"
                    + ");";

    private static final String ADD_COLUMN_FORCE_LOGIN_PENDING =
            "ALTER TABLE " + TABLE_EVENTS + " ADD COLUMN " + COLUMN_FORCE_LOGIN_PENDING + " INTEGER NOT NULL DEFAULT 0;";

    public static final String DROP_TABLE_EVENTS = "DROP TABLE IF EXISTS " + TABLE_EVENTS + ";";

    /**
     * 老版本创建的表没有强制登录待替换列时补充该列。
     * 不通过升级数据库版本实现：onUpgrade 会删表重建，且老版本 SDK（降级后）遇到更高的数据库版本会无法打开；
     * 列默认值为 0，老版本 SDK 写入时不带该列按普通事件处理，读取时忽略该列（待替换事件按自动生成的设备 ID 上报）。
     */
    static void ensureForceLoginPendingColumn(SQLiteDatabase db) {
        boolean exists = false;
        try (Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_EVENTS + ")", null)) {
            int nameIndex = cursor.getColumnIndex("name");
            while (nameIndex >= 0 && cursor.moveToNext()) {
                if (COLUMN_FORCE_LOGIN_PENDING.equals(cursor.getString(nameIndex))) {
                    exists = true;
                    break;
                }
            }
        }
        if (!exists) {
            db.execSQL(ADD_COLUMN_FORCE_LOGIN_PENDING);
        }
    }

    /**
     * 写入事件。补列失败时表中没有待替换列，带该列的写入会失败（insert 返回 -1）：
     * 此时去掉待替换标志按普通事件写入，事件按自动生成的设备 ID 上报，不丢失。
     */
    static long insertEvent(SQLiteDatabase db, ContentValues values) {
        long rowId = db.insert(TABLE_EVENTS, null, values);
        if (rowId == -1 && values.containsKey(COLUMN_FORCE_LOGIN_PENDING)) {
            Logger.w(TAG, "insert force login pending event failed, retry without the pending flag");
            ContentValues fallback = new ContentValues(values);
            fallback.remove(COLUMN_FORCE_LOGIN_PENDING);
            rowId = db.insert(TABLE_EVENTS, null, fallback);
        }
        return rowId;
    }

    public static ContentValues putValues(byte[] data, String eventType, Integer policy) {
        return putValues(data, eventType, policy, false);
    }

    /**
     * @param forceLoginPending 强制登录待替换事件写入 1；普通事件不写该列，使用默认值 0
     */
    public static ContentValues putValues(byte[] data, String eventType, Integer policy, boolean forceLoginPending) {
        long current = System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_CREATE_TIME, current);
        contentValues.put(COLUMN_LAST_MODIFIED, current);
        contentValues.put(COLUMN_DATA, data);
        contentValues.put(COLUMN_EVENT_TYPE, eventType);
        contentValues.put(COLUMN_POLICY, policy);
        if (forceLoginPending) {
            contentValues.put(COLUMN_FORCE_LOGIN_PENDING, 1);
        }
        return contentValues;
    }

}
