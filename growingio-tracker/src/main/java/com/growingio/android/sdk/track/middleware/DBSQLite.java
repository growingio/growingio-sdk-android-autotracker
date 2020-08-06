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
import android.database.sqlite.SQLiteStatement;

import com.growingio.android.sdk.track.utils.LogUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 所有跟数据库有交互的代码
 */
public class DBSQLite {
    private static final long EVENT_VALID_PERIOD_MILLS = 7 * 24 * 60 * 60_000;

    static final int UPLOAD_FAILED = -1;
    static final int UPLOAD_SUCCESS = 1;
    static final int UPLOAD_NO_MSG = 0;
    private static final String TAG = "GIO.DBSQLite";
    final DBSQLiteOpenHelper mDbHelper;
    private final IEventSender mSender;

    public DBSQLite(Context context, IEventSender sender) {
        mDbHelper = new DBSQLiteOpenHelper(context, "growing3.db");
        mSender = sender;
    }

    public void insertEvent(GEvent gEvent) {
        try {
            mDbHelper.insertEvents(Serializer.objectSerialize(gEvent), gEvent.getTag(), gEvent.getSendPolicy());
        } catch (IOException e) {
            LogUtil.e(TAG, e, "insertEvent failed: %s", e.getMessage());
        }
    }

    public void updateStatics(int generateNum, int sentSuccessNum,
                              int bytesSent, int failedNum,
                              int invalidNum) {
        try {
            if (generateNum == 0 && sentSuccessNum == 0 && bytesSent == 0 && failedNum == 0 && invalidNum == 0) {
                return;
            }
            mDbHelper.updateStatics(generateNum, sentSuccessNum, bytesSent, failedNum, invalidNum);
        } catch (Exception e) {
            LogUtil.e(TAG, e, "updateStatics failed: %s", e.getMessage());
        }
    }

    public void cleanOldLog() {
        long current = System.currentTimeMillis();
        long sevenDayAgo = current - EVENT_VALID_PERIOD_MILLS;
        mDbHelper.removeOldLog(sevenDayAgo);
    }

    public void uploadStaticEvent() {
        Cursor cursor = null;
        List<SDKStaticsEvent> staticsEvents = new ArrayList<>();
        try {
            cursor = mDbHelper.queryStatics(20);
            while (cursor.moveToNext()) {
                SDKStaticsEvent event = new SDKStaticsEvent();
                event.setId(cursor.getLong(cursor.getColumnIndex(DBSQLiteOpenHelper.COLUMN_ID)));
                event.setSavedNum(cursor.getInt(cursor.getColumnIndex(DBSQLiteOpenHelper.COLUMN_NUM_NEW)));
                event.setBytesSent(cursor.getInt(cursor.getColumnIndex(DBSQLiteOpenHelper.COLUMN_NUM_BYTES)));
                event.setFailedNum(cursor.getInt(cursor.getColumnIndex(DBSQLiteOpenHelper.COLUMN_FAILED_GENERATE)));
                event.setHourTime(cursor.getString(cursor.getColumnIndex(DBSQLiteOpenHelper.COLUMN_DATETIME)));
                event.setInvalidNum(cursor.getInt(cursor.getColumnIndex(DBSQLiteOpenHelper.COLUMN_NUM_INVALID)));
                event.setSentSuccessNum(cursor.getInt(cursor.getColumnIndex(DBSQLiteOpenHelper.COLUMN_NUM_SENT)));
                staticsEvents.add(event);
            }
        } catch (Exception e) {
            LogUtil.e(TAG, e, e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (staticsEvents.size() > 0) {
            if (SDKStaticsEvent.sendEvents(staticsEvents)) {
                LogUtil.d(TAG, "uploadStaticEvent completely, and delete record");
                mDbHelper.removeStatics(staticsEvents.get(staticsEvents.size() - 1).getId());
            }
        }
    }

    /**
     * 上传事件
     *
     * @param policy 对应的发送策略
     * @param num    一次发送多少条
     * @return true -- 表示发送成功, 还有更多消息, false -- 发送失败,需调度下次发送, null 表示 -- 没有更多消息
     */
    public int uploadEvent(int policy, int num) {
        Cursor cursor = null;
        long lastId = -1;
        String tag = null;
        boolean hasException = false;
        List<GEvent> gEvents = new ArrayList<>();
        try {
            cursor = mDbHelper.queryEvents(policy, num);

            while (cursor.moveToNext()) {
                if (cursor.isLast()) {
                    lastId = cursor.getLong(cursor.getColumnIndex(DBSQLiteOpenHelper.COLUMN_ID));
                    tag = cursor.getString(cursor.getColumnIndex(DBSQLiteOpenHelper.COLUMN_TAG));
                }
                byte[] data = cursor.getBlob(cursor.getColumnIndex(DBSQLiteOpenHelper.COLUMN_DATA));
                GEvent event = unpack(data);
                if (event != null) {
                    gEvents.add(event);
                } else {
                    long delId = cursor.getLong(cursor.getColumnIndex(DBSQLiteOpenHelper.COLUMN_ID));
                    updateStatics(0, 0, 0, 0, 1);
                    mDbHelper.removeEventById(delId);
                }
            }
            if (lastId != -1) {
                if (!mSender.send(gEvents)) {
                    // 上传失败, 下一个发送再尝试周期
                    LogUtil.d(TAG, "send failed, and will retry later");
                    return UPLOAD_FAILED;
                }
                updateStatics(0, gEvents.size(), 0, 0, 0);
            }
        } catch (Throwable t) {
            LogUtil.e(TAG, t, t.getMessage());
            hasException = true;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (lastId != -1) {
            mDbHelper.removeEvents(lastId, policy, tag);
            if (hasException) {
                updateStatics(0, 0, 0, 0, gEvents.size());
            }
            return UPLOAD_SUCCESS;
        } else {
            // 没有消息
            return UPLOAD_NO_MSG;
        }
    }

    private GEvent unpack(byte[] data) {
        try {
            return Serializer.objectDeserialization(data);
        } catch (IOException e) {
            LogUtil.e(TAG, e, e.getMessage());
        } catch (ClassNotFoundException e) {
            LogUtil.e(TAG, e, e.getMessage());
        }
        return null;
    }

    static class DBSQLiteOpenHelper extends SQLiteOpenHelper {
        private static final int DATABASE_VERSION = 1;

        private static final String TABLE_EVENTS = "events";
        private static final String COLUMN_ID = "_id";
        private static final String COLUMN_CREATE_TIME = "_created";
        private static final String COLUMN_LAST_MODIFIED = "_modified";
        private static final String COLUMN_DATA = "_data";
        private static final String COLUMN_TAG = "_tag";
        private static final String COLUMN_POLICY = "_policy";

        private static final String TABLE_STATISTICS = "statistics";
        private static final String COLUMN_DATETIME = "time";
        private static final String COLUMN_NUM_NEW = "new_num";
        private static final String COLUMN_NUM_SENT = "sent_num";
        private static final String COLUMN_NUM_BYTES = "sent_bytes";
        private static final String COLUMN_FAILED_GENERATE = "failed";
        private static final String COLUMN_NUM_INVALID = "invalid";

        private static final String CREATE_TABLE_EVENTS =
                "CREATE TABLE IF NOT EXISTS " + TABLE_EVENTS + "(\n"
                        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                        + COLUMN_CREATE_TIME + " INTEGER NOT NULL, \n"
                        + COLUMN_LAST_MODIFIED + " INTEGER NOT NULL, \n"
                        + COLUMN_DATA + " BLOB NOT NULL, \n"
                        + COLUMN_TAG + " TEXT NOT NULL, \n"
                        + COLUMN_POLICY + " INTEGER NOT NULL \n"
                        + ");";

        private static final String CREATE_TABLE_STATISTICS =
                "CREATE TABLE IF NOT EXISTS " + TABLE_STATISTICS + "(\n"
                        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                        + COLUMN_DATETIME + " STRING NOT NULL UNIQUE, \n"
                        + COLUMN_CREATE_TIME + " INTEGER, \n"
                        + COLUMN_NUM_NEW + " INTEGER DEFAULT 0, \n"
                        + COLUMN_NUM_SENT + " INTEGER DEFAULT 0, \n"
                        + COLUMN_NUM_BYTES + " INTEGER DEFAULT 0, \n"
                        + COLUMN_FAILED_GENERATE + " INTEGER DEFAULT 0, \n"
                        + COLUMN_NUM_INVALID + " INTEGER DEFAULT 0 \n"
                        + ");";

        private static final String DROP_TABLE_EVENTS = "DROP TABLE IF EXISTS " + TABLE_EVENTS + ";";
        private static final String DROP_TABLE_STATISTICS = "DROP TABLE IF EXISTS " + TABLE_STATISTICS + ";";

        DBSQLiteOpenHelper(Context context, String databaseName) {
            super(context, databaseName, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_EVENTS);
            db.execSQL(CREATE_TABLE_STATISTICS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DROP_TABLE_EVENTS);
            db.execSQL(DROP_TABLE_STATISTICS);
            onCreate(db);
        }

        /**
         * 向数据库插入一条消息
         *
         * @param data   数据
         * @param policy 发送策略
         */
        public void insertEvents(byte[] data, String tag, int policy) {
            long current = System.currentTimeMillis();
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_CREATE_TIME, current);
            contentValues.put(COLUMN_LAST_MODIFIED, current);
            contentValues.put(COLUMN_DATA, data);
            contentValues.put(COLUMN_TAG, tag);
            contentValues.put(COLUMN_POLICY, policy);
            getWritableDatabase().insert(TABLE_EVENTS, null, contentValues);
        }

        @SuppressLint("Recycle")
        public Cursor queryEvents(int policy, int num) {
            String subSelect = "SELECT " + COLUMN_TAG
                    + " FROM " + TABLE_EVENTS + " WHERE " + COLUMN_POLICY + "=" + policy
                    + " LIMIT 1";
            String sql = "SELECT " + COLUMN_ID + ", " + COLUMN_DATA + ", "
                    + COLUMN_TAG
                    + " FROM " + TABLE_EVENTS
                    + " WHERE " + COLUMN_TAG + "=(" + subSelect + ") AND " + COLUMN_POLICY + "=" + policy
                    + " LIMIT " + num + ";";
            return getReadableDatabase().rawQuery(sql, null);
        }

        private Cursor queryStatics(int maxNum) {

            return getReadableDatabase().query(TABLE_STATISTICS,
                    new String[]{
                            COLUMN_ID,
                            COLUMN_NUM_NEW,
                            COLUMN_NUM_SENT,
                            COLUMN_NUM_BYTES,
                            COLUMN_NUM_INVALID,
                            COLUMN_DATETIME,
                            COLUMN_FAILED_GENERATE},
                    COLUMN_DATETIME + "!=?", new String[]{hourStr()}, null, null, null,
                    String.valueOf(maxNum));
        }

        public void removeEvents(long id, int policy, String tag) {
            getWritableDatabase().delete(TABLE_EVENTS,
                    COLUMN_ID + "<=? AND " + COLUMN_TAG + "=? AND " + COLUMN_POLICY + "=?",
                    new String[]{String.valueOf(id), tag, String.valueOf(policy)});
        }

        public void removeEventById(long id) {
            getWritableDatabase().delete(TABLE_EVENTS,
                    COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        }

        public void removeStatics(long id) {
            getWritableDatabase().delete(TABLE_STATISTICS,
                    COLUMN_ID + "<=" + id, null);
        }

        public void removeOldLog(long time) {
            int deleteNum = getWritableDatabase().delete(TABLE_EVENTS,
                    COLUMN_CREATE_TIME + "<=" + time, null);
            LogUtil.e(TAG, "remove OldLog: deleteNum: %d", deleteNum);
            if (deleteNum > 0) {
                updateStatics(0, 0, 0, 0, deleteNum);
            }

            deleteNum = getWritableDatabase().delete(TABLE_STATISTICS, COLUMN_CREATE_TIME + "<=" + time, null);
            LogUtil.e(TAG, "removeOldLog, and remove statistics num: %d", deleteNum);
        }

        // 清库
        public void delAllMsg() {
            getWritableDatabase().delete(TABLE_EVENTS, null, null);
            getWritableDatabase().delete(TABLE_STATISTICS, null, null);
        }

        @SuppressLint("SimpleDateFormat")
        private String hourStr() {
            return new SimpleDateFormat("yyyyMMddHH").format(new Date());
        }

        public void updateStatics(int generateNum, int sentSuccessNum,
                                  int bytesSent, int failedNum,
                                  int invalidNum) {
            String hourStr = hourStr();
            StringBuilder updateSQLBuilder = new StringBuilder();
            updateSQLBuilder.append("UPDATE ").append(TABLE_STATISTICS).append(" SET ");

            checkAndAppend(updateSQLBuilder, COLUMN_NUM_NEW, generateNum);
            checkAndAppend(updateSQLBuilder, COLUMN_NUM_SENT, sentSuccessNum);
            checkAndAppend(updateSQLBuilder, COLUMN_NUM_BYTES, bytesSent);
            checkAndAppend(updateSQLBuilder, COLUMN_FAILED_GENERATE, failedNum);
            checkAndAppend(updateSQLBuilder, COLUMN_NUM_INVALID, invalidNum);

            updateSQLBuilder.setLength(updateSQLBuilder.length() - 2);
            updateSQLBuilder.append(" WHERE ")
                    .append('`').append(COLUMN_DATETIME).append('`').append("=\"").append(hourStr)
                    .append("\";");
            SQLiteStatement statement = null;
            try {
                statement = getWritableDatabase().compileStatement(updateSQLBuilder.toString());
                if (statement.executeUpdateDelete() > 0) {
                    LogUtil.d(TAG, "updateStatics successfully: g=%d, s=%d, f=%d, i=%d",
                            generateNum, sentSuccessNum, failedNum, invalidNum);
                    return;
                }
            } finally {
                if (statement != null) {
                    statement.close();
                    statement = null;
                }
            }

            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_DATETIME, hourStr);
            contentValues.put(COLUMN_CREATE_TIME, System.currentTimeMillis());
            getWritableDatabase().insert(TABLE_STATISTICS, null, contentValues);
            try {
                statement = getWritableDatabase().compileStatement(updateSQLBuilder.toString());
                if (statement.executeUpdateDelete() > 0) {
                    LogUtil.d(TAG, "reupdate statics successfully");
                } else {
                    LogUtil.e(TAG, "reupdate statics failedly");
                }
            } finally {
                if (statement != null) {
                    statement.close();
                    statement = null;
                }
            }
        }

        private void checkAndAppend(StringBuilder builder, String columnName, int deltaValue) {
            if (deltaValue > 0) {
                builder.append('`').append(columnName).append('`')
                        .append("=")
                        .append('`').append(columnName).append('`').append(" + ").append(deltaValue)
                        .append(", ");
            }
        }
    }
}
