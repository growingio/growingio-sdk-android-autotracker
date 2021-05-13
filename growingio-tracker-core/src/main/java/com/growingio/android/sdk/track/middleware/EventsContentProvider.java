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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.growingio.android.sdk.track.middleware.EventsInfoTable.TABLE_EVENTS;

public class EventsContentProvider extends ContentProvider {

    //Uri info
    //authority

     public static String eventsInfoAuthority;
     public static Uri authorityUri;

    private static final UriMatcher MATCHER;
    private static final String TAG = "EventsContentProvider";
    //code
    private static final int EVENTS_INFO_CODE = 1;

    static {
        MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    }

    private final Object mLock = new Object();
    public EventsSQLiteOpenHelper dbHelper;

    @Override
    public boolean onCreate() {

        eventsInfoAuthority =  this.getContext().getPackageName() + ".EventsContentProvider";
        authorityUri = Uri.parse("content://" + eventsInfoAuthority);

        MATCHER.addURI(eventsInfoAuthority, TABLE_EVENTS, EVENTS_INFO_CODE);

        this.dbHelper = new EventsSQLiteOpenHelper(this.getContext(), "growing3.db");
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        try {
            synchronized (mLock) {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                switch (MATCHER.match(uri)) {
                    case EVENTS_INFO_CODE:

                        if ("rawQuery".equals(sortOrder)) {
                            return db.rawQuery(selection, null);
                        }
                        return db.query(TABLE_EVENTS, projection, selection, selectionArgs, null, null, sortOrder);
                    default:
                        throw new IllegalArgumentException("UnKnow Uri: " + uri.toString());
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "query error: " + e.getMessage());
            return null;
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (MATCHER.match(uri)) {

            case EVENTS_INFO_CODE:
                long rowId = db.insert(TABLE_EVENTS, null, values);
                Uri insertUri = ContentUris.withAppendedId(uri, rowId);
                this.getContext().getContentResolver().notifyChange(uri, null);
                return insertUri;

            default:
                throw new IllegalArgumentException("UnKnow Uri:" + uri.toString());
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = 0;
        switch (MATCHER.match(uri)) {

            case EVENTS_INFO_CODE:
                count = db.delete(TABLE_EVENTS, selection, selectionArgs);
                return count;

            default:
                throw new IllegalArgumentException("UnKnow Uri:" + uri.toString());
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = 0;
        switch (MATCHER.match(uri)) {

            case EVENTS_INFO_CODE:
                count = db.update(TABLE_EVENTS, values, selection, selectionArgs);
                return count;
            default:
                throw new IllegalArgumentException("UnKnow Uri:" + uri.toString());
        }
    }
}
