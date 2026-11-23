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
package com.growingio.android.database;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Truth;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static com.growingio.android.database.EventDataTable.COLUMN_FORCE_LOGIN_PENDING;
import static com.growingio.android.database.EventDataTable.TABLE_EVENTS;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class ForceLoginPendingColumnTest {

    private static final String DATABASE_NAME = "force-login-pending-column.db";

    // 老版本 SDK 创建的表：没有强制登录待替换列
    private static final String OLD_CREATE_TABLE_EVENTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_EVENTS + "(\n"
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                    + "_created INTEGER NOT NULL, \n"
                    + "_modified INTEGER NOT NULL, \n"
                    + "_data BLOB NOT NULL, \n"
                    + "_event_type TEXT NOT NULL, \n"
                    + "_policy INTEGER NOT NULL \n"
                    + ");";

    private final Application application = ApplicationProvider.getApplicationContext();

    @Before
    @After
    public void deleteDatabase() {
        application.deleteDatabase(DATABASE_NAME);
    }

    @Test
    public void oldTableGetsColumnWithoutDatabaseVersionChange() {
        SQLiteDatabase oldDb = application.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
        oldDb.execSQL(OLD_CREATE_TABLE_EVENTS);
        oldDb.setVersion(1);
        oldDb.insert(TABLE_EVENTS, null, EventDataTable.putValues(new byte[]{1}, "CUSTOM", 2));
        Truth.assertThat(columns(oldDb)).doesNotContain(COLUMN_FORCE_LOGIN_PENDING);
        oldDb.close();

        EventDataSQLiteOpenHelper helper = new EventDataSQLiteOpenHelper(application, DATABASE_NAME);
        SQLiteDatabase db = helper.getWritableDatabase();
        // 不升级数据库版本：onUpgrade 会删表，且老版本 SDK 无法打开更高版本的数据库
        Truth.assertThat(db.getVersion()).isEqualTo(1);
        Truth.assertThat(columns(db)).contains(COLUMN_FORCE_LOGIN_PENDING);
        // 老版本写入的事件按普通事件处理
        Truth.assertThat(pendingFlags(db)).containsExactly(0);

        db.insert(TABLE_EVENTS, null, EventDataTable.putValues(new byte[]{2}, "CUSTOM", 2, true));
        db.insert(TABLE_EVENTS, null, EventDataTable.putValues(new byte[]{3}, "CUSTOM", 2, false));
        Truth.assertThat(pendingFlags(db)).containsExactly(0, 1, 0).inOrder();
        helper.close();

        // 再次打开：列已存在，不重复补列，数据保留
        EventDataSQLiteOpenHelper reopened = new EventDataSQLiteOpenHelper(application, DATABASE_NAME);
        Truth.assertThat(pendingFlags(reopened.getWritableDatabase())).containsExactly(0, 1, 0).inOrder();
        reopened.close();
    }

    @Test
    public void newTableContainsColumn() {
        EventDataSQLiteOpenHelper helper = new EventDataSQLiteOpenHelper(application, DATABASE_NAME);
        SQLiteDatabase db = helper.getWritableDatabase();
        Truth.assertThat(db.getVersion()).isEqualTo(1);
        Truth.assertThat(columns(db)).contains(COLUMN_FORCE_LOGIN_PENDING);
        db.insert(TABLE_EVENTS, null, EventDataTable.putValues(new byte[]{1}, "CUSTOM", 2, true));
        Truth.assertThat(pendingFlags(db)).containsExactly(1);
        helper.close();
    }

    @Test
    public void pendingEventIsKeptWhenColumnIsMissing() {
        // 模拟补列失败：表中没有待替换列
        SQLiteDatabase db = application.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
        db.execSQL(OLD_CREATE_TABLE_EVENTS);

        long pendingRowId = EventDataTable.insertEvent(db, EventDataTable.putValues(new byte[]{1}, "CUSTOM", 2, true));
        long normalRowId = EventDataTable.insertEvent(db, EventDataTable.putValues(new byte[]{2}, "CUSTOM", 2, false));

        // 待替换事件去掉标志后按普通事件写入，不丢失
        Truth.assertThat(pendingRowId).isGreaterThan(0L);
        Truth.assertThat(normalRowId).isGreaterThan(0L);
        try (Cursor cursor = db.rawQuery("SELECT _data FROM " + TABLE_EVENTS + " ORDER BY _id ASC", null)) {
            Truth.assertThat(cursor.getCount()).isEqualTo(2);
        }
        db.close();
    }

    @Test
    public void pendingEventKeepsFlagWhenColumnExists() {
        EventDataSQLiteOpenHelper helper = new EventDataSQLiteOpenHelper(application, DATABASE_NAME);
        SQLiteDatabase db = helper.getWritableDatabase();
        Truth.assertThat(EventDataTable.insertEvent(db, EventDataTable.putValues(new byte[]{1}, "CUSTOM", 2, true))).isGreaterThan(0L);
        Truth.assertThat(pendingFlags(db)).containsExactly(1);
        helper.close();
    }

    private List<String> columns(SQLiteDatabase db) {
        List<String> result = new ArrayList<>();
        try (Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_EVENTS + ")", null)) {
            int nameIndex = cursor.getColumnIndex("name");
            while (cursor.moveToNext()) {
                result.add(cursor.getString(nameIndex));
            }
        }
        return result;
    }

    private List<Integer> pendingFlags(SQLiteDatabase db) {
        List<Integer> result = new ArrayList<>();
        try (Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_FORCE_LOGIN_PENDING + " FROM " + TABLE_EVENTS + " ORDER BY _id ASC", null)) {
            while (cursor.moveToNext()) {
                result.add(cursor.getInt(0));
            }
        }
        return result;
    }
}
