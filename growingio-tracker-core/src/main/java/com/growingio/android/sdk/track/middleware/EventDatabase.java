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
package com.growingio.android.sdk.track.middleware;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *
 * @author cpacm 2021/11/22
 */
public class EventDatabase {
    public static final int DATABASE_OP_INSERT = 0;
    public static final int DATABASE_OP_QUERY = 1;
    public static final int DATABASE_OP_UPDATE = 2;
    public static final int DATABASE_OP_DELETE = 3;
    public static final int DATABASE_OP_OUTDATED = 4;
    public static final int DATABASE_OP_QUERY_DELETE = 5;
    public static final int DATABASE_OP_CLEAR = 6;

    private int dbOp;
    private List<GEvent> events;
    private int policy;
    private int limit;
    private long lastId;
    private String eventType;

    public int getDbOp() {
        return dbOp;
    }

    public List<GEvent> getEvents() {
        return events;
    }

    public int getPolicy() {
        return policy;
    }

    public int getLimit() {
        return limit;
    }

    public long getLastId() {
        return lastId;
    }

    public String getEventType() {
        return eventType;
    }

    public static EventDatabase insert(GEvent event) {
        EventDatabase ed = new EventDatabase();
        ed.dbOp = DATABASE_OP_INSERT;
        ed.events = new ArrayList<>();
        ed.events.add(event);
        return ed;
    }

    public static EventDatabase inserts(List<GEvent> events) {
        EventDatabase ed = new EventDatabase();
        ed.dbOp = DATABASE_OP_INSERT;
        ed.events = new ArrayList<>();
        ed.events.addAll(events);
        return ed;
    }

    public static EventDatabase outDated() {
        EventDatabase ed = new EventDatabase();
        ed.dbOp = DATABASE_OP_OUTDATED;
        ed.limit = 7;
        return ed;
    }

    public static EventDatabase outDated(int day) {
        EventDatabase ed = new EventDatabase();
        ed.dbOp = DATABASE_OP_OUTDATED;
        ed.limit = day;
        return ed;
    }

    public static EventDatabase query(int policy, int limit) {
        EventDatabase ed = new EventDatabase();
        ed.dbOp = DATABASE_OP_QUERY;
        ed.policy = policy;
        ed.limit = limit;
        return ed;
    }

    public static EventDatabase queryAndDelete(int policy, int limit) {
        EventDatabase ed = new EventDatabase();
        ed.dbOp = DATABASE_OP_QUERY_DELETE;
        ed.policy = policy;
        ed.limit = limit;
        return ed;
    }

    public static EventDatabase delete(long lastId, int policy, String eventType) {
        EventDatabase ed = new EventDatabase();
        ed.dbOp = DATABASE_OP_DELETE;
        ed.policy = policy;
        ed.eventType = eventType;
        ed.lastId = lastId;
        return ed;
    }

    public static EventDatabase update(long lastId, String eventType) {
        EventDatabase ed = new EventDatabase();
        ed.dbOp = DATABASE_OP_UPDATE;
        ed.eventType = eventType;
        ed.lastId = lastId;
        return ed;
    }

    public static EventDatabase clear() {
        EventDatabase ed = new EventDatabase();
        ed.dbOp = DATABASE_OP_CLEAR;
        return ed;
    }
}
