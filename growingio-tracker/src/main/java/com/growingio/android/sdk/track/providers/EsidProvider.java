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

package com.growingio.android.sdk.track.providers;

import android.content.Context;

import com.growingio.android.sdk.track.GConfig;
import com.growingio.android.sdk.track.events.EventType;
import com.growingio.android.sdk.track.interfaces.GMainThread;
import com.growingio.android.sdk.track.models.EsidProperty;
import com.growingio.android.sdk.track.models.EventSID;
import com.growingio.android.sdk.track.utils.GIOProviders;
import com.growingio.android.sdk.track.utils.ProcessLock;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 向SDK提供esid与gsid计数功能
 */
public interface EsidProvider {

    /**
     * 参考文档： <a href='https://codes.growingio.com/w/prd/mobile/sdk_data/'>https://codes.growingio.com/w/prd/mobile/sdk_data/</a><br/>
     * 每次调用都会自动累加记录对应的sid，尽量合并多次调用，不能重复调用。
     *
     * @param eventType 事件类型
     * @param size      事件包含具体条数
     * @return first:全局事件累计个数，second:和当前类型累计个数
     */
    @GMainThread
    EsidProperty getAndAddEsid(EventType eventType, int size);

    class EsidPolicy implements EsidProvider {

        private static final String ESID_TYPE_ALL = "all";
        private ProcessLock mEsidFileLock;
        private Context mContext;

        public EsidPolicy(Context context) {
            mContext = context;
            mEsidFileLock = new ProcessLock(mContext, "growingio.lock");
        }

        public static EsidProvider get(final Context context) {
            return GIOProviders.provider(EsidProvider.class, new GIOProviders.DefaultCallback<EsidProvider>() {
                @Override
                public EsidProvider value() {
                    return new EsidPolicy(context);
                }
            });
        }

        @Override
        public EsidProperty getAndAddEsid(EventType eventType, int size) {
            try {
                mEsidFileLock.acquire(1000);
                return getAndAddEsidLocked(eventType, size);
            } finally {
                mEsidFileLock.release();
            }
        }

        private EsidProperty getAndAddEsidLocked(EventType eventType, int size) {
            ObjectInputStream inputStream = null;
            EventSID sid = null;
            try {
                inputStream = new ObjectInputStream(mContext.openFileInput(GConfig.PREF_ECSID_FILE_NAME));
                try {
                    sid = (EventSID) inputStream.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } catch (Exception ignored) {
            } finally {
                if (sid == null) {
                    sid = new EventSID();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            int esid = sid.getSid(eventType.toString());
            int gesid = sid.getSid(ESID_TYPE_ALL);
            sid.setSid(eventType.toString(), esid + size).setSid(ESID_TYPE_ALL, gesid + size);
            ObjectOutputStream outputStream = null;
            try {
                outputStream = new ObjectOutputStream(mContext.openFileOutput(GConfig.PREF_ECSID_FILE_NAME, Context.MODE_PRIVATE));
                outputStream.writeObject(sid);
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
            EsidProperty esidProperty = new EsidProperty();
            esidProperty.setEsid(esid);
            esidProperty.setGsid(gesid);
            return esidProperty;
        }
    }
}
