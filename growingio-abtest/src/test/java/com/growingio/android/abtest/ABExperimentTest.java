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
package com.growingio.android.abtest;

import static org.junit.Assert.assertEquals;

import com.growingio.android.sdk.track.middleware.abtest.ABExperiment;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ABExperimentTest {
    @Test
    public void ABExperimentEqualsTest() {
        Map<String, String> data1 = new HashMap<>();
        data1.put("name", "cpacm");
        data1.put("hobby", "kick");
        ABExperiment a1 = new ABExperiment("layerId", 100, 100, data1);

        Map<String, String> data2 = new HashMap<>();
        data2.put("name", "cpacm");
        data2.put("hobby", "kick");
        ABExperiment a2 = new ABExperiment("layerId", 100, 100, data2);

        assertEquals(a1, a2);
    }
}
