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
package com.growingio.android.sdk.autotrack;

import android.content.Context;
import android.content.SharedPreferences;

import com.growingio.android.sdk.TrackerContext;
import com.growingio.android.sdk.track.providers.PersistentDataProvider;
import com.growingio.android.sdk.track.providers.TrackerLifecycleProvider;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class UpgradeProvider implements TrackerLifecycleProvider {
    private static final String KEEP_ID = "KEEP_ID";
    private PersistentDataProvider persistentDataProvider;

    @Override
    public void setup(TrackerContext context) {
        this.persistentDataProvider = context.getProvider(PersistentDataProvider.class);
        if (this.persistentDataProvider.getString(KEEP_ID, null) == null) {
            upgradeDeviceId(context);
            upgradeUserId(context);
            this.persistentDataProvider.putString(KEEP_ID, "true");
        }
    }

    @Override
    public void shutdown() {

    }

    private void upgradeDeviceId(Context context) {
        String v2SpFileName = "growing_persist_data";
        SharedPreferences sharedPreferences = context.getSharedPreferences(v2SpFileName, Context.MODE_PRIVATE);
        String deviceId = sharedPreferences.getString("device_id", null);
        if (deviceId != null) {
            this.persistentDataProvider.setDeviceId(deviceId);
        }
    }

    private void upgradeUserId(Context context) {
        File v2ShareFile = new File(context.getFilesDir(), ".gio.dir/gio.ipc.1");
        if (v2ShareFile.exists()) {
            FileChannel fileChannel = null;
            try {
                RandomAccessFile randomAccessFile = new RandomAccessFile(v2ShareFile, "rw");
                fileChannel = randomAccessFile.getChannel();
                // 44 = 2(magic_num) + 2(process_num) + 40(process_id)
                // 44 = 4 * 11(modCount)
                // 4 = split   44 + 44 + 4 为variableBaseAddress变量存储区地址
                // 2 = sessionId len
                // 10 * 4 = sessionId data 44 + 44 + 4 + 2 + 10 * 4 为userId的start
                // 2 = userId len
                // 1000 * 4 = userId data
                ByteBuffer byteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 44 + 44 + 4 + 2 + 10 * 4, 2 + 1000 * 4);
                short len = byteBuffer.getShort();
                if (len != 0) {
                    byte[] result = new byte[len];
                    byteBuffer.get(result);
                    this.persistentDataProvider.setLoginUserIdAndUserKey(new String(result), null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fileChannel != null) {
                    try {
                        fileChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
