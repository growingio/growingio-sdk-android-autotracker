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

package com.growingio.android.oaid;

import android.content.Context;

import com.growingio.android.sdk.track.log.Logger;
import com.growingio.android.sdk.track.modelloader.ModelLoader;
import com.growingio.android.sdk.track.modelloader.ModelLoaderFactory;
import com.growingio.android.sdk.track.utils.OaidHelper;

public class OaidDataLoader implements ModelLoader<OaidHelper, String> {
    private final Context mContext;
    private final IOaidHelper mOaidHelper;

    public OaidDataLoader(Context context, IOaidHelper oaidHelper) {
        this.mContext = context;
        this.mOaidHelper = oaidHelper;
    }

    @Override
    public LoadData<String> buildLoadData(OaidHelper oaidHelper) {
        return new LoadData<>(new OaidDataFetcher(mContext, mOaidHelper));
    }

    public static class Factory implements ModelLoaderFactory<OaidHelper, String> {
        private static final String TAG = "OaidDataLoader.Factory";
        private final Context mContext;
        private static volatile IOaidHelper sOaidHelper;

        public Factory(Context context) {
            this.mContext = context;
            if (sOaidHelper == null) {
                synchronized (Factory.class) {
                    if (sOaidHelper == null) {
                        // 高版本可能包含低版本的类, 需要优先判断是否为高版本
                        try {
                            if (hasClass("com.bun.miitmdid.core.CertChecker")) {
                                sOaidHelper = new OaidHelper1026();
                            } else if (hasClass("com.bun.miitmdid.interfaces.IIdentifierListener")) {
                                sOaidHelper = new OaidHelper1025();
                            } else if (hasClass("com.bun.supplier.IIdentifierListener")) {
                                sOaidHelper = new OaidHelper1013();
                            } else if (hasClass("com.bun.miitmdid.core.IIdentifierListener")) {
                                sOaidHelper = new OaidHelper1010();
                            }
                        } catch (Throwable throwable) {
                            Logger.d(TAG, "not compatible with the version of oaid sdk");
                        }

                        if (sOaidHelper == null) {
                            // 异常情况下使用空实现
                            sOaidHelper = new IOaidHelper() {
                                @Override
                                public void preloadOaid(Context context) {
                                }

                                @Override
                                public String getOaid() {
                                    return null;
                                }
                            };
                        } else {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    sOaidHelper.preloadOaid(context);
                                }
                            }, TAG).start();
                        }

                    }
                }
            }
        }

        @Override
        public ModelLoader<OaidHelper, String> build() {
            return new OaidDataLoader(mContext, sOaidHelper);
        }

        private static boolean hasClass(String className) {
            try {
                Class.forName(className);
                return true;
            } catch (Throwable e) {
                return false;
            }
        }
    }
}
