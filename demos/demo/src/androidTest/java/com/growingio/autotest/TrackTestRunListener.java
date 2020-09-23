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

package com.growingio.autotest;

import android.util.Log;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class TrackTestRunListener extends RunListener {
    private static final String TAG  = "TrackTestRunListener";
    @Override
    public void testRunStarted(Description description) throws Exception {
        Log.e(TAG, "testRunStarted: ");
        super.testRunStarted(description);
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        Log.e(TAG, "testRunFinished: ");
        super.testRunFinished(result);
    }

    @Override
    public void testStarted(Description description) throws Exception {
        Log.e(TAG, "testStarted: ");
        super.testStarted(description);
    }

    @Override
    public void testFinished(Description description) throws Exception {
        Log.e(TAG, "testFinished: ");
        super.testFinished(description);
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        Log.e(TAG, "testFailure: ");
        super.testFailure(failure);
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        Log.e(TAG, "testAssumptionFailure: ");
        super.testAssumptionFailure(failure);
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        Log.e(TAG, "testIgnored: ");
        super.testIgnored(description);
    }
}
