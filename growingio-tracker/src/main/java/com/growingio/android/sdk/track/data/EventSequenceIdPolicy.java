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

package com.growingio.android.sdk.track.data;

import android.content.Context;

import com.growingio.android.sdk.track.interfaces.TrackThread;
import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.utils.ProcessLock;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 参考文档： <a href='https://codes.growingio.com/w/prd/mobile/sdk_data/'>https://codes.growingio.com/w/prd/mobile/sdk_data/</a><br/>
 * 每次调用都会自动累加记录对应的 SequenceId，尽量合并多次调用，不能重复调用。
 */

class EventSequenceIdPolicy {
    private static final String TAG = "EventSequenceIdPolicy";

    private static final String TYPE_GLOBAL = "TYPE_GLOBAL";

    private static final String FILE_NAME = "growingio_event_sequence_id";
    private static final String FILE_LOCK = FILE_NAME + ".lock";

    private final ProcessLock mProcessLock;
    private final Context mContext;

    EventSequenceIdPolicy(Context context) {
        mContext = context;
        mProcessLock = new ProcessLock(mContext, FILE_LOCK);
    }

    @TrackThread
    EventSequenceId getAndAdd(String eventType, int size) {
        try {
            mProcessLock.acquire(1000);
            return doGetAndAdd(eventType, size);
        } finally {
            mProcessLock.release();
        }
    }

    @TrackThread
    EventSequenceId getAndIncrement(String eventType) {
        return getAndAdd(eventType, 1);
    }

    private EventSequenceId doGetAndAdd(String eventType, int size) {
        ObjectInputStream inputStream = null;
        EventSequenceIdMap idMap = null;
        try {
            inputStream = new ObjectInputStream(mContext.openFileInput(FILE_NAME));
            try {
                idMap = (EventSequenceIdMap) inputStream.readObject();
            } catch (ClassNotFoundException e) {
                Logger.e(TAG, e);
            }
        } catch (Exception ignored) {
        } finally {
            if (idMap == null) {
                idMap = new EventSequenceIdMap();
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        long eventTypeId = idMap.getSequenceId(eventType);
        long globalId = idMap.getSequenceId(TYPE_GLOBAL);

        idMap.setSequenceId(eventType, eventTypeId + size)
                .setSequenceId(TYPE_GLOBAL, globalId + size);
        ObjectOutputStream outputStream = null;
        try {
            outputStream = new ObjectOutputStream(mContext.openFileOutput(FILE_NAME, Context.MODE_PRIVATE));
            outputStream.writeObject(idMap);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new EventSequenceId(globalId, eventTypeId);
    }
}
