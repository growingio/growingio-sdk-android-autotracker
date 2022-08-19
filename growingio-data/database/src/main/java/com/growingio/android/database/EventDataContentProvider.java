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

import static com.growingio.android.database.EventDataTable.TABLE_EVENTS;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class EventDataContentProvider extends ContentProvider {

    public static final String CONTENT_PROVIDER_NAME = "EventDataContentProvider";

    private static final UriMatcher MATCHER;
    private static final String TAG = "EventContentProvider";
    //code
    private static final int EVENT_DATA_CODE = 1;


    static {
        MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    }

    private final Object mLock = new Object();
    public EventDataSQLiteOpenHelper dbHelper;

    @Override
    public boolean onCreate() {
        String eventsInfoAuthority = this.getContext().getPackageName() + "." + CONTENT_PROVIDER_NAME;
        MATCHER.addURI(eventsInfoAuthority, TABLE_EVENTS, EVENT_DATA_CODE);
        this.dbHelper = new EventDataSQLiteOpenHelper(this.getContext());
        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        try {
            synchronized (mLock) {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                if (MATCHER.match(uri) == EVENT_DATA_CODE) {
                    if ("rawQuery".equals(sortOrder)) {
                        return db.rawQuery(selection, null);
                    }
                    return db.query(TABLE_EVENTS, projection, selection, selectionArgs, null, null, sortOrder);
                }
                throw new IllegalArgumentException("UnKnow Uri: " + uri.toString());
            }
        } catch (SQLException e) {
            Log.e(TAG, "query error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (MATCHER.match(uri) == EVENT_DATA_CODE) {
            long rowId = db.insert(TABLE_EVENTS, null, values);
            Uri insertUri = ContentUris.withAppendedId(uri, rowId);
            this.getContext().getContentResolver().notifyChange(uri, null);
            return insertUri;
        }
        throw new IllegalArgumentException("UnKnow Uri:" + uri.toString());
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        if (MATCHER.match(uri) == EVENT_DATA_CODE) {
            count = db.delete(TABLE_EVENTS, selection, selectionArgs);
            return count;
        }
        throw new IllegalArgumentException("UnKnow Uri:" + uri.toString());
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        if (MATCHER.match(uri) == EVENT_DATA_CODE) {
            count = db.update(TABLE_EVENTS, values, selection, selectionArgs);
            return count;
        }
        throw new IllegalArgumentException("UnKnow Uri:" + uri.toString());
    }

    @Override
    public void shutdown() {
        super.shutdown();
        dbHelper.close();
    }
}
