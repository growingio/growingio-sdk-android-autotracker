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

package com.growingio.android.sdk.autotrack.util;

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

import com.google.common.truth.Truth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;

/**
 * 通过利用系统类反射调用， 从而绕过检测
 */
@RunWith(PowerMockRunner.class)
public class HurtLockerTest {

    @Test
    public void testHurtLock() throws Exception {
        int maxSize = HurtLocker.getInternalState(new ArrayList<String>(), "MAX_ARRAY_SIZE");
        Truth.assertThat(maxSize == (Integer.MAX_VALUE - 8)).isTrue();
        String superValue = HurtLocker.getInternalState(new SubClass(), "mSuperValue");
        Truth.assertThat(superValue).isEqualTo("superValue");
    }
}

class SuperClass {
    private String mSuperValue = "superValue";
}

class SubClass extends SuperClass {
}
