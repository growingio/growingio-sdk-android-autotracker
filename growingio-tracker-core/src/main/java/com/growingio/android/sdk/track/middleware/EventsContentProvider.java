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

    public EventsSQLiteOpenHelper dbHelper;
    private static final UriMatcher MATCHER;

    private static final String TAG = "EventsContentProvider";
    private final Object mLock = new Object();

    //Uri info
    //authority
    public static final String EVENTS_INFO_AUTHORITY = "com.growingio.android.sdk.track.middleware.EventsContentProvider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + EVENTS_INFO_AUTHORITY);
    //code
    private static final int EVENTS_INFO_CODE = 1;

    static {
        MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        MATCHER.addURI(EVENTS_INFO_AUTHORITY, TABLE_EVENTS, EVENTS_INFO_CODE);
    }

    @Override
    public boolean onCreate() {
        this.dbHelper = new EventsSQLiteOpenHelper(this.getContext(), "growing3.db");
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        try{
            synchronized (mLock) {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                switch (MATCHER.match(uri)){
                    case EVENTS_INFO_CODE:

                        if(sortOrder.equals("rawQuery")){
                            return db.rawQuery(selection, null);
                        }
                        return db.query(TABLE_EVENTS,projection, selection, selectionArgs,null, null, sortOrder);
                    default:
                        throw new IllegalArgumentException("UnKnow Uri: " + uri.toString());
                }
            }
        }catch (SQLException e){
            Log.e(TAG, "query error: "+ e.getMessage());
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
