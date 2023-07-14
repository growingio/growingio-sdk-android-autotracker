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

package com.growingio.android.database;

import android.content.ContentValues;
import android.net.Uri;

import com.growingio.android.sdk.TrackerContext;

import java.io.File;

public class EventDataTable {

    public static final String TABLE_EVENTS = "events";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CREATE_TIME = "_created";
    public static final String COLUMN_LAST_MODIFIED = "_modified";
    public static final String COLUMN_DATA = "_data";
    public static final String COLUMN_EVENT_TYPE = "_event_type";
    public static final String COLUMN_POLICY = "_policy";


    private EventDataTable() {
    }

    public static final String CREATE_TABLE_EVENTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_EVENTS + "(\n"
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                    + COLUMN_CREATE_TIME + " INTEGER NOT NULL, \n"
                    + COLUMN_LAST_MODIFIED + " INTEGER NOT NULL, \n"
                    + COLUMN_DATA + " BLOB NOT NULL, \n"
                    + COLUMN_EVENT_TYPE + " TEXT NOT NULL, \n"
                    + COLUMN_POLICY + " INTEGER NOT NULL \n"
                    + ");";

    public static final String DROP_TABLE_EVENTS = "DROP TABLE IF EXISTS " + TABLE_EVENTS + ";";

    public static Uri getContentUri() {
        return Uri.parse("content://" + TrackerContext.get().getPackageName() + "." + EventDataContentProvider.CONTENT_PROVIDER_NAME + File.separator + TABLE_EVENTS);
    }

    public static ContentValues putValues(byte[] data, String eventType, Integer policy) {
        long current = System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_CREATE_TIME, current);
        contentValues.put(COLUMN_LAST_MODIFIED, current);
        contentValues.put(COLUMN_DATA, data);
        contentValues.put(COLUMN_EVENT_TYPE, eventType);
        contentValues.put(COLUMN_POLICY, policy);
        return contentValues;
    }

}
