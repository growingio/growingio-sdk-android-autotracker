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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.growingio.android.sdk.track.log.Logger;

public class EventDataSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = "EventDataSQLiteOpenHelper";
    // 保持为 1：onUpgrade 会删表重建，且老版本 SDK 无法打开更高版本的数据库；新增列在 onOpen 中补充
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "growingio.db";


    EventDataSQLiteOpenHelper(Context context, String databaseName) {
        super(context, databaseName, null, DATABASE_VERSION);
    }

    EventDataSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(EventDataTable.CREATE_TABLE_EVENTS);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (db.isReadOnly()) {
            return;
        }
        try {
            EventDataTable.ensureForceLoginPendingColumn(db);
        } catch (Exception e) {
            Logger.e(TAG, e, "failed to add force login pending column");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(EventDataTable.DROP_TABLE_EVENTS);
        onCreate(db);
    }
}
