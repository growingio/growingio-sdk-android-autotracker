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
import android.os.Bundle;

import com.growingio.android.sdk.track.log.Logger;

/**
 * 用于将事件入库
 * 每个进程应该仅有一个EventSaver
 */
public class EventSaver {
    private static final String TAG = "GIO.EventSaver";

    private final Context mContext;
    private final DBSQLite mDbSQLite;

    public EventSaver(Context context, IEventSender sender) {
        this.mContext = context;
        mDbSQLite = new DBSQLite(context, sender);
    }

    public synchronized boolean saveEvent(GEvent gEvent) {
        try {
            return saveDBEventLocked(gEvent);
        } catch (Exception e) {
            Logger.e(TAG, e, "unknown exception, and reject to write DB:");
            return false;
        }
    }

    public synchronized void close() {
        mDbSQLite.mDbHelper.getReadableDatabase().close();
    }

    public void cleanAllMsg() {
        sendEvent(GIOSenderService.ACTION_DEL_ALL, false);
    }

    public void toggleSend() {
        sendEvent(GIOSenderService.ACTION_SEND_WITH_LIMIT, false);
    }

    protected void sendEvent(int action, boolean instant) {
        Bundle bundle = new Bundle();
        bundle.putInt(GIOSenderService.ARG_COMMAND, action);
        bundle.putBoolean("instant", instant);
        GIOSenderService.startService(this.mContext, bundle);
    }

    private boolean saveDBEventLocked(GEvent gEvent) {
        mDbSQLite.insertEvent(gEvent);
        sendEvent(GIOSenderService.ACTION_NEW_EVENT, gEvent.getSendPolicy() == GEvent.SEND_POLICY_INSTANT);
        return true;
    }

}
