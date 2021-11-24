/*
 *   Copyright (C) 2020 Beijing Yishu Technology Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.growingio.database;

import android.net.Uri;
import android.text.TextUtils;

import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.middleware.EventDatabase;
import com.growingio.android.sdk.track.middleware.EventDbResult;
import com.growingio.android.sdk.track.middleware.GEvent;
import com.growingio.android.sdk.track.modelloader.DataFetcher;

import static com.growingio.android.sdk.track.middleware.GEvent.SEND_POLICY_INSTANT;
import static com.growingio.android.sdk.track.middleware.GEvent.SEND_POLICY_WIFI;

/**
 * <p>
 * database run in work thread default.
 *
 * @author cpacm 2021/11/22
 */
public class DatabaseDataFetcher implements DataFetcher<EventDbResult> {

    private final EventDatabase eventDatabase;
    private final EventDataManager dataManager;

    public DatabaseDataFetcher(EventDataManager dataManager, EventDatabase eventDatabase) {
        this.eventDatabase = eventDatabase;
        this.dataManager = dataManager;
    }

    @Override
    public void loadData(DataCallback<? super EventDbResult> callback) {
        try {
            EventDbResult dbResult = executeDatabase(eventDatabase);
            if (dbResult.isSuccess()) {
                callback.onDataReady(dbResult);
            } else {
                callback.onLoadFailed(new IllegalArgumentException("database operation failed"));
            }
        } catch (IllegalArgumentException e) {
            Logger.e("DatabaseDataFetcher", e);
            callback.onLoadFailed(e);
        }
    }

    @Override
    public EventDbResult executeData() {
        try {
            return executeDatabase(eventDatabase);
        } catch (IllegalArgumentException e) {
            EventDbResult dbResult = new EventDbResult(false);
            Logger.e("DatabaseDataFetcher", e);
            return dbResult;
        }
    }


    private EventDbResult executeDatabase(EventDatabase database) throws IllegalArgumentException {
        EventDbResult dbResult = new EventDbResult();
        if (database.getDbOp() == EventDatabase.DATABASE_OP_INSERT) {
            assertCondition(database.getEvents() != null && !database.getEvents().isEmpty(), "leak necessary event");
            int count = dataManager.insertEvents(database.getEvents());
            dbResult.setSum(count);
            dbResult.setSuccess(count == database.getEvents().size());
            return dbResult;
        } else if (database.getDbOp() == EventDatabase.DATABASE_OP_OUTDATED) {
            int sum = dataManager.removeOverdueEvents();
            dbResult.setSum(sum);
            dbResult.setSuccess(sum >= 0);
            return dbResult;
        } else if (database.getDbOp() == EventDatabase.DATABASE_OP_QUERY) {
            assertCondition(database.getLimit() > 0
                            && database.getPolicy() >= SEND_POLICY_INSTANT && database.getPolicy() <= SEND_POLICY_WIFI,
                    "leak necessary param");
            dataManager.queryEvents(database.getPolicy(), database.getLimit(), dbResult);
            return dbResult;
        } else if (database.getDbOp() == EventDatabase.DATABASE_OP_DELETE) {
            assertCondition(database.getLastId() > 0
                    && database.getPolicy() >= SEND_POLICY_INSTANT && database.getPolicy() <= SEND_POLICY_WIFI
                    && !TextUtils.isEmpty(database.getEventType()), "leak necessary param");
            int sum = dataManager.removeEvents(database.getLastId(), database.getPolicy(), database.getEventType());
            dbResult.setSum(sum);
            dbResult.setSuccess(sum >= 0);
            return dbResult;
        } else if (database.getDbOp() == EventDatabase.DATABASE_OP_QUERY_DELETE) {
            assertCondition(database.getLimit() > 0
                            && database.getPolicy() >= SEND_POLICY_INSTANT && database.getPolicy() <= SEND_POLICY_WIFI,
                    "leak necessary param");
            dataManager.queryEventsAndDelete(database.getPolicy(), database.getLimit(), dbResult);
            return dbResult;
        } else if (database.getDbOp() == EventDatabase.DATABASE_OP_CLEAR) {
            dataManager.removeAllEvents();
            dbResult.setSuccess(true);
            return dbResult;
        }
        dbResult.setSuccess(false);
        return dbResult;
    }

    private void assertCondition(boolean condition, String msg) throws IllegalArgumentException {
        if (!condition) throw new IllegalArgumentException(msg);
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void cancel() {

    }

    @Override
    public Class<EventDbResult> getDataClass() {
        return EventDbResult.class;
    }
}
