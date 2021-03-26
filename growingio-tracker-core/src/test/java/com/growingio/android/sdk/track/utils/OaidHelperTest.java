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

package com.growingio.android.sdk.track.utils;

import android.content.Context;

import com.bun.miitmdid.core.MdidSdkHelper;
import com.bun.miitmdid.interfaces.IdSupplier;
import com.google.common.truth.Truth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MdidSdkHelper.class)
public class OaidHelperTest {
    @Before
    public void setUp() {
        PowerMockito.mockStatic(MdidSdkHelper.class);
    }

    @Test
    public void getOaidAsync() throws InterruptedException {
        OaidHelper oaidHelper = new OaidHelper();
        final IdSupplier idSupplier = PowerMockito.mock(IdSupplier.class);
        Context context = PowerMockito.mock(Context.class);
        Semaphore semaphore = new Semaphore(1);
        semaphore.acquire();
        PowerMockito.when(MdidSdkHelper.InitSdk(context, true, oaidHelper)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        semaphore.release();
                        oaidHelper.OnSupport(true, idSupplier);
                    }
                }).start();
                return null;
            }
        });
        semaphore.tryAcquire(1, TimeUnit.SECONDS);
        PowerMockito.when(idSupplier.getOAID()).thenReturn("oaid");
        Truth.assertThat("oaid").isEqualTo(oaidHelper.getOaid(context));
    }

    @Test
    public void getOaidSync() {
        OaidHelper oaidHelper = new OaidHelper();
        final IdSupplier idSupplier = PowerMockito.mock(IdSupplier.class);
        Context context = PowerMockito.mock(Context.class);
        Semaphore semaphore = new Semaphore(1);
        PowerMockito.when(MdidSdkHelper.InitSdk(context, true, oaidHelper)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                oaidHelper.OnSupport(true, idSupplier);
                return null;
            }
        });
        PowerMockito.when(idSupplier.getOAID()).thenReturn("oaid");
        Truth.assertThat("oaid").isEqualTo(oaidHelper.getOaid(context));
    }
}
