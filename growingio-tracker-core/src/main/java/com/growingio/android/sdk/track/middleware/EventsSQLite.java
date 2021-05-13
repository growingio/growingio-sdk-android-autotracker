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

import android.content.Context;
import android.database.Cursor;

import com.growingio.android.sdk.track.log.Logger;

import java.io.IOException;
import java.util.List;

public class EventsSQLite {
    private static final String TAG = "EventsSQLite";

    private static final long EVENT_VALID_PERIOD_MILLS = 7 * 24 * 60 * 60_000;

    private final EventsManager eventsManager;

    EventsSQLite(Context context) {
        eventsManager = new EventsManager(context);
    }

    void insertEvent(GEvent gEvent) {
        try {
            eventsManager.insertEvents(Serializer.objectSerialize(gEvent), gEvent.getEventType(), gEvent.getSendPolicy());
        } catch (IOException e) {
            Logger.e(TAG, e, "insertEvent failed: %s", e.getMessage());
        }
    }

    void removeOverdueEvents() {
        long current = System.currentTimeMillis();
        long sevenDayAgo = current - EVENT_VALID_PERIOD_MILLS;
          eventsManager.removeOverdueEvents(sevenDayAgo);
    }

    long queryEvents(int policy, int limit, List<GEvent> events) {
        Cursor cursor = null;
        long lastId = -1;
        try {
            cursor = eventsManager.queryEvents(policy, limit);
            while (cursor.moveToNext()) {
                if (cursor.isLast()) {
                    lastId = cursor.getLong(cursor.getColumnIndex(EventsInfoTable.COLUMN_ID));
                }
                byte[] data = cursor.getBlob(cursor.getColumnIndex(EventsInfoTable.COLUMN_DATA));
                GEvent event = unpack(data);
                if (event != null) {
                    events.add(event);
                } else {
                    long delId = cursor.getLong(cursor.getColumnIndex(EventsInfoTable.COLUMN_ID));
                    eventsManager.removeEventById(delId);
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
        eventsManager.removeEvents(lastId, policy, eventType);
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
        eventsManager.removeAllEvents();
    }

}
